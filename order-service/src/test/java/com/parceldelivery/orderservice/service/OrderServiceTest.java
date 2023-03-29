package com.parceldelivery.orderservice.service;

import com.parceldelivery.orderservice.dto.CreateOrderDto;
import com.parceldelivery.orderservice.dto.OrderDestinationUpdateDto;
import com.parceldelivery.orderservice.dto.OrderDto;
import com.parceldelivery.orderservice.kafka.OrderKafkaConsumer;
import com.parceldelivery.orderservice.kafka.OrderKafkaConsumerService;
import com.parceldelivery.orderservice.model.Order;
import com.parceldelivery.orderservice.repository.OrderRepository;
import com.parceldelivery.shared.kafka.config.KafkaTopicConfig;
import com.parceldelivery.shared.kafka.OrderMessage;
import com.parceldelivery.shared.model.OrderStatus;
import com.parceldelivery.shared.test.annotation.MockAdminDetails;
import com.parceldelivery.shared.test.annotation.MockUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.parceldelivery.orderservice.util.OrderTestUtil.generateCreateOrderDto;
import static com.parceldelivery.orderservice.util.OrderTestUtil.generateOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(locations = "/application.yml")
class OrderServiceTest {
	@MockBean
	private OrderRepository orderRepository;

	@MockBean
	private KafkaTemplate<String, Object> kafkaTemplate;

	@MockBean
	private OrderKafkaConsumer kafkaConsumer;

	@MockBean
	private OrderKafkaConsumerService orderKafkaConsumerService;

	@Autowired
	private KafkaTopicConfig kafkaTopicConfig;

	@Autowired
	private OrderService orderService;

	@Test
	@MockAdminDetails
	public void testGetAllOrders_shouldReturnListOfOrderDto() {
		List<Order> orders = Arrays.asList(
				generateOrder(OrderStatus.CREATED, 2L, "user"),
				generateOrder(OrderStatus.CREATED, 2L, "user"));
		given(orderRepository.findAll(PageRequest.of(0, 10))).willReturn(new PageImpl<>(orders));

		List<OrderDto> results = orderService.getAllOrders(10, 0);

		assertEquals(2, results.size());
		for (int i = 0; i < results.size(); i++) {
			assertEquals(orders.get(i).getOrderId(), results.get(i).orderId());
			assertEquals(orders.get(i).getDescription(), results.get(i).description());
			assertEquals(orders.get(i).getDestination(), results.get(i).destination());
			assertEquals(orders.get(i).getStatus(), results.get(i).status());
		}
		verify(orderRepository, times(1)).findAll(PageRequest.of(0, 10));
	}

	@Test
	@MockUserDetails
	public void testGetAllOrders_ByCreatedById_shouldReturnListOfUserOrder() {
		Order userOrder = generateOrder(OrderStatus.CREATED, 2L, "user");
		List<Order> orders = Collections.singletonList(userOrder);

		given(orderRepository.findAllByCreatedById(anyLong())).willReturn(orders);

		List<OrderDto> results = orderService.getAllOrders(10, 0);
		assertEquals(1, results.size());

		OrderDto result = results.iterator().next();

		assertEquals(userOrder.getOrderId(), result.orderId());
		assertEquals(userOrder.getDescription(), result.description());
		assertEquals(userOrder.getDestination(), result.destination());
		assertEquals(userOrder.getStatus(), result.status());

		verify(orderRepository, times(1)).findAllByCreatedById(anyLong());
	}

	@Test
	@MockAdminDetails
	public void testGetAllOrders_shouldReturnEmptyList() {
		List<Order> orders = List.of();
		given(orderRepository.findAll(PageRequest.of(0, 10))).willReturn(new PageImpl<>(orders));
		List<OrderDto> results = orderService.getAllOrders(10, 0);
		assertEquals(0, results.size());
		verify(orderRepository, times(1)).findAll(PageRequest.of(0, 10));
	}

	@Test
	@MockAdminDetails
	public void testGetOrder_whenAdmin_shouldReturnOrder() {
		Order order = generateOrder(OrderStatus.CREATED, 2L, "user");
		given(orderRepository.findById(order.getOrderId())).willReturn(Optional.of(order));

		OrderDto result = orderService.getOrder(order.getOrderId());

		assertEquals(order.getOrderId(), result.orderId());
		assertEquals(order.getDescription(), result.description());
		assertEquals(order.getDestination(), result.destination());
		assertEquals(order.getStatus(), result.status());
		verify(orderRepository, times(1)).findById(order.getOrderId());
	}

	@Test
	@MockUserDetails
	public void testGetOrder_whenUser_shouldReturnOrder() {
		Order order = generateOrder(OrderStatus.CREATED, 2L, "user");
		given(orderRepository.findById(order.getOrderId())).willReturn(Optional.of(order));

		OrderDto result = orderService.getOrder(order.getOrderId());

		assertEquals(order.getOrderId(), result.orderId());
		assertEquals(order.getDescription(), result.description());
		assertEquals(order.getDestination(), result.destination());
		assertEquals(order.getStatus(), result.status());
		verify(orderRepository, times(1)).findById(order.getOrderId());
	}

	@Test
	@MockUserDetails
	public void testGetOrder_whenUserHaveNoOrders_shouldThrowException() {
		UUID orderId = UUID.randomUUID();
		given(orderRepository.findById(orderId)).willReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> orderService.getOrder(orderId));
		verify(orderRepository, times(1)).findById(orderId);
	}

	@Test
	@MockUserDetails
	public void testGetOrder_whenUserTriesStealInfoAnotherUserOrder_shouldThrowException() {
		Order order = generateOrder(OrderStatus.CREATED, 3L, "anotherUser");
		given(orderRepository.findById(order.getOrderId())).willReturn(Optional.of(order));

		assertThrows(ResponseStatusException.class, () -> orderService.getOrder(order.getOrderId()));

		verify(orderRepository, times(1)).findById(order.getOrderId());
	}

	@Test
	@MockUserDetails
	public void testCreateOrder_shouldSaveOrder() {
		CreateOrderDto request = generateCreateOrderDto();
		Order savedOrder = generateOrder(OrderStatus.CREATED, 2L, "user");

		given(orderRepository.save(any(Order.class))).willReturn(savedOrder);
		given(kafkaTemplate.send(anyString(), any(OrderMessage.class))).willReturn(null);

		OrderDto result = orderService.createOrder(request);

		assertNotNull(result.orderId());
		assertEquals(savedOrder.getCreatedByName(), result.createdBy());
		assertEquals(savedOrder.getOrderId(), result.orderId());
		assertEquals(savedOrder.getDescription(), result.description());
		assertEquals(savedOrder.getDestination(), result.destination());
		assertEquals(savedOrder.getStatus(), result.status());
		assertEquals(savedOrder.getPrice(), result.price());

		verify(kafkaTemplate, times(1)).send(anyString(), any(OrderMessage.class));
		verify(orderRepository, times(1)).save(any(Order.class));
	}

	@Test
	void testUpdateDestination_shouldUpdate() {
		OrderDestinationUpdateDto request = new OrderDestinationUpdateDto("New destination");
		Order order = generateOrder(OrderStatus.CREATED, 2L, "user");

		given(orderRepository.findById(order.getOrderId())).willReturn(Optional.of(order));
		given(kafkaTemplate.send(anyString(), any(OrderMessage.class))).willReturn(null);

		orderService.updateDestination(order.getOrderId(), request);

		verify(kafkaTemplate, times(1)).send(anyString(), any(OrderMessage.class));
		verify(orderRepository, times(1)).findById(order.getOrderId());
	}

	@Test
	void testUpdateDestination_shouldNotFoundTest() {
		UUID orderId = UUID.randomUUID();
		OrderDestinationUpdateDto request = new OrderDestinationUpdateDto("New destination");
		given(orderRepository.findById(orderId)).willReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> orderService.updateDestination(orderId, request));
		verify(orderRepository, times(1)).findById(orderId);
	}

	@Test
	void testUpdateDestination_whenOrderCancelled_shouldThrowException() {
		OrderDestinationUpdateDto request = new OrderDestinationUpdateDto("New destination");
		Order order = generateOrder(OrderStatus.CANCELLED, 1L, "user");
		given(orderRepository.findById(order.getOrderId())).willReturn(Optional.of(order));
		assertThrows(ResponseStatusException.class, () -> orderService.updateDestination(order.getOrderId(), request));
		verify(orderRepository, times(1)).findById(order.getOrderId());
	}

	@Test
	void testUpdateDestination_whenOrderCompleted_shouldThrowException() {
		OrderDestinationUpdateDto request = new OrderDestinationUpdateDto("New destination");
		Order order = generateOrder(OrderStatus.COMPLETED, 1L, "user");
		given(orderRepository.findById(order.getOrderId())).willReturn(Optional.of(order));
		assertThrows(ResponseStatusException.class, () -> orderService.updateDestination(order.getOrderId(), request));
		verify(orderRepository, times(1)).findById(order.getOrderId());
	}

	@Test
	void testOrderCancel_shouldCancel() {
		Order order = generateOrder(OrderStatus.CREATED, 2L, "user");

		given(orderRepository.findById(order.getOrderId())).willReturn(Optional.of(order));
		given(kafkaTemplate.send(anyString(), any(OrderMessage.class))).willReturn(null);

		orderService.cancelOrder(order.getOrderId());

		verify(kafkaTemplate, times(1)).send(anyString(), any(UUID.class));
		verify(orderRepository, times(1)).findById(order.getOrderId());
	}

	@Test
	void testOrderCancel_shouldNotFoundTest() {
		UUID orderId = UUID.randomUUID();
		given(orderRepository.findById(orderId)).willReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> orderService.cancelOrder(orderId));
		verify(orderRepository, times(1)).findById(orderId);
	}

	@Test
	void testOrderCancel_whenOrderCancelled_shouldThrowException() {
		Order order = generateOrder(OrderStatus.CANCELLED, 1L, "user");
		given(orderRepository.findById(order.getOrderId())).willReturn(Optional.of(order));
		assertThrows(ResponseStatusException.class, () -> orderService.cancelOrder(order.getOrderId()));
		verify(orderRepository, times(1)).findById(order.getOrderId());
	}

	@Test
	void testOrderCancel_whenOrderCompleted_shouldThrowException() {
		Order order = generateOrder(OrderStatus.COMPLETED, 1L, "user");
		given(orderRepository.findById(order.getOrderId())).willReturn(Optional.of(order));
		assertThrows(ResponseStatusException.class, () -> orderService.cancelOrder(order.getOrderId()));
		verify(orderRepository, times(1)).findById(order.getOrderId());
	}
}