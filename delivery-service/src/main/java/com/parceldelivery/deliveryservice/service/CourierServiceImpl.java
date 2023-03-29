package com.parceldelivery.deliveryservice.service;

import com.parceldelivery.deliveryservice.dto.CourierAssignmentDto;
import com.parceldelivery.deliveryservice.dto.CourierStatusDto;
import com.parceldelivery.deliveryservice.dto.DeliveryDto;
import com.parceldelivery.deliveryservice.dto.DeliveryStatusDto;
import com.parceldelivery.deliveryservice.model.Delivery;
import com.parceldelivery.deliveryservice.model.DeliveryStatus;
import com.parceldelivery.deliveryservice.repository.DeliveryRepository;
import com.parceldelivery.deliveryservice.service.client.AuthFeignClient;
import com.parceldelivery.shared.model.RoleType;
import com.parceldelivery.shared.model.UserDto;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourierServiceImpl implements CourierService {

	private final DeliveryRepository deliveryRepository;
	private final DeliveryService deliveryService;
	private final AuthFeignClient authFeignClient;

	@Override
	public List<CourierStatusDto> getListCouriersStatus() {
		return deliveryRepository.findAll().stream()
				.filter(delivery -> Objects.nonNull(delivery.getCourier()))
				.map(DeliveryServiceImpl::mapToDeliveryDto)
				.collect(Collectors.groupingBy(DeliveryDto::courier))
				.entrySet().stream()
				.map(entry -> new CourierStatusDto(entry.getKey(), entry.getValue()))
				.toList();
	}

	@Override
	@Transactional
	public void assignCourier(@RequestHeader("Authorization") String token, CourierAssignmentDto dto) {
		Delivery delivery = deliveryRepository.findById(dto.orderId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found delivery " + dto.orderId()));

		UserDto userInfo;
		try {
			userInfo = getCourier(token, dto);
		} catch (FeignException e) {
			log.error(e.responseBody().toString());
			throw new ResponseStatusException(e.status(), "Courier's ID is wrong", e.getCause());
		}
		if (userInfo == null || userInfo.role() != RoleType.ROLE_COURIER) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong ID, it does not belong to the courier type");
		}

		delivery.setCourier(userInfo.username());
		delivery.setCourierId(userInfo.id());
		deliveryRepository.save(delivery);

		deliveryService.editStatus(delivery.getOrderId(), new DeliveryStatusDto(DeliveryStatus.ASSIGNED));
		log.info("Assigned courierId {} to the {} order", dto.courierId(), dto.orderId());
	}

	@CircuitBreaker(name = "getCourier", fallbackMethod = "fallbackGetCourier")
	@Retry(name = "retryGetCourier", fallbackMethod = "fallbackGetCourier")
	@TimeLimiter(name = "timeLimitGetCourier", fallbackMethod = "fallbackGetCourier")
	private UserDto getCourier(String token, CourierAssignmentDto dto) {
		ResponseEntity<UserDto> response = authFeignClient.getUser(token, dto.courierId());
		return response.getBody();
	}

	@SuppressWarnings("unused")
	private void fallbackGetCourier(String token, CourierAssignmentDto dto, Throwable t) {
		log.error(t.getMessage());
		if (deliveryRepository.findAllByCourierId(dto.courierId()).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Courier's ID is probably wrong, check it and try later.");
		}
	}
}
