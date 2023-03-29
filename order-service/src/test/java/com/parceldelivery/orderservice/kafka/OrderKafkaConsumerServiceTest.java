package com.parceldelivery.orderservice.kafka;

import com.parceldelivery.orderservice.model.Order;
import com.parceldelivery.orderservice.repository.OrderRepository;
import com.parceldelivery.shared.kafka.config.KafkaTopicConfig;
import com.parceldelivery.shared.kafka.OrderChangeStatusMessage;
import com.parceldelivery.shared.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static com.parceldelivery.orderservice.util.OrderTestUtil.generateOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(locations = "/application.yml")
class OrderKafkaConsumerServiceTest {

	@MockBean
	private OrderRepository orderRepository;

	@MockBean
	private KafkaTemplate<String, Object> kafkaTemplate;

	@MockBean
	private OrderKafkaConsumer orderKafkaConsumer;

	@Autowired
	private KafkaTopicConfig kafkaTopicConfig;

	@Autowired
	private OrderKafkaConsumerService service;

	@Test
	public void testUpdateStatus_orderExists_statusUpdated() {
		Order order = generateOrder(OrderStatus.DELIVERING, 2L, "user");
		OrderChangeStatusMessage message = new OrderChangeStatusMessage(order.getOrderId(), OrderStatus.DELIVERING);

		given(orderRepository.findById(order.getOrderId())).willReturn(Optional.of(order));
		given(kafkaTemplate.send(anyString(), any(OrderChangeStatusMessage.class))).willReturn(null);

		service.updateStatus(message);

		verify(orderRepository, times(1)).findById(order.getOrderId());
		verify(kafkaTemplate, times(1)).send(anyString(), any(OrderChangeStatusMessage.class));
	}

	@Test
	public void testUpdateStatus_orderDoesNotExist_statusNotUpdated() {
		UUID orderId = UUID.randomUUID();
		OrderChangeStatusMessage message = new OrderChangeStatusMessage(orderId, OrderStatus.CANCELLED);

		given(orderRepository.findById(orderId)).willReturn(Optional.empty());

		service.updateStatus(message);

		verify(kafkaTemplate, never()).send(anyString(), any(OrderChangeStatusMessage.class));
	}
}