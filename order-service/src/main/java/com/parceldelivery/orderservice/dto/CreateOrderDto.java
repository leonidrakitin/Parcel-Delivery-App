package com.parceldelivery.orderservice.dto;

import lombok.Builder;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
public record CreateOrderDto(@NotBlank String description,
                             @NotBlank String destination,
                             @NotNull @Min(1) Double price) {
}
