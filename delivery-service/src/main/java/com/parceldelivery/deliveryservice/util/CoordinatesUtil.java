package com.parceldelivery.deliveryservice.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CoordinatesUtil {
	public static Double generateCoordinates() {
		return Math.random() * (Math.random() * 1000 + 1);
	}
}
