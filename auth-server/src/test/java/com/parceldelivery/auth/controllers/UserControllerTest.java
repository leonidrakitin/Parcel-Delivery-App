package com.parceldelivery.auth.controllers;

import com.parceldelivery.auth.services.UserService;
import com.parceldelivery.shared.model.RoleType;
import com.parceldelivery.shared.model.UserDto;
import com.parceldelivery.shared.security.jwt.UserDetailsImpl;
import com.parceldelivery.shared.test.annotation.MockAdminDetails;
import com.parceldelivery.shared.test.annotation.MockUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.parceldelivery.auth.util.AuthTestUtil.generateUserDto;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(UserController.class)
@TestPropertySource(locations = "/application.yml")
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@Test
	@MockAdminDetails
	void getUser() throws Exception {
		UserDto userDto = generateUserDto(1L, RoleType.ROLE_COURIER);

		given(userService.getUser(userDto.id())).willReturn(userDto);

		mockMvc.perform(get("/v1/users/{userId}", userDto.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is(userDto.username())))
				.andExpect(jsonPath("$.email", is(userDto.email())))
				.andExpect(jsonPath("$.role", is(userDto.role().toString())));

		verify(userService, times(1)).getUser(userDto.id());
	}

	@Test
	@MockUserDetails
	void getAuthUserInfo_ifUser() throws Exception {
		UserDetailsImpl principal = UserDetailsImpl.builder()
					.id(2L)
					.email("user@company.com")
					.username("user")
					.password("p@ssword")
					.authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
				.build();

		mockMvc.perform(get("/v1/users/me"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email", is(principal.getEmail())))
				.andExpect(jsonPath("$.username", is(principal.getUsername())))
				.andExpect(jsonPath("$.authorities", hasSize(1)))
				.andExpect(jsonPath("$.authorities[0].authority",
						is(principal.getAuthorities().iterator().next().toString())));
	}
}