package com.parceldelivery.notificationservice.kafka;

import com.parceldelivery.shared.kafka.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderKafkaConsumer {
	@KafkaListener(topics = "${kafka.topic.notification-message}", groupId = "${spring.kafka.consumer.group-id}")
	public void consumeNotification(NotificationMessage message) {
		// send out an email notification
		log.info("Received a notification message for the order -> {}", message.toString());
	}
}