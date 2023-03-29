package com.parceldelivery.deliveryservice.dto;

import com.parceldelivery.deliveryservice.model.DeliveryStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record DeliveryDto(UUID orderId, String courier, String destination, DeliveryStatus status) {
}
