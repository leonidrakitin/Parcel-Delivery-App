package com.parceldelivery.orderservice.model;

import com.parceldelivery.shared.model.OrderStatus;
import lombok.*;
import org.hibernate.annotations.Type;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {

	@Id
	@Column(name = "order_uuid")
	@GeneratedValue(generator = "uuid2")
	@Type(type = "pg-uuid")
	private UUID orderId;

	@Column(name = "description")
	private String description;

	@Column(name = "price")
	private Double price;

	@Column(name = "destination")
	private String destination;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private OrderStatus status;

	@Column(name = "created_time")
	private ZonedDateTime createdTime;

	@Column(name = "created_by_id")
	private Long createdById;

	@Column(name = "created_by_name")
	private String createdByName;
}