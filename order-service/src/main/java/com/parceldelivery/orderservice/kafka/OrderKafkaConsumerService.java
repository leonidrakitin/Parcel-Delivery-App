package com.parceldelivery.orderservice.kafka;

import com.parceldelivery.orderservice.repository.OrderRepository;
import com.parceldelivery.shared.kafka.config.KafkaTopicConfig;
import com.parceldelivery.shared.kafka.OrderChangeStatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderKafkaConsumerService {
	private final OrderRepository orderRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final KafkaTopicConfig kafkaTopicConfig;
	@Transactional
	@Retryable
	public void updateStatus(OrderChangeStatusMessage message) {
		var orderOptional = orderRepository.findById(message.orderId());
		if (orderOptional.isEmpty()) {
			log.error("Received a request to edit the status of the order that does not exist.");
			return;
		}
		orderOptional.get().setStatus(message.status());
		kafkaTemplate.send(kafkaTopicConfig.getNotificationTopic(), message);
	}
}
