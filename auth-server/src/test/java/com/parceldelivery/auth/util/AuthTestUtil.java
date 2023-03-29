package com.parceldelivery.auth.util;

import com.parceldelivery.auth.models.Role;
import com.parceldelivery.auth.models.User;
import com.parceldelivery.shared.model.RoleType;
import com.parceldelivery.shared.model.UserDto;
import com.parceldelivery.shared.security.jwt.UserDetailsImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Random;

public class AuthTestUtil {
	public static UserDto generateUserDto(RoleType role) {
		return UserDto.builder()
					.id(new Random().nextLong())
					.username("username")
					.email("email@email.org")
					.role(role)
				.build();
	}
	public static UserDto generateUserDto(Long userId, RoleType role) {
		return UserDto.builder()
					.id(userId)
					.username("username")
					.email("email@email.org")
					.role(role)
				.build();
	}

	public static User generateUser() {
		return User.builder()
					.id(2L)
					.username("user")
					.password("pass")
					.email("email@email.org")
					.role(new Role(1, RoleType.ROLE_USER))
				.build();
	}

	public static User generateUser(String username, RoleType role) {
		return User.builder()
				.id(3L)
				.username(username)
				.password("testpassword")
				.email("test@email.org")
				.role(new Role(1, role))
				.build();
	}

	public static UserDetailsImpl generateUserDetails(String username, String password) {
		return UserDetailsImpl.builder()
				.id(2L)
				.username(username)
				.password(password)
				.email("email@email.org")
				.authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
				.build();
	}
}
