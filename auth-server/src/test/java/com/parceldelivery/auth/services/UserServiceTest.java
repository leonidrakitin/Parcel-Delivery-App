package com.parceldelivery.auth.services;

import com.parceldelivery.auth.models.User;
import com.parceldelivery.auth.repository.RoleRepository;
import com.parceldelivery.auth.repository.UserRepository;
import com.parceldelivery.shared.model.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static com.parceldelivery.auth.util.AuthTestUtil.generateUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(locations = "/application.yml")
class UserServiceTest {

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private RoleRepository roleRepository;

	@MockBean
	private RefreshTokenService refreshTokenService;

	@Autowired
	private UserService userService;

	@Test
	void testGetUser_thenOk() {
		User user = generateUser();
		given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

		UserDto result = userService.getUser(user.getId());

		assertNotNull(result);
		assertEquals(user.getId(), result.id());
		assertEquals(user.getUsername(), result.username());
		assertEquals(user.getEmail(), result.email());
		assertEquals(user.getRole().getName(), result.role());

		verify(userRepository, times(1)).findById(any());
	}

	@Test
	void testGetUser_thenNotFoundException() {
		Long userId = 1L;
		given(userRepository.findById(userId)).willReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> userService.getUser(userId));
		verify(userRepository, times(1)).findById(any());
	}
}