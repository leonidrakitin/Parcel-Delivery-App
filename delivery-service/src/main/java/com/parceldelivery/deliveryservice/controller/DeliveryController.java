package com.parceldelivery.deliveryservice.controller;

import com.parceldelivery.deliveryservice.dto.DeliveryDto;
import com.parceldelivery.deliveryservice.dto.DeliveryStatusDto;
import com.parceldelivery.deliveryservice.dto.CoordinatesDto;
import com.parceldelivery.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
	private final DeliveryService deliveryService;

	@RolesAllowed({"ROLE_ADMIN", "ROLE_COURIER"})
	@GetMapping
	public ResponseEntity<List<DeliveryDto>> getDeliveries() {
		return ResponseEntity.ok(deliveryService.getDeliveries());
	}

	@RolesAllowed({"ROLE_ADMIN", "ROLE_COURIER"})
	@GetMapping("/{orderId}")
	public ResponseEntity<DeliveryDto> getDeliveryDetails(@NotNull @PathVariable UUID orderId) {
		return ResponseEntity.ok(deliveryService.getDeliveryDetails(orderId));
	}

	@RolesAllowed({"ROLE_ADMIN", "ROLE_COURIER"})
	@PatchMapping("/{orderId}/edit/status")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void editStatus(@NotNull @PathVariable UUID orderId,
	                       @Valid @RequestBody DeliveryStatusDto dto) {
		deliveryService.editStatus(orderId, dto);
	}

	@RolesAllowed("ROLE_ADMIN")
	@GetMapping("/{orderId}/coordinates")
	public ResponseEntity<CoordinatesDto> getDeliveryCoordinates(@NotNull @PathVariable UUID orderId) {
		return ResponseEntity.ok(deliveryService.getDeliveryCoordinates(orderId));
	}
}
