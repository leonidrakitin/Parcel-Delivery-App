package com.parceldelivery.deliveryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parceldelivery.deliveryservice.dto.CoordinatesDto;
import com.parceldelivery.deliveryservice.dto.DeliveryDto;
import com.parceldelivery.deliveryservice.dto.DeliveryStatusDto;
import com.parceldelivery.deliveryservice.model.DeliveryStatus;
import com.parceldelivery.deliveryservice.service.DeliveryService;
import com.parceldelivery.shared.test.annotation.MockAdminDetails;
import com.parceldelivery.shared.test.annotation.MockCourierDetails;
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

import static com.parceldelivery.deliveryservice.util.DeliveryTestUtil.generateCoordinatesDto;
import static com.parceldelivery.deliveryservice.util.DeliveryTestUtil.generateDeliveryDto;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(DeliveryController.class)
@TestPropertySource(locations = "/application.yml")
class DeliveryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DeliveryService deliveryService;

	@Test
	@MockAdminDetails
	void testGetDeliveries_whenAdmin_returnsListOfDeliveries() throws Exception {
		List<DeliveryDto> deliveries = Arrays.asList(
				generateDeliveryDto("courier1", DeliveryStatus.OUT_FOR_DELIVERY),
				generateDeliveryDto("courier2", DeliveryStatus.ASSIGNED)
		);

		given(deliveryService.getDeliveries()).willReturn(deliveries);

		mockMvc.perform(get("/v1/deliveries"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].orderId", is(deliveries.get(0).orderId().toString())))
				.andExpect(jsonPath("$[0].courier", is(deliveries.get(0).courier())))
				.andExpect(jsonPath("$[0].destination", is(deliveries.get(0).destination())))
				.andExpect(jsonPath("$[0].status", is(deliveries.get(0).status().toString())))
				.andExpect(jsonPath("$[1].orderId", is(deliveries.get(1).orderId().toString())))
				.andExpect(jsonPath("$[1].courier", is(deliveries.get(1).courier())))
				.andExpect(jsonPath("$[1].destination", is(deliveries.get(1).destination())))
				.andExpect(jsonPath("$[1].status", is(deliveries.get(1).status().toString())));

		verify(deliveryService, times(1)).getDeliveries();
	}

	@Test
	@MockCourierDetails
	void testGetDeliveries_whenCourier_returnsListOfDeliveries() throws Exception {
		List<DeliveryDto> deliveries = Arrays.asList(
				generateDeliveryDto("courier", DeliveryStatus.OUT_FOR_DELIVERY),
				generateDeliveryDto("courier", DeliveryStatus.ASSIGNED)
		);

		given(deliveryService.getDeliveries()).willReturn(deliveries);

		mockMvc.perform(get("/v1/deliveries"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].orderId", is(deliveries.get(0).orderId().toString())))
				.andExpect(jsonPath("$[0].courier", is(deliveries.get(0).courier())))
				.andExpect(jsonPath("$[0].destination", is(deliveries.get(0).destination())))
				.andExpect(jsonPath("$[0].status", is(deliveries.get(0).status().toString())))
				.andExpect(jsonPath("$[1].orderId", is(deliveries.get(1).orderId().toString())))
				.andExpect(jsonPath("$[1].courier", is(deliveries.get(1).courier())))
				.andExpect(jsonPath("$[1].destination", is(deliveries.get(1).destination())))
				.andExpect(jsonPath("$[1].status", is(deliveries.get(1).status().toString())));

		verify(deliveryService, times(1)).getDeliveries();
	}

	@Test
	@MockCourierDetails
	void testGetDeliveries_whenCourier_returnsEmptyList() throws Exception {
		List<DeliveryDto> deliveries = List.of();
		given(deliveryService.getDeliveries()).willReturn(deliveries);
		mockMvc.perform(get("/v1/deliveries"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		verify(deliveryService, times(1)).getDeliveries();
	}

	@Test
	@MockCourierDetails
	void testGetDeliveryDetails_returnDelivery() throws Exception {
		DeliveryDto deliveryDto = generateDeliveryDto("courier", DeliveryStatus.OUT_FOR_DELIVERY);

		given(deliveryService.getDeliveryDetails(deliveryDto.orderId())).willReturn(deliveryDto);

		mockMvc.perform(get("/v1/deliveries/{orderId}", deliveryDto.orderId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId", is(deliveryDto.orderId().toString())))
				.andExpect(jsonPath("$.courier", is(deliveryDto.courier())))
				.andExpect(jsonPath("$.destination", is(deliveryDto.destination())))
				.andExpect(jsonPath("$.status", is(deliveryDto.status().toString())));

		verify(deliveryService, times(1)).getDeliveryDetails(deliveryDto.orderId());
	}

	@Test
	@MockCourierDetails
	void testGetDeliveryDetails_throwException() throws Exception {
		DeliveryDto deliveryDto = generateDeliveryDto("courier", DeliveryStatus.OUT_FOR_DELIVERY);

		given(deliveryService.getDeliveryDetails(deliveryDto.orderId()))
				.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		mockMvc.perform(get("/v1/deliveries/{orderId}", deliveryDto.orderId()))
				.andExpect(status().isNotFound());

		verify(deliveryService, times(1)).getDeliveryDetails(deliveryDto.orderId());
	}

	@Test
	@MockCourierDetails
	void testEditStatus_withValidOrderIdAndDto_returnsNoContent() throws Exception {
		DeliveryDto deliveryDto = generateDeliveryDto("courier", DeliveryStatus.OUT_FOR_DELIVERY);
		DeliveryStatusDto deliveryStatusDto = new DeliveryStatusDto(DeliveryStatus.PICKED_UP);

		doNothing().when(deliveryService).editStatus(deliveryDto.orderId(), deliveryStatusDto);

		mockMvc.perform(patch("/v1/deliveries/{orderId}/edit/status", deliveryDto.orderId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(deliveryStatusDto)))
				.andExpect(status().isNoContent());

		verify(deliveryService, times(1)).editStatus(deliveryDto.orderId(), deliveryStatusDto);
	}

	@Test
	@MockAdminDetails
	void testGetDeliveryCoordinates_returnsCoordinates() throws Exception {
		CoordinatesDto coordinatesDto = generateCoordinatesDto(UUID.randomUUID());

		given(deliveryService.getDeliveryCoordinates(coordinatesDto.orderId())).willReturn(coordinatesDto);

		mockMvc.perform(get("/v1/deliveries/{orderId}/coordinates", coordinatesDto.orderId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId", is(coordinatesDto.orderId().toString())))
				.andExpect(jsonPath("$.latitude", is(coordinatesDto.latitude())))
				.andExpect(jsonPath("$.longitude", is(coordinatesDto.longitude())));

		verify(deliveryService, times(1)).getDeliveryCoordinates(coordinatesDto.orderId());
	}
}