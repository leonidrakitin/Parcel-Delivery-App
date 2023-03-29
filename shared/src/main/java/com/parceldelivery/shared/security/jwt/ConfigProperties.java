package com.parceldelivery.shared.security.jwt;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ConfigProperties {
	@Value("${spring.security.jwt.secretKey}")
	private String jwtSecret;

	@Value("${spring.security.jwt.expirationMs}")
	private int jwtExpirationMs;

	@Value("${spring.security.jwt.refreshExpirationMs}")
	private Long refreshTokenDurationMs;
}
