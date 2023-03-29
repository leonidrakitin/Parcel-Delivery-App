package com.parceldelivery.orderservice.repository;

import com.parceldelivery.orderservice.model.Order;
import org.springframework.data.domain.Page;
import javax.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
	@Nonnull
	Page<Order> findAll(@Nonnull Pageable pageable);
	List<Order> findAllByCreatedById(Long createdById);
}