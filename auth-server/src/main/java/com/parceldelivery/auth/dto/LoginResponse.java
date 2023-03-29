package com.parceldelivery.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
	private Long id;
	private String username;
	private String email;
	private String role;
	private String type;
	private String token;
	private Instant expiredAt;
	private String refreshToken;
}
