package com.parceldelivery.deliveryservice.service;

import com.parceldelivery.deliveryservice.dto.DeliveryDto;
import com.parceldelivery.deliveryservice.dto.DeliveryStatusDto;
import com.parceldelivery.deliveryservice.dto.CoordinatesDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface DeliveryService {
	List<DeliveryDto> getDeliveries();
	DeliveryDto getDeliveryDetails(UUID orderId);
	void editStatus(UUID orderId, DeliveryStatusDto dto);
	CoordinatesDto getDeliveryCoordinates(UUID orderId);
}
