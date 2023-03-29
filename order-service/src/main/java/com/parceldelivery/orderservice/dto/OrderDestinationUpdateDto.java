package com.parceldelivery.orderservice.dto;

import javax.validation.constraints.NotBlank;

public record OrderDestinationUpdateDto(@NotBlank String newDestination) {
}
