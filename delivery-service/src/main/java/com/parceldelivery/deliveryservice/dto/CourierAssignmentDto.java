package com.parceldelivery.deliveryservice.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public record CourierAssignmentDto(@NotNull UUID orderId, @NotNull Long courierId) {
}
