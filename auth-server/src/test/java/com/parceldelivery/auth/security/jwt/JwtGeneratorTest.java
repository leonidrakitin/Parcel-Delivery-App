package com.parceldelivery.auth.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.parceldelivery.shared.security.jwt.CustomClaims;
import com.parceldelivery.shared.security.jwt.ConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class JwtGeneratorTest {

	private ConfigProperties configProperties;
	private JwtGenerator jwtGenerator;

	@BeforeEach
	public void before()
	{
		configProperties = mock(ConfigProperties.class);
		jwtGenerator = new JwtGenerator(configProperties);
	}

	@Test
	void testGenerateJwtToken() {
		long userId = 1L;
		String user = "user";
		String role = "ROLE_USER";

		given(configProperties.getJwtSecret()).willReturn("SECRET-KEY");
		given(configProperties.getJwtExpirationMs()).willReturn(1000);

		String token = jwtGenerator.generateJwtToken(userId, user, role);
		DecodedJWT decodedJWT = JWT.decode(token);

		assertEquals(decodedJWT.getSubject(), user);
		assertEquals(decodedJWT.getClaim(CustomClaims.USER_ID).asLong(), userId);
		assertEquals(decodedJWT.getClaim(CustomClaims.ROLES).asString(), role);
	}
}