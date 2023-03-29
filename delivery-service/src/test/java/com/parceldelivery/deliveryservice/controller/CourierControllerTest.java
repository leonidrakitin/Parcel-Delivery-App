package com.parceldelivery.deliveryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parceldelivery.deliveryservice.dto.CourierAssignmentDto;
import com.parceldelivery.deliveryservice.dto.CourierStatusDto;
import com.parceldelivery.deliveryservice.model.DeliveryStatus;
import com.parceldelivery.deliveryservice.service.CourierService;
import com.parceldelivery.shared.test.annotation.MockAdminDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.parceldelivery.deliveryservice.util.DeliveryTestUtil.generateDeliveryDto;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(CourierController.class)
@TestPropertySource(locations = "/application.yml")
class CourierControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CourierService courierService;

	@Test
	@MockAdminDetails
	void testGetListCouriersStatus_returnsListOfDeliveries() throws Exception {
		List<CourierStatusDto> courierStatusDtos = Arrays.asList(
				new CourierStatusDto("courier1", Arrays.asList(
						generateDeliveryDto("courier1", DeliveryStatus.CANCELED),
						generateDeliveryDto("courier1", DeliveryStatus.OUT_FOR_DELIVERY)
				)),
				new CourierStatusDto("courier2", Collections.singletonList(
						generateDeliveryDto("courier2", DeliveryStatus.ASSIGNED)
				))
		);

		given(courierService.getListCouriersStatus()).willReturn(courierStatusDtos);

		mockMvc.perform(get("/v1/couriers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].courierName", is(courierStatusDtos.get(0).courierName())))
				.andExpect(jsonPath("$[0].deliveryDtoList", hasSize(2)))
				.andExpect(jsonPath("$[0].deliveryDtoList[0].courier",
						is(courierStatusDtos.get(0).deliveryDtoList().get(0).courier())))
				.andExpect(jsonPath("$[0].deliveryDtoList[0].status",
						is(courierStatusDtos.get(0).deliveryDtoList().get(0).status().toString())))
				.andExpect(jsonPath("$[0].deliveryDtoList[1].courier",
						is(courierStatusDtos.get(0).deliveryDtoList().get(1).courier())))
				.andExpect(jsonPath("$[0].deliveryDtoList[1].status",
						is(courierStatusDtos.get(0).deliveryDtoList().get(1).status().toString())))
				.andExpect(jsonPath("$[1].courierName", is(courierStatusDtos.get(1).courierName())))
				.andExpect(jsonPath("$[1].deliveryDtoList", hasSize(1)))
				.andExpect(jsonPath("$[1].deliveryDtoList[0].courier",
						is(courierStatusDtos.get(1).deliveryDtoList().get(0).courier())))
				.andExpect(jsonPath("$[1].deliveryDtoList[0].status",
						is(courierStatusDtos.get(1).deliveryDtoList().get(0).status().toString())));

		verify(courierService, times(1)).getListCouriersStatus();
	}

	@Test
	@MockAdminDetails
	void testAssignCourier_shouldReturnOk() throws Exception {
		String token = "token";
		CourierAssignmentDto dto = new CourierAssignmentDto(UUID.randomUUID(), new Random().nextLong());

		doNothing().when(courierService).assignCourier(token, dto);

		mockMvc.perform(post("/v1/couriers/assign")
						.with(csrf())
						.header(HttpHeaders.AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(dto)))
				.andExpect(status().isOk());

		verify(courierService, times(1)).assignCourier(token, dto);
	}

	@Test
	@MockAdminDetails
	void testAssignCourier_shouldReturnBadRequest() throws Exception {
		CourierAssignmentDto dto = new CourierAssignmentDto(UUID.randomUUID(), new Random().nextLong());
		String token = "token";

		doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(courierService).assignCourier(token, dto);

		mockMvc.perform(post("/v1/couriers/assign")
						.with(csrf())
						.header(HttpHeaders.AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(dto)))
				.andExpect(status().isBadRequest());

		verify(courierService, times(1)).assignCourier(token, dto);
	}
}