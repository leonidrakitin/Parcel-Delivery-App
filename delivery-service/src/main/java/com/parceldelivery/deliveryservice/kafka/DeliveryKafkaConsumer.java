package com.parceldelivery.deliveryservice.kafka;

import com.parceldelivery.shared.kafka.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryKafkaConsumer {
	private final DeliveryKafkaConsumerService consumerService;

	@KafkaListener(topics = "${kafka.topic.put-delivery}", groupId = "${spring.kafka.consumer.group-id}")
	@RetryableTopic
	public void consumeOrderUpdates(OrderMessage message) {
		log.info("Received a message to put a delivery -> {}", message.toString());
		consumerService.putDelivery(message);
	}

	@KafkaListener(topics = "${kafka.topic.cancel-delivery}", groupId = "${spring.kafka.consumer.group-id}")
	@RetryableTopic
	public void consumeOrderCancelled(UUID orderId) {
		log.info("Received a message to cancel the delivery -> {}", orderId);
		consumerService.cancelDelivery(orderId);
	}
}