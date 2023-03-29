package com.parceldelivery.deliveryservice.repository;

import com.parceldelivery.deliveryservice.model.Coordinates;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoordinatesRepository extends CrudRepository<Coordinates, Long> {
	@Query(value = "SELECT id, order_id, latitude, longitude FROM delivery_coordinates " +
			"WHERE order_id = :orderId ORDER BY id DESC FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
	Optional<Coordinates> findActualDeliveryLocation(UUID orderId);
}