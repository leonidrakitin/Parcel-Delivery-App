package com.parceldelivery.deliveryservice.service;

import com.parceldelivery.deliveryservice.dto.CoordinatesDto;
import com.parceldelivery.deliveryservice.dto.DeliveryDto;
import com.parceldelivery.deliveryservice.dto.DeliveryStatusDto;
import com.parceldelivery.deliveryservice.model.Delivery;
import com.parceldelivery.deliveryservice.model.DeliveryStatus;
import com.parceldelivery.deliveryservice.repository.CoordinatesRepository;
import com.parceldelivery.deliveryservice.repository.DeliveryRepository;
import com.parceldelivery.shared.kafka.config.KafkaTopicConfig;
import com.parceldelivery.shared.kafka.OrderChangeStatusMessage;
import com.parceldelivery.shared.model.OrderStatus;
import com.parceldelivery.shared.security.jwt.UserDetailsImpl;
import com.parceldelivery.shared.security.jwt.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

	private final DeliveryRepository deliveryRepository;
	private final CoordinatesRepository coordinatesRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final KafkaTopicConfig kafkaTopicConfig;

	@Override
	public List<DeliveryDto> getDeliveries() {
		UserDetailsImpl principal = AuthUtil.getPrincipal();
		List<Delivery> results = AuthUtil.isAdmin()
				? deliveryRepository.findAll()
				: deliveryRepository.findAllByCourierId(principal.getId());
		return results.stream().map(DeliveryServiceImpl::mapToDeliveryDto).toList();
	}

	@Override
	public DeliveryDto getDeliveryDetails(UUID orderId) {
		return deliveryRepository.findById(orderId)
				.map(DeliveryServiceImpl::mapToDeliveryDto)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found delivery " + orderId));
	}

	@Override
	@Transactional
	public void editStatus(UUID orderId, DeliveryStatusDto dto) {
		Delivery delivery = deliveryRepository.findById(orderId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found delivery " + orderId));
		if (delivery.getStatus().equals(DeliveryStatus.CANCELED)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This delivery was cancelled -> " + orderId);
		} else if (dto.newStatus().equals(DeliveryStatus.ORDERED)) {
			delivery.setCourier(null);
		}
		delivery.setStatus(dto.newStatus());

		var orderStatus = defineOrderStatus(dto);
		var message = new OrderChangeStatusMessage(orderId, orderStatus);
		kafkaTemplate.send(kafkaTopicConfig.getEditStatusDeliveryTopic(), message);
		log.info("Edited status of the {} order and {} delivery to the {}", orderId, orderStatus, dto.newStatus());
	}

	@Override
	public CoordinatesDto getDeliveryCoordinates(UUID orderId) {
		var coordinates = coordinatesRepository.findActualDeliveryLocation(orderId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found delivery " + orderId));
		return CoordinatesDto.builder()
				.orderId(coordinates.getOrderId())
				.latitude(coordinates.getLatitude())
				.longitude(coordinates.getLongitude())
				.build();
	}

	private OrderStatus defineOrderStatus(DeliveryStatusDto dto) {
		return switch(dto.newStatus()) {
			case ORDERED -> OrderStatus.CREATED;
			case ASSIGNED -> OrderStatus.ASSIGNED;
			case PICKED_UP, OUT_FOR_DELIVERY -> OrderStatus.DELIVERING;
			case DELIVERED -> OrderStatus.COMPLETED;
			case CANCELED -> OrderStatus.CANCELLED;
		};
	}

	static DeliveryDto mapToDeliveryDto(Delivery delivery) {
		return DeliveryDto.builder()
				.orderId(delivery.getOrderId())
				.courier(delivery.getCourier())
				.destination(delivery.getDestination())
				.status(delivery.getStatus())
				.build();
	}
}
