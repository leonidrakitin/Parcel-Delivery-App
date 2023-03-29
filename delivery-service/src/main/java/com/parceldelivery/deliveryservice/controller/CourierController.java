package com.parceldelivery.deliveryservice.controller;

import com.parceldelivery.deliveryservice.dto.CourierAssignmentDto;
import com.parceldelivery.deliveryservice.dto.CourierStatusDto;
import com.parceldelivery.deliveryservice.service.CourierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/couriers")
@RequiredArgsConstructor
public class CourierController {
	private final CourierService courierService;

	@RolesAllowed("ROLE_ADMIN")
	@GetMapping
	public List<CourierStatusDto> getListCouriersStatus() {
		return courierService.getListCouriersStatus();
	}

	@RolesAllowed("ROLE_ADMIN")
	@PostMapping("/assign")
	@ResponseStatus(HttpStatus.OK)
	public void assignCourier(@RequestHeader("Authorization") String token,
	                          @Valid @RequestBody CourierAssignmentDto dto) {
		courierService.assignCourier(token, dto);
	}
}
