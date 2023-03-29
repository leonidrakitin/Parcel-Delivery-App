package com.parceldelivery.deliveryservice.util;

import com.parceldelivery.deliveryservice.dto.CoordinatesDto;
import com.parceldelivery.deliveryservice.dto.DeliveryDto;
import com.parceldelivery.deliveryservice.model.Coordinates;
import com.parceldelivery.deliveryservice.model.Delivery;
import com.parceldelivery.deliveryservice.model.DeliveryStatus;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class DeliveryTestUtil {
	public static DeliveryDto generateDeliveryDto(String courier, DeliveryStatus status) {
		return DeliveryDto.builder()
					.orderId(UUID.randomUUID())
					.status(status)
					.destination("destination")
					.courier(courier)
				.build();
	}

	public static CoordinatesDto generateCoordinatesDto(UUID orderId) {
		return CoordinatesDto.builder()
				.orderId(orderId)
				.latitude(CoordinatesUtil.generateCoordinates())
				.longitude(CoordinatesUtil.generateCoordinates())
				.build();
	}

	public static Coordinates generateCoordinatesEntity(UUID orderId) {
		return Coordinates.builder()
				.orderId(orderId)
				.latitude(CoordinatesUtil.generateCoordinates())
				.longitude(CoordinatesUtil.generateCoordinates())
				.build();
	}

	public static Delivery generateDelivery(UUID orderId, String destination) {
		return new Delivery(orderId, destination, DeliveryStatus.PICKED_UP, 3L, "courier");
	}

	public static Delivery generateDelivery(DeliveryStatus status) {
		return new Delivery(UUID.randomUUID(), "destination", status, 3L, "courier");
	}

	public static Delivery generateDelivery(DeliveryStatus status, Long courierId, String courier) {
		return new Delivery(UUID.randomUUID(), "destination", status, courierId, courier);
	}
}
