package com.parceldelivery.auth.services;

import com.parceldelivery.auth.dto.LoginRequest;
import com.parceldelivery.auth.dto.LoginResponse;
import com.parceldelivery.auth.dto.RegisterRequest;
import com.parceldelivery.auth.dto.TokenRefreshRequest;
import com.parceldelivery.auth.dto.TokenRefreshResponse;
import com.parceldelivery.auth.models.RefreshToken;
import com.parceldelivery.auth.models.Role;
import com.parceldelivery.auth.models.User;
import com.parceldelivery.auth.repository.RoleRepository;
import com.parceldelivery.auth.repository.UserRepository;
import com.parceldelivery.auth.security.jwt.JwtGenerator;
import com.parceldelivery.shared.model.RoleType;
import com.parceldelivery.shared.model.UserDto;
import com.parceldelivery.shared.security.jwt.ConfigProperties;
import com.parceldelivery.shared.security.jwt.UserDetailsImpl;
import com.parceldelivery.shared.security.jwt.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final AuthenticationManager authenticationManager;
	private final ConfigProperties properties;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder encoder;
	private final JwtGenerator jwtGenerator;
	private final RefreshTokenService refreshTokenService;

	@Override
	public LoginResponse authenticateUser(LoginRequest loginRequest) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		String role = userDetails.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.iterator().next();
		String jwt = jwtGenerator.generateJwtToken(userDetails.getId(), userDetails.getUsername(), role);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

		return LoginResponse.builder()
				.token(jwt)
				.refreshToken(refreshToken.getToken())
				.id(userDetails.getId())
				.expiredAt(Instant.now().plusMillis(properties.getJwtExpirationMs()))
				.type("Bearer")
				.username(userDetails.getUsername())
				.email(userDetails.getEmail())
				.role(role)
				.build();
	}

	@Override
	public UserDto registerUser(RegisterRequest signUpRequest) {
		return createUser(RoleType.ROLE_USER, signUpRequest);
	}

	@Override
	public UserDto registerCourier(RegisterRequest signUpRequest) {
		return createUser(RoleType.ROLE_COURIER, signUpRequest);
	}

	@Transactional
	private UserDto createUser(RoleType roleType, RegisterRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already exists.");
		}
		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already exists!");
		}
		Role role = roleRepository.findByName(roleType)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		User user = User.builder()
				.username(signUpRequest.getUsername())
				.email(signUpRequest.getEmail())
				.password(encoder.encode(signUpRequest.getPassword()))
				.role(role)
				.build();
		user = userRepository.save(user);

		return UserDto.builder()
				.id(user.getId())
				.username(user.getUsername())
				.email(user.getEmail())
				.role(role.getName())
				.build();
	}

	@Override
	public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
		Long userId = refreshTokenService.findByToken(request.refreshToken())
				.map(refreshTokenService::verifyExpiration)
				.map(RefreshToken::getUserId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refresh token not found"));
		UserDetailsImpl userDetails = AuthUtil.getPrincipal();
		String username = userDetails.getUsername();
		String role = AuthUtil.getAuthenticationRole().toString();
		if (!Objects.equals(userDetails.getId(), userId)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong refresh token");
		}
		String newToken = jwtGenerator.generateJwtToken(userId, username, role);
		log.info("User {} (id:{}) refreshed token", username, userId);
		return new TokenRefreshResponse(newToken, request.refreshToken());
	}
}
