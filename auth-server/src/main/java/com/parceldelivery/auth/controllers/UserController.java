package com.parceldelivery.auth.controllers;

import com.parceldelivery.shared.model.UserDto;
import com.parceldelivery.auth.services.UserService;
import com.parceldelivery.shared.security.jwt.UserDetailsImpl;
import com.parceldelivery.shared.security.jwt.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService service;

	@RolesAllowed("ROLE_ADMIN")
	@GetMapping("/{userId}")
	public ResponseEntity<UserDto> getUser(@NotNull @PathVariable Long userId) {
		return ResponseEntity.ok(service.getUser(userId));
	}

	@GetMapping("/me")
	public ResponseEntity<UserDetailsImpl> getAuthUserInfo() {
		return ResponseEntity.ok(AuthUtil.getPrincipal());
	}

}
