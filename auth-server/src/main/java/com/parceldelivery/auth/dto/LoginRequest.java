package com.parceldelivery.auth.dto;

import javax.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String username, @NotBlank String password) {
}
