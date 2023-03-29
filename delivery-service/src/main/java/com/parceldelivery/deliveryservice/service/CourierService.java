package com.parceldelivery.deliveryservice.service;

import com.parceldelivery.deliveryservice.dto.CourierAssignmentDto;
import com.parceldelivery.deliveryservice.dto.CourierStatusDto;

import java.util.List;

public interface CourierService {
	List<CourierStatusDto> getListCouriersStatus();
	void assignCourier(String token, CourierAssignmentDto dto);
}
