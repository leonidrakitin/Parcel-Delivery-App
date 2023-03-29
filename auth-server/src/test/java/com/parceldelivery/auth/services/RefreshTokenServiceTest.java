package com.parceldelivery.auth.services;

import com.parceldelivery.auth.models.RefreshToken;
import com.parceldelivery.auth.repository.RefreshTokenRepository;
import com.parceldelivery.auth.repository.RoleRepository;
import com.parceldelivery.auth.repository.UserRepository;
import com.parceldelivery.shared.security.jwt.ConfigProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(locations = "/application.yml")
class RefreshTokenServiceTest {

	@MockBean
	private ConfigProperties properties;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private RoleRepository roleRepository;

	@MockBean
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private RefreshTokenService service;

	@Test
	void testFindByToken_ifPresent_thenOk() {
		RefreshToken token = new RefreshToken(1L, 2L, "token", Instant.now());
		given(refreshTokenRepository.findByToken(token.getToken())).willReturn(Optional.of(token));
		var result = service.findByToken(token.getToken());
		assertTrue(result.isPresent());
		verify(refreshTokenRepository, times(1)).findByToken(any());
	}

	@Test
	void testFindByToken_ifEmpty_thenOk() {
		RefreshToken token = new RefreshToken(1L, 2L, "token", Instant.now());
		given(refreshTokenRepository.findByToken(token.getToken())).willReturn(Optional.empty());
		var result = service.findByToken(token.getToken());
		assertTrue(result.isEmpty());
		verify(refreshTokenRepository, times(1)).findByToken(any());
	}

	@Test
	void testVerifyExpiration_isExpired() {
		RefreshToken token = new RefreshToken(1L, 2L, "token", Instant.MIN);
		doNothing().when(refreshTokenRepository).delete(token);
		assertThrows(ResponseStatusException.class, () -> service.verifyExpiration(token));
		verify(refreshTokenRepository, times(1)).delete(any());
	}

	@Test
	void testVerifyExpiration_isNotExpired() {
		RefreshToken token = new RefreshToken(1L, 2L, "token", Instant.MAX);
		RefreshToken verifiedToken = service.verifyExpiration(token);
		assertNotNull(verifiedToken);
		verify(refreshTokenRepository, never()).delete(any());
	}

	@Test
	void testCreateRefreshToken() {
		RefreshToken token = new RefreshToken(1L, 2L, "token", Instant.MIN);
		given(refreshTokenRepository.save(any())).willReturn(token);
		assertNotNull(service.createRefreshToken(token.getUserId()));
		verify(refreshTokenRepository, times(1)).save(any());
	}
}