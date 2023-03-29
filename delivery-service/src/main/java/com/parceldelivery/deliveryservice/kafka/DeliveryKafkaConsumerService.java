package com.parceldelivery.deliveryservice.kafka;

import com.parceldelivery.deliveryservice.exception.OrderNotFoundException;
import com.parceldelivery.deliveryservice.model.Coordinates;
import com.parceldelivery.deliveryservice.model.Delivery;
import com.parceldelivery.deliveryservice.model.DeliveryStatus;
import com.parceldelivery.deliveryservice.repository.CoordinatesRepository;
import com.parceldelivery.deliveryservice.repository.DeliveryRepository;
import com.parceldelivery.deliveryservice.util.CoordinatesUtil;
import com.parceldelivery.shared.kafka.OrderMessage;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryKafkaConsumerService {

	private final CoordinatesRepository coordinatesRepository;
	private final DeliveryRepository deliveryRepository;

	@Transactional
	@Retry(name = "retryPutDelivery")
	public void putDelivery(OrderMessage message) {
		deliveryRepository.findById(message.orderId())
				.ifPresentOrElse(
						delivery -> {
							delivery.setDestination(message.destination());
							log.info("Updated the delivery destination to {} with orderId {}",
									message.destination(), message.orderId());
						}, () -> {
							var delivery = deliveryRepository.save(Delivery.builder()
									.orderId(message.orderId())
									.destination(message.destination())
									.status(DeliveryStatus.ORDERED)
									.build());
							coordinatesRepository.save(Coordinates.builder()
									.orderId(delivery.getOrderId())
									.latitude(CoordinatesUtil.generateCoordinates())
									.longitude(CoordinatesUtil.generateCoordinates())
									.build());
							log.info("Create new delivery for order " + message.orderId());
						});
	}

	@Transactional
	@Retryable
	public void cancelDelivery(UUID orderId) {
		deliveryRepository.findById(orderId)
				.ifPresentOrElse(delivery -> {
					delivery.setStatus(DeliveryStatus.CANCELED);
					log.info("The delivery for order {} was cancelled", orderId);
				}, () -> {
					log.error("Received a request to edit a delivery that does not exist.");
					throw new OrderNotFoundException("Not found delivery for order " + orderId);
				});
	}
}
