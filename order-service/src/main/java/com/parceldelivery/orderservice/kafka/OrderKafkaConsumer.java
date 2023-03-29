package com.parceldelivery.orderservice.kafka;

import com.parceldelivery.shared.kafka.OrderChangeStatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderKafkaConsumer {
	private final OrderKafkaConsumerService consumerService;

	@KafkaListener(topics = "${kafka.topic.edit-status-delivery}", groupId = "${spring.kafka.consumer.group-id}")
	@RetryableTopic
	public void consumeOrderStatusUpdates(OrderChangeStatusMessage message) {
		log.info("Received a message to change the order status -> {}", message.toString());
		consumerService.updateStatus(message);
	}
}