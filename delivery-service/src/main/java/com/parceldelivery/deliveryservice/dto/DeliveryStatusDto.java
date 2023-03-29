package com.parceldelivery.deliveryservice.dto;

import com.parceldelivery.deliveryservice.model.DeliveryStatus;

import javax.validation.constraints.NotNull;

public record DeliveryStatusDto(@NotNull DeliveryStatus newStatus) {
}
