package com.parceldelivery.auth.services;

import com.parceldelivery.auth.models.User;
import com.parceldelivery.auth.repository.RoleRepository;
import com.parceldelivery.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static com.parceldelivery.auth.util.AuthTestUtil.generateUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(locations = "/application.yml")
class UserDetailsServiceImplTest {

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private RoleRepository roleRepository;

	@MockBean
	private RefreshTokenService refreshTokenService;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Test
	void testLoadUserByUsername_thenOk() {
		User user = generateUser();
		given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));

		UserDetails result = userDetailsService.loadUserByUsername(user.getUsername());

		assertNotNull(result);
		assertFalse(result.getAuthorities().isEmpty());
		assertEquals(user.getUsername(), result.getUsername());
		assertEquals(user.getPassword(), result.getPassword());
		assertEquals(user.getRole().getName().toString(),
				result.getAuthorities().iterator().next().getAuthority());

		verify(userRepository, times(1)).findByUsername(any());
	}

	@Test
	void testLoadUserByUsername_thenNotFoundException() {
		String username = "user";
		given(userRepository.findByUsername(username)).willReturn(Optional.empty());
		assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(username));
		verify(userRepository, times(1)).findByUsername(any());
	}
}