package com.parceldelivery.auth.services;

import com.parceldelivery.auth.models.RefreshToken;
import com.parceldelivery.auth.repository.RefreshTokenRepository;
import com.parceldelivery.shared.security.jwt.ConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final ConfigProperties properties;
  private final RefreshTokenRepository refreshTokenRepository;

  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }

  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
      refreshTokenRepository.delete(token);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token was expired.");
    }
    return token;
  }

	@Transactional
	public RefreshToken createRefreshToken(Long userId) {
		RefreshToken refreshToken = RefreshToken.builder()
				.userId(userId)
				.token(UUID.randomUUID().toString())
				.expiryDate(Instant.now().plusMillis(properties.getRefreshTokenDurationMs()))
				.build();
		refreshToken = refreshTokenRepository.save(refreshToken);
		return refreshToken;
	}
}
