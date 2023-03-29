package com.parceldelivery.auth.services;

import com.parceldelivery.auth.models.User;
import com.parceldelivery.auth.repository.UserRepository;
import com.parceldelivery.shared.model.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	@Transactional
	public UserDto getUser(Long userId) {
		return userRepository
				.findById(userId)
				.map(this::mapToUserDto)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found by provided user ID"));
	}

	private UserDto mapToUserDto(User user) {
		return UserDto.builder()
				.id(user.getId())
				.username(user.getUsername())
				.email(user.getEmail())
				.role(user.getRole().getName())
				.build();
	}
}
