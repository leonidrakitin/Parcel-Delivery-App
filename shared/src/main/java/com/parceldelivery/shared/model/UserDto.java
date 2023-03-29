package com.parceldelivery.shared.model;

import lombok.Builder;

@Builder
public record UserDto(Long id, String username, String email, RoleType role) {
}
