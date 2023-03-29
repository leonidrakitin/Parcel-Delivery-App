package com.parceldelivery.shared.kafka;

import lombok.NonNull;

import java.util.UUID;

public record OrderMessage(@NonNull UUID orderId, @NonNull String destination) {
}
