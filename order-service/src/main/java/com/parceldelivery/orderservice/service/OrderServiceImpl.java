package com.parceldelivery.orderservice.service;

import com.parceldelivery.orderservice.dto.CreateOrderDto;
import com.parceldelivery.orderservice.dto.OrderDestinationUpdateDto;
import com.parceldelivery.orderservice.dto.OrderDto;
import com.parceldelivery.orderservice.model.Order;
import com.parceldelivery.orderservice.repository.OrderRepository;
import com.parceldelivery.shared.kafka.config.KafkaTopicConfig;
import com.parceldelivery.shared.kafka.OrderMessage;
import com.parceldelivery.shared.model.OrderStatus;
import com.parceldelivery.shared.security.jwt.UserDetailsImpl;
import com.parceldelivery.shared.security.jwt.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
	private final OrderRepository orderRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final KafkaTopicConfig kafkaTopicConfig;

	public List<OrderDto> getAllOrders(Integer limit, Integer page) {
		UserDetailsImpl principal = AuthUtil.getPrincipal();
		Stream<Order> results = AuthUtil.isAdmin()
				? orderRepository.findAll(PageRequest.of(page, limit)).stream()
				: orderRepository.findAllByCreatedById(principal.getId()).stream();
		return results.map(this::mapOrderToDto).toList();
	}

	public OrderDto getOrder(UUID orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found order " + orderId));

		if(!AuthUtil.isAdmin()) {
			UserDetailsImpl principal = AuthUtil.getPrincipal();
			if (!Objects.equals(order.getCreatedById(), principal.getId())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You don't have access to " + orderId + " order's details.");
			}
		}

		return mapOrderToDto(order);
	}

	@Transactional
	public OrderDto createOrder(CreateOrderDto request) {
		UserDetailsImpl principal = AuthUtil.getPrincipal();
		Order order = Order.builder()
					.description(request.description())
					.price(request.price())
					.destination(request.destination())
					.status(OrderStatus.CREATED)
					.createdTime(ZonedDateTime.now())
					.createdByName(principal.getUsername())
					.createdById(principal.getId())
				.build();
		order = orderRepository.save(order);

		var message = new OrderMessage(order.getOrderId(), order.getDestination());
		kafkaTemplate.send(kafkaTopicConfig.getPutDeliveryTopic(), message);

		OrderDto orderDto = mapOrderToDto(order);
		log.info("The new order was created {}", orderDto);
		return orderDto;
	}

	@Transactional
	public void updateDestination(UUID orderId, OrderDestinationUpdateDto request) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found order " + orderId));

		if (order.getStatus().equals(OrderStatus.CANCELLED) || order.getStatus().equals(OrderStatus.COMPLETED)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "The order was cancelled or delivered.");
		}

		order.setDestination(request.newDestination());

		var message = new OrderMessage(orderId, request.newDestination());
		kafkaTemplate.send(kafkaTopicConfig.getPutDeliveryTopic(), message);
		log.info("The order {} has been updated with a new destination: {}", orderId, orderId);
	}

	@Transactional
	public void cancelOrder(UUID orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found order " + orderId));

		if (order.getStatus().equals(OrderStatus.CANCELLED) || order.getStatus().equals(OrderStatus.COMPLETED)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "The order has already been cancelled or delivered.");
		}

		order.setStatus(OrderStatus.CANCELLED);

		kafkaTemplate.send(kafkaTopicConfig.getCancelDeliveryTopic(), orderId);
		log.info("The order {} was cancelled by user.", orderId);
	}

	private OrderDto mapOrderToDto(Order order) {
		return OrderDto.builder()
					.orderId(order.getOrderId())
					.description(order.getDescription())
					.destination(order.getDestination())
					.status(order.getStatus())
					.price(order.getPrice())
					.createdTime(order.getCreatedTime())
					.createdBy(order.getCreatedByName())
				.build();
	}
}
