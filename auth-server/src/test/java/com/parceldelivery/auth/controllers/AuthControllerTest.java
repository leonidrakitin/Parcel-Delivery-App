package com.parceldelivery.auth.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parceldelivery.auth.dto.LoginRequest;
import com.parceldelivery.auth.dto.LoginResponse;
import com.parceldelivery.auth.dto.RegisterRequest;
import com.parceldelivery.auth.dto.TokenRefreshRequest;
import com.parceldelivery.auth.dto.TokenRefreshResponse;
import com.parceldelivery.auth.services.AuthService;
import com.parceldelivery.shared.model.RoleType;
import com.parceldelivery.shared.model.UserDto;
import com.parceldelivery.shared.test.annotation.MockAdminDetails;
import com.parceldelivery.shared.test.annotation.MockUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static com.parceldelivery.auth.util.AuthTestUtil.generateUserDto;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(AuthController.class)
@TestPropertySource(locations = "/application.yml")
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService authService;

	@Test
	@MockUserDetails
	void testAuthenticateUser() throws Exception {
		LoginRequest request = new LoginRequest("username", "password");
		LoginResponse response = new LoginResponse(2L, "username", "email@email.org",
				"USER_ROLE", "Bearer", "token", Instant.now(), "refresh-token");

		given(authService.authenticateUser(request)).willReturn(response);

		mockMvc.perform(post("/v1/auth/login")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is(response.getUsername())))
				.andExpect(jsonPath("$.email", is(response.getEmail())))
				.andExpect(jsonPath("$.role", is(response.getRole())))
				.andExpect(jsonPath("$.type", is(response.getType())))
				.andExpect(jsonPath("$.token", is(response.getToken())))
				.andExpect(jsonPath("$.refreshToken", is(response.getRefreshToken())));
	}

	@Test
	@MockUserDetails
	void testRegisterUser() throws Exception {
		RegisterRequest request = new RegisterRequest("username", "email@email.org", "password");
		UserDto userDto = generateUserDto(RoleType.ROLE_USER);

		given(authService.registerUser(any())).willReturn(userDto);

		mockMvc.perform(post("/v1/auth/register")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(userDto.id())))
				.andExpect(jsonPath("$.username", is(userDto.username())))
				.andExpect(jsonPath("$.email", is(userDto.email())))
				.andExpect(jsonPath("$.role", is(userDto.role().toString())));
	}

	@Test
	@MockAdminDetails
	void tesRegisterCourier() throws Exception {
		RegisterRequest request = new RegisterRequest("username", "email@email.org", "password");
		UserDto userDto = generateUserDto(RoleType.ROLE_COURIER);

		given(authService.registerCourier(any())).willReturn(userDto);

		mockMvc.perform(post("/v1/auth/courier/create")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(userDto.id())))
				.andExpect(jsonPath("$.username", is(userDto.username())))
				.andExpect(jsonPath("$.email", is(userDto.email())))
				.andExpect(jsonPath("$.role", is(userDto.role().toString())));
	}

	@Test
	@MockUserDetails
	void testRefreshToken() throws Exception {
		TokenRefreshRequest request = new TokenRefreshRequest("refresh-token-old");
		TokenRefreshResponse response = new TokenRefreshResponse("access-token", "refresh-token");

		given(authService.refreshToken(any())).willReturn(response);

		mockMvc.perform(post("/v1/auth/refresh")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", is(response.accessToken())))
				.andExpect(jsonPath("$.refreshToken", is(response.refreshToken())));
	}
}