package com.parceldelivery.deliveryservice.service.client;

import com.parceldelivery.shared.model.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.validation.constraints.NotNull;

@FeignClient(value = "auth-server")
public interface AuthFeignClient {
	@GetMapping("/v1/users/{userId}")
	ResponseEntity<UserDto> getUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String token, @NotNull @PathVariable Long userId);
}
