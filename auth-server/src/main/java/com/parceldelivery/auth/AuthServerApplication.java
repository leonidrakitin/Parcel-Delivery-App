package com.parceldelivery.auth;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@SecurityScheme(type = SecuritySchemeType.HTTP,
		name = "bearerAuth",
		in = SecuritySchemeIn.HEADER,
		scheme = "bearer",
		bearerFormat = "JWT")
@OpenAPIDefinition(
		info = @Info(title = "Authentication Service API", version = "0.0.1-SNAPSHOT"),
		security = { @SecurityRequirement(name = "bearerAuth") })
public class AuthServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}
}
