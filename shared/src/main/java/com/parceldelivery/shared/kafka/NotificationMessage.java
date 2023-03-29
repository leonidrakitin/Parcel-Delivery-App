package com.parceldelivery.shared.kafka;

import java.util.UUID;

public record NotificationMessage(UUID orderId) {
}
