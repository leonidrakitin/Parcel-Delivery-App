package com.parceldelivery.shared.kafka.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KafkaTopicConfig {

	@Value("${kafka.topic.put-delivery}")
	private String putDeliveryTopic;

	@Value("${kafka.topic.edit-status-delivery}")
	private String editStatusDeliveryTopic;

	@Value("${kafka.topic.cancel-delivery}")
	private String cancelDeliveryTopic;

	@Value("${kafka.topic.assign-courier}")
	private String assignCourierTopic;

	@Value("${kafka.topic.notification-message}")
	private String notificationTopic;

	/* USE BELOW CODE FOR SPECIFIC SETTINGS
	@Bean
	public List<NewTopic> deliveryServiceTopics() {
		return List.of(
				TopicBuilder.name(properties.getCreateNewDeliveryTopic()).build(),
				TopicBuilder.name(properties.getEditDestinationDeliveryTopic()).build(),
				TopicBuilder.name(properties.getCancelDeliveryTopic()).build()
		);
	}
	*/
}