package com.parceldelivery.deliveryservice.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CoordinatesDto(UUID orderId, Double latitude, Double longitude) {
}
