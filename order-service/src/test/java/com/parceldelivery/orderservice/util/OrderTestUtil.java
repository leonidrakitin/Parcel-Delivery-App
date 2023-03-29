package com.parceldelivery.orderservice.util;

import com.parceldelivery.orderservice.dto.CreateOrderDto;
import com.parceldelivery.orderservice.dto.OrderDto;
import com.parceldelivery.orderservice.model.Order;
import com.parceldelivery.shared.model.OrderStatus;
import lombok.experimental.UtilityClass;

import java.time.ZonedDateTime;
import java.util.UUID;

@UtilityClass
public class OrderTestUtil {
	public static Order generateOrder(OrderStatus status, Long createdId, String username) {
		return Order.builder()
				.orderId(UUID.randomUUID())
				.destination("destination")
				.description("description")
				.price(10D)
				.status(status)
				.createdTime(ZonedDateTime.now())
				.createdById(createdId)
				.createdByName(username)
				.build();
	}

	public static OrderDto generateOrderDto(OrderStatus status, String username) {
		return OrderDto.builder()
				.orderId(UUID.randomUUID())
				.description("description")
				.destination("destination")
				.status(status)
				.price(10D)
				.createdBy(username)
				.createdTime(ZonedDateTime.now())
				.build();
	}

	public static CreateOrderDto generateCreateOrderDto() {
		return CreateOrderDto.builder()
					.description("description")
					.destination("destination")
					.price(10D)
				.build();
	}
}
