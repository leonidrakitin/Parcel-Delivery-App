package com.parceldelivery.deliveryservice.repository;

import com.parceldelivery.deliveryservice.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
	List<Delivery> findAllByCourierId(Long courierId);
}