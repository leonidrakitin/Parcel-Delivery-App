package com.parceldelivery.auth.services;

import com.parceldelivery.auth.dto.LoginResponse;
import com.parceldelivery.shared.model.UserDto;
import com.parceldelivery.auth.dto.LoginRequest;
import com.parceldelivery.auth.dto.RegisterRequest;
import com.parceldelivery.auth.dto.TokenRefreshRequest;
import com.parceldelivery.auth.dto.TokenRefreshResponse;

public interface AuthService {
	LoginResponse authenticateUser(LoginRequest loginRequest);
	UserDto registerUser(RegisterRequest signUpRequest);
	UserDto registerCourier(RegisterRequest signUpRequest);
	TokenRefreshResponse refreshToken(TokenRefreshRequest request);
}
