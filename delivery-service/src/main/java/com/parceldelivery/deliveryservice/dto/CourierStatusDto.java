package com.parceldelivery.deliveryservice.dto;

import java.util.List;

public record CourierStatusDto (String courierName, List<DeliveryDto> deliveryDtoList) {
}
