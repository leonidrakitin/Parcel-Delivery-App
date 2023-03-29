package com.parceldelivery.orderservice.service;

import com.parceldelivery.orderservice.dto.OrderDestinationUpdateDto;
import com.parceldelivery.orderservice.dto.CreateOrderDto;
import com.parceldelivery.orderservice.dto.OrderDto;

import java.util.List;
import java.util.UUID;

public interface OrderService {
	List<OrderDto> getAllOrders(Integer limit, Integer page);
	OrderDto getOrder(UUID orderId);
	OrderDto createOrder(CreateOrderDto request);
	void updateDestination(UUID orderId, OrderDestinationUpdateDto request);
	void cancelOrder(UUID orderId);
}
