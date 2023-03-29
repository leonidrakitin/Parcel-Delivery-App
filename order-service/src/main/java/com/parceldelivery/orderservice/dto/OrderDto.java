package com.parceldelivery.orderservice.dto;

import com.parceldelivery.shared.model.OrderStatus;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
public record OrderDto(UUID orderId, String description, String destination,
                       OrderStatus status, Double price, ZonedDateTime createdTime,
                       String createdBy) {
}
