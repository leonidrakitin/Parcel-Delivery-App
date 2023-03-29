package com.parceldelivery.auth.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.parceldelivery.shared.security.jwt.CustomClaims;
import com.parceldelivery.shared.security.jwt.ConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtGenerator {

	private final ConfigProperties properties;

	public String generateJwtToken(Long userId, String username, String role) {
		return JWT.create()
				.withClaim(CustomClaims.ROLES, role)
				.withClaim(CustomClaims.USER_ID, userId)
				.withSubject(username)
				.withIssuedAt(new Date())
				.withExpiresAt(new Date(new Date().getTime() + properties.getJwtExpirationMs()))
				.sign(Algorithm.HMAC512(properties.getJwtSecret()));
	}
}
