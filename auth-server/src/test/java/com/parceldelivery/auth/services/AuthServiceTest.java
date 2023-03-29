package com.parceldelivery.auth.services;

import com.parceldelivery.auth.dto.LoginRequest;
import com.parceldelivery.auth.dto.LoginResponse;
import com.parceldelivery.auth.dto.RegisterRequest;
import com.parceldelivery.auth.dto.TokenRefreshRequest;
import com.parceldelivery.auth.dto.TokenRefreshResponse;
import com.parceldelivery.auth.models.RefreshToken;
import com.parceldelivery.auth.models.User;
import com.parceldelivery.auth.repository.RoleRepository;
import com.parceldelivery.auth.repository.UserRepository;
import com.parceldelivery.auth.security.jwt.JwtGenerator;
import com.parceldelivery.shared.model.RoleType;
import com.parceldelivery.shared.model.UserDto;
import com.parceldelivery.shared.security.jwt.ConfigProperties;
import com.parceldelivery.shared.security.jwt.UserDetailsImpl;
import com.parceldelivery.shared.test.annotation.MockUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static com.parceldelivery.auth.util.AuthTestUtil.generateUser;
import static com.parceldelivery.auth.util.AuthTestUtil.generateUserDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(locations = "/application.yml")
class AuthServiceTest {
	@MockBean
	private AuthenticationManager authenticationManager;
	@MockBean
	private UserRepository userRepository;

	@MockBean
	private RoleRepository roleRepository;

	@MockBean
	private PasswordEncoder encoder;

	@MockBean
	private JwtGenerator jwtGenerator;

	@MockBean
	private RefreshTokenService refreshTokenService;

	@Autowired
	private ConfigProperties properties;

	@Autowired
	private AuthServiceImpl authService;

	@Test
	void testAuthenticateUser_thenOk() {
		String username = "testuser";
		String password = "testpassword";
		String jwtToken = "test_token";
		LoginRequest loginRequest = new LoginRequest(username, password);
		UserDetailsImpl userDetails = generateUserDetails(username, password);
		UsernamePasswordAuthenticationToken authenticationToken =
				new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
		String role = userDetails.getAuthorities().iterator().next().getAuthority();
		RefreshToken refreshToken = new RefreshToken(1L, 2L, "token", Instant.MAX);

		given(authenticationManager.authenticate(any())).willReturn(authenticationToken);
		given(jwtGenerator.generateJwtToken(eq(userDetails.getId()), eq(username), eq(role))).willReturn(jwtToken);
		given(refreshTokenService.createRefreshToken(eq(userDetails.getId()))).willReturn(refreshToken);

		LoginResponse response = authService.authenticateUser(loginRequest);

		assertEquals(jwtToken, response.getToken());
		assertEquals(refreshToken.getToken(), response.getRefreshToken());
		assertEquals(userDetails.getId(), response.getId());
		assertEquals("Bearer", response.getType());
		assertEquals(userDetails.getUsername(), response.getUsername());
		assertEquals(userDetails.getEmail(), response.getEmail());
		assertEquals(role, response.getRole());

		verify(authenticationManager, times(1)).authenticate(any());
		verify(jwtGenerator, times(1)).generateJwtToken(eq(userDetails.getId()), eq(username), eq(role));
		verify(refreshTokenService, times(1)).createRefreshToken(eq(userDetails.getId()));
	}

	@Test
	void testAuthenticateUser_ifInvalidCredentials_thenThrowException() {
		LoginRequest loginRequest = new LoginRequest("invalid_user", "invalid_password");
		given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willThrow(new BadCredentialsException(""));
		assertThrows(BadCredentialsException.class, () -> authService.authenticateUser(loginRequest));
		verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
	}

	@Test
	void testRegisterUser_thenOk() {
		RegisterRequest registerRequest = new RegisterRequest("user", "test@email.org", "testpassword");
		User user = generateUser(registerRequest.getUsername(), RoleType.ROLE_USER);

		given(userRepository.existsByUsername(registerRequest.getUsername())).willReturn(false);
		given(userRepository.existsByEmail(registerRequest.getEmail())).willReturn(false);
		given(roleRepository.findByName(user.getRole().getName())).willReturn(Optional.of(user.getRole()));
		given(userRepository.save(any())).willReturn(user);

		UserDto userDto = authService.registerUser(registerRequest);
		assertEquals(userDto.id(), user.getId());
		assertEquals(userDto.username(), user.getUsername());
		assertEquals(userDto.email(), user.getEmail());
		assertEquals(userDto.role(), user.getRole().getName());
	}

	@Test
	void testRegisterCourier_thenOk() {
		RegisterRequest registerRequest = new RegisterRequest("courier", "test@email.org", "testpassword");
		User user = generateUser(registerRequest.getUsername(), RoleType.ROLE_COURIER);

		given(userRepository.existsByUsername(registerRequest.getUsername())).willReturn(false);
		given(userRepository.existsByEmail(registerRequest.getEmail())).willReturn(false);
		given(roleRepository.findByName(user.getRole().getName())).willReturn(Optional.of(user.getRole()));
		given(userRepository.save(any())).willReturn(user);

		UserDto userDto = authService.registerCourier(registerRequest);
		assertEquals(userDto.id(), user.getId());
		assertEquals(userDto.username(), user.getUsername());
		assertEquals(userDto.email(), user.getEmail());
		assertEquals(userDto.role(), user.getRole().getName());

		verify(userRepository, times(1)).existsByUsername(registerRequest.getUsername());
		verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
		verify(roleRepository, times(1)).findByName(user.getRole().getName());
		verify(userRepository, times(1)).save(any());
	}

	@Test
	@MockUserDetails
	void testRefreshToken_whenRefreshTokenValid_thenReturnNewAccessToken() {
		TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest("valid_refresh_token");
		Long userId = 2L;
		String username = "user";
		String newToken = "new_token";
		String role = RoleType.ROLE_USER.toString();
		RefreshToken refreshToken = new RefreshToken(1L, userId, "valid_refresh_token", Instant.now().plusSeconds(1000));

		given(refreshTokenService.findByToken(tokenRefreshRequest.refreshToken())).willReturn(Optional.of(refreshToken));
		given(refreshTokenService.verifyExpiration(refreshToken)).willReturn(refreshToken);
		given(jwtGenerator.generateJwtToken(userId, username, role)).willReturn(newToken);

		TokenRefreshResponse tokenRefreshResponse = authService.refreshToken(tokenRefreshRequest);
		assertEquals(newToken, tokenRefreshResponse.accessToken());
		assertEquals(tokenRefreshRequest.refreshToken(), tokenRefreshResponse.refreshToken());

		verify(refreshTokenService, times(1)).findByToken(tokenRefreshRequest.refreshToken());
		verify(refreshTokenService, times(1)).verifyExpiration(refreshToken);
		verify(jwtGenerator, times(1)).generateJwtToken(userId, username, role);
	}

	@Test
	@MockUserDetails
	void testRefreshToken_whenRefreshTokenInvalid_thenResponseStatusExceptionThrown() {
		TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest("invalid_refresh_token");
		given(refreshTokenService.findByToken(tokenRefreshRequest.refreshToken())).willReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> authService.refreshToken(tokenRefreshRequest));
		verify(refreshTokenService, times(1)).findByToken(tokenRefreshRequest.refreshToken());
	}

	@Test
	@MockUserDetails
	void testRefreshToken_whenUnauthorized_thenResponseStatusExceptionThrown() {
		TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest("valid_refresh_token");
		Long userId = 1000L; //Invalid userid
		RefreshToken refreshToken = new RefreshToken(1L, userId, "valid_refresh_token", Instant.now().plusSeconds(1000));

		given(refreshTokenService.findByToken(tokenRefreshRequest.refreshToken())).willReturn(Optional.of(refreshToken));
		given(refreshTokenService.verifyExpiration(refreshToken)).willReturn(refreshToken);

		assertThrows(ResponseStatusException.class, () -> authService.refreshToken(tokenRefreshRequest));
		verify(refreshTokenService, times(1)).findByToken(tokenRefreshRequest.refreshToken());
		verify(refreshTokenService, times(1)).verifyExpiration(refreshToken);
	}
}