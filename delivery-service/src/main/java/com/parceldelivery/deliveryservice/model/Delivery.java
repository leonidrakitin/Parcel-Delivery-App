package com.parceldelivery.deliveryservice.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
	@Id
	@Column(name = "order_uuid")
	private UUID orderId;

	@Column(name = "destination")
	private String destination;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private DeliveryStatus status;

	@Column(name = "courier_id")
	private Long courierId;

	@Column(name = "courier")
	private String courier;
}
