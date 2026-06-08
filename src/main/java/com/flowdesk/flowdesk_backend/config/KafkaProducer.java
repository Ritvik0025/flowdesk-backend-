package com.flowdesk.flowdesk_backend.config;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTaskEvent(String eventType, Long taskId, String projectId) {
        String message = eventType + ":" + taskId + ":" + projectId;
        kafkaTemplate.send("task.events", message);
        System.out.println("Kafka event sent: " + message);
    }
}