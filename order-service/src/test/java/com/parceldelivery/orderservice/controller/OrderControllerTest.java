package com.parceldelivery.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parceldelivery.orderservice.dto.CreateOrderDto;
import com.parceldelivery.orderservice.dto.OrderDestinationUpdateDto;
import com.parceldelivery.orderservice.dto.OrderDto;
import com.parceldelivery.orderservice.service.OrderService;
import com.parceldelivery.shared.model.OrderStatus;
import com.parceldelivery.shared.test.annotation.MockAdminDetails;
import com.parceldelivery.shared.test.annotation.MockUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.parceldelivery.orderservice.util.OrderTestUtil.generateCreateOrderDto;
import static com.parceldelivery.orderservice.util.OrderTestUtil.generateOrderDto;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(OrderController.class)
@TestPropertySource(locations = "/application.yml")
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OrderService orderService;

	@Test
	@MockAdminDetails
	void testGetAllOrders_whenAdmin_shouldReturnListOfOrderDto() throws Exception {
		List<OrderDto> orderDtoList = Arrays.asList(
				generateOrderDto(OrderStatus.CREATED, "user"),
				generateOrderDto(OrderStatus.CANCELLED, "user2")
		);

		given(orderService.getAllOrders(1000, 0)).willReturn(orderDtoList);

		mockMvc.perform(get("/v1/orders"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].createdBy", is(orderDtoList.get(0).createdBy())))
				.andExpect(jsonPath("$[0].description", is(orderDtoList.get(0).description())))
				.andExpect(jsonPath("$[0].destination", is(orderDtoList.get(0).destination())))
				.andExpect(jsonPath("$[0].price", is(orderDtoList.get(0).price())))
				.andExpect(jsonPath("$[0].status", is(orderDtoList.get(0).status().toString())))
				.andExpect(jsonPath("$[1].createdBy", is(orderDtoList.get(1).createdBy())))
				.andExpect(jsonPath("$[1].description", is(orderDtoList.get(1).description())))
				.andExpect(jsonPath("$[1].destination", is(orderDtoList.get(1).destination())))
				.andExpect(jsonPath("$[1].price", is(orderDtoList.get(1).price())))
				.andExpect(jsonPath("$[1].status", is(orderDtoList.get(1).status().toString())));

		verify(orderService, times(1)).getAllOrders(1000, 0);
	}

	@Test
	@MockUserDetails
	void testGetAllOrders_whenUser_shouldReturnListOfOrderDto() throws Exception {
		List<OrderDto> orderDtoList = Arrays.asList(
				generateOrderDto(OrderStatus.CREATED, "user"),
				generateOrderDto(OrderStatus.CANCELLED, "user")
		);

		given(orderService.getAllOrders(1000, 0)).willReturn(orderDtoList);

		mockMvc.perform(get("/v1/orders"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].createdBy", is(orderDtoList.get(0).createdBy())))
				.andExpect(jsonPath("$[0].description", is(orderDtoList.get(0).description())))
				.andExpect(jsonPath("$[0].destination", is(orderDtoList.get(0).destination())))
				.andExpect(jsonPath("$[0].price", is(orderDtoList.get(0).price())))
				.andExpect(jsonPath("$[0].status", is(orderDtoList.get(0).status().toString())))
				.andExpect(jsonPath("$[1].createdBy", is(orderDtoList.get(1).createdBy())))
				.andExpect(jsonPath("$[1].description", is(orderDtoList.get(1).description())))
				.andExpect(jsonPath("$[1].destination", is(orderDtoList.get(1).destination())))
				.andExpect(jsonPath("$[1].price", is(orderDtoList.get(1).price())))
				.andExpect(jsonPath("$[1].status", is(orderDtoList.get(1).status().toString())));

		verify(orderService, times(1)).getAllOrders(1000, 0);
	}

	@Test
	@MockUserDetails
	void testGetAllOrders_whenUser_shouldReturnEmptyList() throws Exception {
		List<OrderDto> orderDtoList = List.of();
		given(orderService.getAllOrders(1000, 0)).willReturn(orderDtoList);
		mockMvc.perform(get("/v1/orders"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		verify(orderService, times(1)).getAllOrders(1000, 0);
	}

	@Test
	@MockAdminDetails
	void testGetOrder_whenAdmin_shouldReturnOrder() throws Exception {
		OrderDto orderDto = generateOrderDto(OrderStatus.DELIVERING, "user");

		given(orderService.getOrder(orderDto.orderId())).willReturn(orderDto);

		mockMvc.perform(get("/v1/orders/{orderId}", orderDto.orderId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.createdBy", is(orderDto.createdBy())))
				.andExpect(jsonPath("$.description", is(orderDto.description())))
				.andExpect(jsonPath("$.destination", is(orderDto.destination())))
				.andExpect(jsonPath("$.price", is(orderDto.price())))
				.andExpect(jsonPath("$.status", is(orderDto.status().toString())));

		verify(orderService, times(1)).getOrder(orderDto.orderId());
	}

	@Test
	@MockUserDetails
	void testGetOrder_whenUser_shouldReturnOrder() throws Exception {
		OrderDto orderDto = generateOrderDto(OrderStatus.DELIVERING, "user");

		given(orderService.getOrder(orderDto.orderId())).willReturn(orderDto);

		mockMvc.perform(get("/v1/orders/{orderId}", orderDto.orderId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.createdBy", is(orderDto.createdBy())))
				.andExpect(jsonPath("$.description", is(orderDto.description())))
				.andExpect(jsonPath("$.destination", is(orderDto.destination())))
				.andExpect(jsonPath("$.price", is(orderDto.price())))
				.andExpect(jsonPath("$.status", is(orderDto.status().toString())));

		verify(orderService, times(1)).getOrder(orderDto.orderId());
	}

	@Test
	@MockUserDetails
	void testGetOrder_whenUserHaveNoOrders_shouldNotFound() throws Exception {
		OrderDto orderDto = generateOrderDto(OrderStatus.DELIVERING, "user");

		given(orderService.getOrder(orderDto.orderId()))
				.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		mockMvc.perform(get("/v1/orders/{orderId}", orderDto.orderId()))
				.andExpect(status().isNotFound());

		verify(orderService, times(1)).getOrder(orderDto.orderId());
	}

	@Test
	@MockUserDetails
	void testGetOrder_whenGetAnotherUserOrder_shouldNotFound() throws Exception {
		OrderDto orderDto = generateOrderDto(OrderStatus.DELIVERING, "anotherUser");

		given(orderService.getOrder(orderDto.orderId()))
				.willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

		mockMvc.perform(get("/v1/orders/{orderId}", orderDto.orderId()))
				.andExpect(status().isBadRequest());

		verify(orderService, times(1)).getOrder(orderDto.orderId());
	}

	@Test
	@MockUserDetails
	void testCreateOrder_shouldReturnCreatedOrderDto() throws Exception {
		CreateOrderDto orderRequest = generateCreateOrderDto();
		OrderDto orderDto = generateOrderDto(OrderStatus.ASSIGNED, "user");

		given(orderService.createOrder(orderRequest)).willReturn(orderDto);

		mockMvc.perform(post("/v1/orders")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(orderRequest)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/v1/orders/")))
				.andExpect(jsonPath("$.createdBy", is(orderDto.createdBy())))
				.andExpect(jsonPath("$.description", is(orderDto.description())))
				.andExpect(jsonPath("$.destination", is(orderDto.destination())))
				.andExpect(jsonPath("$.price", is(orderDto.price())))
				.andExpect(jsonPath("$.status", is(orderDto.status().toString())));

		verify(orderService, times(1)).createOrder(orderRequest);
	}

	@Test
	@MockUserDetails
	void testUpdateDestination_whenUser_shouldReturnNoContent() throws Exception {
		OrderDto orderDto = generateOrderDto(OrderStatus.ASSIGNED, "user");
		OrderDestinationUpdateDto request = new OrderDestinationUpdateDto("new_destination");

		doNothing().when(orderService).updateDestination(orderDto.orderId(), request);

		mockMvc.perform(patch("/v1/orders/{orderId}/edit/destination", orderDto.orderId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(request)))
				.andExpect(status().isNoContent());

		verify(orderService, times(1)).updateDestination(orderDto.orderId(), request);
	}

	@Test
	@MockAdminDetails
	void testUpdateDestination_whenAdmin_shouldReturnNoContent() throws Exception {
		OrderDto orderDto = generateOrderDto(OrderStatus.ASSIGNED, "user");
		OrderDestinationUpdateDto request = new OrderDestinationUpdateDto("new_destination");

		doNothing().when(orderService).updateDestination(orderDto.orderId(), request);

		mockMvc.perform(patch("/v1/orders/{orderId}/edit/destination", orderDto.orderId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(request)))
				.andExpect(status().isNoContent());

		verify(orderService, times(1)).updateDestination(orderDto.orderId(), request);
	}

	@Test
	@MockUserDetails
	void testCancelOrder_shouldReturnNoContent() throws Exception {
		UUID orderId = UUID.randomUUID();

		doNothing().when(orderService).cancelOrder(orderId);

		mockMvc.perform(delete("/v1/orders/{orderId}/cancel", orderId).with(csrf()))
				.andExpect(status().isNoContent());

		verify(orderService, times(1)).cancelOrder(orderId);
	}
}