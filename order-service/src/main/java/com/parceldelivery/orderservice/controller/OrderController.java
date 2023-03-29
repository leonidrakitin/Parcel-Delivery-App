package com.parceldelivery.orderservice.controller;

import com.parceldelivery.orderservice.dto.OrderDestinationUpdateDto;
import com.parceldelivery.orderservice.dto.CreateOrderDto;
import com.parceldelivery.orderservice.dto.OrderDto;
import com.parceldelivery.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@RolesAllowed({"ROLE_ADMIN", "ROLE_USER"})
	@GetMapping
	public ResponseEntity<List<OrderDto>> getAllOrders(
			@RequestParam(value = "limit", required = false, defaultValue = "1000") Integer limit,
			@RequestParam(value = "page", required = false, defaultValue = "0") Integer page
	) {
		return ResponseEntity.ok(orderService.getAllOrders(limit, page));
	}

	@RolesAllowed({"ROLE_ADMIN", "ROLE_USER"})
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderDto> getOrder(@NotNull @PathVariable UUID orderId) {
		return ResponseEntity.ok(orderService.getOrder(orderId));
	}

	@RolesAllowed("ROLE_USER")
	@PostMapping
	public ResponseEntity<OrderDto> createOrder(HttpServletRequest request,
	                                            @Valid @RequestBody CreateOrderDto orderRequest) {
		var orderDto = orderService.createOrder(orderRequest);
		return ResponseEntity.created(createLocationURI(request, orderDto)).body(orderDto);
	}

	@RolesAllowed({"ROLE_ADMIN", "ROLE_USER"})
	@PatchMapping("/{orderId}/edit/destination")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateDestination(@NotNull @PathVariable UUID orderId,
	                              @Valid @RequestBody OrderDestinationUpdateDto request) {
		orderService.updateDestination(orderId, request);
	}

	@RolesAllowed("ROLE_USER")
	@DeleteMapping("{orderId}/cancel")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancelOrder(@NotNull @PathVariable UUID orderId) {
		orderService.cancelOrder(orderId);
	}

	private URI createLocationURI(HttpServletRequest request, OrderDto orderDto) {
		try {
			return new URI(request.getRequestURL().append("/").append(orderDto.orderId()).toString());
		} catch (URISyntaxException ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}