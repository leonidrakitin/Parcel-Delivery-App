package com.parceldelivery.deliveryservice.service;

import com.parceldelivery.deliveryservice.dto.CourierAssignmentDto;
import com.parceldelivery.deliveryservice.dto.CourierStatusDto;
import com.parceldelivery.deliveryservice.kafka.DeliveryKafkaConsumer;
import com.parceldelivery.deliveryservice.kafka.DeliveryKafkaConsumerService;
import com.parceldelivery.deliveryservice.model.Delivery;
import com.parceldelivery.deliveryservice.model.DeliveryStatus;
import com.parceldelivery.deliveryservice.repository.DeliveryRepository;
import com.parceldelivery.deliveryservice.service.client.AuthFeignClient;
import com.parceldelivery.shared.model.RoleType;
import com.parceldelivery.shared.model.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static com.parceldelivery.deliveryservice.util.DeliveryTestUtil.generateDelivery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(locations = "/application.yml")
class CourierServiceTest {
	@MockBean
	private DeliveryRepository deliveryRepository;

	@MockBean
	private DeliveryService deliveryService;

	@MockBean
	private DeliveryKafkaConsumerService deliveryKafkaConsumerService;

	@MockBean
	private DeliveryKafkaConsumer kafkaConsumer;

	@MockBean
	private AuthFeignClient authFeignClient;

	@Autowired
	private CourierService courierService;

	@Test
	void testGetListCouriersStatus() {
		List<Delivery> deliveryList = Arrays.asList(
				generateDelivery(DeliveryStatus.ORDERED, 3L, "courier"),
				generateDelivery(DeliveryStatus.OUT_FOR_DELIVERY, 2L, "courier2")
		);
		given(deliveryRepository.findAll()).willReturn(deliveryList);

		List<CourierStatusDto> results = courierService.getListCouriersStatus();
		assertEquals(2, results.size());
		assertEquals(1, results.get(0).deliveryDtoList().size());
		assertEquals(1, results.get(1).deliveryDtoList().size());

		verify(deliveryRepository, times(1)).findAll();
	}

	@Test
	void testAssignCourier_thenOk() {
		String token = "token";
		CourierAssignmentDto dto = new CourierAssignmentDto(UUID.randomUUID(), new Random().nextLong());
		Delivery delivery = generateDelivery(dto.orderId(), "destination");
		UserDto userDto = new UserDto(3L, "courier", "courier@company.com", RoleType.ROLE_COURIER);

		given(deliveryRepository.findById(dto.orderId())).willReturn(Optional.of(delivery));
		given(authFeignClient.getUser(token, dto.courierId())).willReturn(ResponseEntity.ok(userDto));

		courierService.assignCourier(token, dto);

		verify(authFeignClient, times(1)).getUser(token, dto.courierId());
		verify(deliveryRepository, times(1)).save(any());
		verify(deliveryRepository, times(1)).findById(dto.orderId());
		verify(deliveryService, times(1)).editStatus(any(), any());
	}

	@Test
	void testAssignCourier_throwNotFoundException() {
		String token = "token";
		CourierAssignmentDto dto = new CourierAssignmentDto(UUID.randomUUID(), new Random().nextLong());

		given(deliveryRepository.findById(dto.orderId())).willReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> courierService.assignCourier(token, dto));

		verify(deliveryRepository, times(1)).findById(dto.orderId());
		verify(authFeignClient, never()).getUser(token, dto.courierId());
		verify(deliveryRepository, never()).save(any());
		verify(deliveryService, never()).editStatus(any(), any());
	}

	@Test
	void testAssignCourier_throwBadRequestException() {
		String token = "token";
		CourierAssignmentDto dto = new CourierAssignmentDto(UUID.randomUUID(), new Random().nextLong());
		Delivery delivery = generateDelivery(dto.orderId(), "destination");
		UserDto userDto = new UserDto(3L, "courier", "courier@company.com", RoleType.ROLE_USER);

		given(deliveryRepository.findById(dto.orderId())).willReturn(Optional.of(delivery));
		given(authFeignClient.getUser(token, dto.courierId())).willReturn(ResponseEntity.ok(userDto));

		assertThrows(ResponseStatusException.class, () -> courierService.assignCourier(token, dto));

		verify(deliveryRepository, times(1)).findById(dto.orderId());
		verify(authFeignClient, times(1)).getUser(token, dto.courierId());
		verify(deliveryRepository, never()).save(any());
		verify(deliveryService, never()).editStatus(any(), any());
	}
}