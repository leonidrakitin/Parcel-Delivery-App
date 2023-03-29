package com.parceldelivery.deliveryservice.service;

import com.parceldelivery.deliveryservice.dto.CoordinatesDto;
import com.parceldelivery.deliveryservice.dto.DeliveryDto;
import com.parceldelivery.deliveryservice.dto.DeliveryStatusDto;
import com.parceldelivery.deliveryservice.kafka.DeliveryKafkaConsumer;
import com.parceldelivery.deliveryservice.model.Coordinates;
import com.parceldelivery.deliveryservice.model.Delivery;
import com.parceldelivery.deliveryservice.model.DeliveryStatus;
import com.parceldelivery.deliveryservice.repository.CoordinatesRepository;
import com.parceldelivery.deliveryservice.repository.DeliveryRepository;
import com.parceldelivery.shared.kafka.config.KafkaTopicConfig;
import com.parceldelivery.shared.kafka.OrderChangeStatusMessage;
import com.parceldelivery.shared.test.annotation.MockAdminDetails;
import com.parceldelivery.shared.test.annotation.MockCourierDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.parceldelivery.deliveryservice.util.DeliveryTestUtil.generateCoordinatesEntity;
import static com.parceldelivery.deliveryservice.util.DeliveryTestUtil.generateDelivery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(locations = "/application.yml")
class DeliveryServiceTest {
	@MockBean
	private DeliveryRepository deliveryRepository;

	@MockBean
	private CoordinatesRepository coordinatesRepository;

	@MockBean
	private KafkaTemplate<String, Object> kafkaTemplate;

	@MockBean
	private DeliveryKafkaConsumer kafkaConsumer;

	@Autowired
	private KafkaTopicConfig kafkaTopicConfig;

	@Autowired
	private DeliveryService deliveryService;

	@Test
	@MockAdminDetails
	void testGetDeliveries_whenAdmin_returnAllDeliveries() {
		List<Delivery> deliveryList = Arrays.asList(
				generateDelivery(DeliveryStatus.PICKED_UP),
				generateDelivery(DeliveryStatus.OUT_FOR_DELIVERY),
				generateDelivery(DeliveryStatus.ORDERED)
		);
		given(deliveryRepository.findAll()).willReturn(deliveryList);

		List<DeliveryDto> results = deliveryService.getDeliveries();

		assertEquals(3, results.size());
		for (int i = 0; i < results.size(); i++) {
			assertEquals(results.get(i).orderId(), deliveryList.get(i).getOrderId());
			assertEquals(results.get(i).status(), deliveryList.get(i).getStatus());
			assertEquals(results.get(i).courier(), deliveryList.get(i).getCourier());
			assertEquals(results.get(i).destination(), deliveryList.get(i).getDestination());
		}

		verify(deliveryRepository, times(1)).findAll();
	}

	@Test
	@MockCourierDetails
	void testGetDeliveries_whenCourier_returnAllCourierDeliveries() {
		List<Delivery> deliveryList = Arrays.asList(
				generateDelivery(DeliveryStatus.ORDERED),
				generateDelivery(DeliveryStatus.OUT_FOR_DELIVERY)
		);
		given(deliveryRepository.findAllByCourierId(3L)).willReturn(deliveryList);

		List<DeliveryDto> results = deliveryService.getDeliveries();

		assertEquals(2, results.size());
		for (int i = 0; i < results.size(); i++) {
			assertEquals(results.get(i).orderId(), deliveryList.get(i).getOrderId());
			assertEquals(results.get(i).status(), deliveryList.get(i).getStatus());
			assertEquals(results.get(i).courier(), deliveryList.get(i).getCourier());
			assertEquals(results.get(i).destination(), deliveryList.get(i).getDestination());
		}

		verify(deliveryRepository, times(1)).findAllByCourierId(3L);
	}

	@Test
	@MockCourierDetails
	void testGetDeliveries_whenCourier_returnEmptyList() {
		List<Delivery> deliveryList = List.of();
		given(deliveryRepository.findAllByCourierId(3L)).willReturn(deliveryList);
		List<DeliveryDto> results = deliveryService.getDeliveries();
		assertEquals(0, results.size());
		verify(deliveryRepository, times(1)).findAllByCourierId(3L);
	}

	@Test
	void testGetDeliveryDetails_returnDelivery() {
		Delivery delivery = generateDelivery(DeliveryStatus.ORDERED);

		given(deliveryRepository.findById(delivery.getOrderId())).willReturn(Optional.of(delivery));

		DeliveryDto result = deliveryService.getDeliveryDetails(delivery.getOrderId());

		assertNotNull(result);
		assertEquals(delivery.getOrderId(), result.orderId());
		assertEquals(delivery.getCourier(), result.courier());
		assertEquals(delivery.getStatus(), result.status());
		assertEquals(delivery.getDestination(), result.destination());

		verify(deliveryRepository, times(1)).findById(delivery.getOrderId());
	}

	@Test
	void testGetDeliveryDetails_throwException() {
		UUID orderId = UUID.randomUUID();
		given(deliveryRepository.findById(orderId)).willReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> deliveryService.getDeliveryDetails(orderId));
		verify(deliveryRepository, times(1)).findById(orderId);
	}

	@Test
	void testEditStatus_shouldBeOk() {
		Delivery delivery = generateDelivery(UUID.randomUUID(), "destination");
		DeliveryStatusDto deliveryStatusDto = new DeliveryStatusDto(DeliveryStatus.ORDERED);

		given(deliveryRepository.findById(delivery.getOrderId())).willReturn(Optional.of(delivery));
		given(kafkaTemplate.send(anyString(), any(OrderChangeStatusMessage.class))).willReturn(null);

		deliveryService.editStatus(delivery.getOrderId(), deliveryStatusDto);

		verify(kafkaTemplate, times(1)).send(anyString(), any(OrderChangeStatusMessage.class));
		verify(deliveryRepository, times(1)).findById(delivery.getOrderId());
	}

	@Test
	void testEditStatus_throwNotFound() {
		UUID orderId = UUID.randomUUID();
		DeliveryStatusDto deliveryStatusDto = new DeliveryStatusDto(DeliveryStatus.ORDERED);

		given(deliveryRepository.findById(orderId)).willReturn(Optional.empty());

		assertThrows(ResponseStatusException.class,
				() -> deliveryService.editStatus(orderId, deliveryStatusDto));

		verify(kafkaTemplate, never()).send(anyString(), any(OrderChangeStatusMessage.class));
		verify(deliveryRepository, times(1)).findById(orderId);
	}

	@Test
	void testEditStatus_throwBadRequest() {
		Delivery delivery = generateDelivery(DeliveryStatus.CANCELED);
		DeliveryStatusDto deliveryStatusDto = new DeliveryStatusDto(delivery.getStatus());

		given(deliveryRepository.findById(delivery.getOrderId())).willReturn(Optional.of(delivery));

		assertThrows(ResponseStatusException.class,
				() -> deliveryService.editStatus(delivery.getOrderId(), deliveryStatusDto));

		verify(kafkaTemplate, never()).send(anyString(), any(OrderChangeStatusMessage.class));
		verify(deliveryRepository, times(1)).findById(delivery.getOrderId());
		verify(deliveryRepository, never()).save(any());
	}

	@Test
	void testGetDeliveryCoordinates_shouldBeOk() {
		Coordinates coordinates = generateCoordinatesEntity(UUID.randomUUID());

		given(coordinatesRepository.findActualDeliveryLocation(coordinates.getOrderId()))
				.willReturn(Optional.of(coordinates));

		CoordinatesDto coordinatesDto = deliveryService.getDeliveryCoordinates(coordinates.getOrderId());

		assertNotNull(coordinatesDto);
		assertEquals(coordinatesDto.orderId(), coordinates.getOrderId());
		assertEquals(coordinatesDto.latitude(), coordinates.getLatitude());
		assertEquals(coordinatesDto.longitude(), coordinates.getLongitude());

		verify(coordinatesRepository, times(1)).findActualDeliveryLocation(coordinates.getOrderId());
	}

	@Test
	void testGetDeliveryCoordinates_throwNotFound() {
		UUID orderId = UUID.randomUUID();
		given(coordinatesRepository.findActualDeliveryLocation(orderId)).willReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> deliveryService.getDeliveryCoordinates(orderId));
		verify(coordinatesRepository, times(1)).findActualDeliveryLocation(orderId);
	}
}