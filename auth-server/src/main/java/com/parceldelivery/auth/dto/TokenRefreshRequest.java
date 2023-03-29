package com.parceldelivery.auth.dto;

import javax.validation.constraints.NotBlank;

public record TokenRefreshRequest(@NotBlank String refreshToken) {
}
