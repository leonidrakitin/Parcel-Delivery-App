package com.parceldelivery.auth.controllers;

import com.parceldelivery.auth.dto.LoginResponse;
import com.parceldelivery.shared.model.UserDto;
import com.parceldelivery.auth.dto.LoginRequest;
import com.parceldelivery.auth.dto.RegisterRequest;
import com.parceldelivery.auth.dto.TokenRefreshRequest;
import com.parceldelivery.auth.dto.TokenRefreshResponse;
import com.parceldelivery.auth.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		return ResponseEntity.ok(authService.authenticateUser(loginRequest));
  }

  @PostMapping("/register")
  public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(authService.registerUser(signUpRequest));
  }

	@RolesAllowed("ROLE_ADMIN")
	@PostMapping("courier/create")
	public ResponseEntity<UserDto> registerCourier(@Valid @RequestBody RegisterRequest signUpRequest) {
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(authService.registerCourier(signUpRequest));
	}

	@PostMapping("/refresh")
  public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
    return ResponseEntity.ok(authService.refreshToken(request));
  }
}
