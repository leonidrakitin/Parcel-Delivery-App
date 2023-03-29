package com.parceldelivery.deliveryservice.kafka;

import com.parceldelivery.deliveryservice.exception.OrderNotFoundException;
import com.parceldelivery.deliveryservice.model.Coordinates;
import com.parceldelivery.deliveryservice.model.Delivery;
import com.parceldelivery.deliveryservice.repository.CoordinatesRepository;
import com.parceldelivery.deliveryservice.repository.DeliveryRepository;
import com.parceldelivery.shared.kafka.OrderMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static com.parceldelivery.deliveryservice.util.DeliveryTestUtil.generateCoordinatesEntity;
import static com.parceldelivery.deliveryservice.util.DeliveryTestUtil.generateDelivery;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@SpringBootTest
@TestPropertySource(locations = "/application.yml")
class DeliveryKafkaConsumerServiceTest {

	@MockBean
	private DeliveryRepository deliveryRepository;

	@MockBean
	private CoordinatesRepository coordinatesRepository;

	@MockBean
	private DeliveryKafkaConsumer kafkaConsumer;

	@Autowired
	private DeliveryKafkaConsumerService service;

	@Test
	void testPutDelivery_ifExists() {
		OrderMessage message = new OrderMessage(UUID.randomUUID(), "new-destination");
		Delivery delivery = generateDelivery(message.orderId(), "old-destination");

		given(deliveryRepository.findById(message.orderId())).willReturn(Optional.of(delivery));

		service.putDelivery(message);

		verify(deliveryRepository, times(1)).findById(message.orderId());
	}

	@Test
	void testPutDelivery_ifEmpty() {
		OrderMessage message = new OrderMessage(UUID.randomUUID(), "new-destination");
		Delivery delivery = generateDelivery(message.orderId(), message.destination());
		Coordinates coordinates = generateCoordinatesEntity(message.orderId());

		given(deliveryRepository.findById(message.orderId())).willReturn(Optional.empty());
		given(deliveryRepository.save(any())).willReturn(delivery);
		given(coordinatesRepository.save(any())).willReturn(coordinates);

		service.putDelivery(message);

		verify(deliveryRepository, times(1)).findById(message.orderId());
		verify(deliveryRepository, times(1)).save(any());
		verify(coordinatesRepository, times(1)).save(any());
	}

	@Test
	void testTestCancelDelivery_returnOk() {
		UUID orderId = UUID.randomUUID();
		Delivery delivery = generateDelivery(orderId, "destination");
		given(deliveryRepository.findById(orderId)).willReturn(Optional.of(delivery));
		service.cancelDelivery(orderId);
		verify(deliveryRepository, times(1)).findById(orderId);
	}

	@Test
	void testCancelDelivery_notFound() {
		UUID orderId = UUID.randomUUID();
		given(deliveryRepository.findById(orderId)).willReturn(Optional.empty());
		assertThrows(OrderNotFoundException.class, () -> service.cancelDelivery(orderId));
		verify(deliveryRepository, times(1)).findById(orderId);
	}
}