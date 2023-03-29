package com.parceldelivery.shared.kafka;

import com.parceldelivery.shared.model.OrderStatus;

import java.util.UUID;

public record OrderChangeStatusMessage(UUID orderId, OrderStatus status) {
}
