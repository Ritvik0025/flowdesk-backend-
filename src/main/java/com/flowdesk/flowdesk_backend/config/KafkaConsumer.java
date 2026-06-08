package com.flowdesk.flowdesk_backend.config;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @KafkaListener(topics = "task.events", groupId = "flowdesk-group")
    public void consumeTaskEvent(String message) {
        System.out.println("Kafka event received: " + message);

        String[] parts = message.split(":");
        if (parts.length >= 3) {
            String eventType = parts[0];
            String taskId = parts[1];
            String projectId = parts[2];

            switch (eventType) {
                case "TASK_CREATED":
                    System.out.println("New task " + taskId + " created in project " + projectId);
                    break;
                case "TASK_UPDATED":
                    System.out.println("Task " + taskId + " status updated in project " + projectId);
                    break;
                case "TASK_DELETED":
                    System.out.println("Task " + taskId + " deleted from project " + projectId);
                    break;
            }
        }
    }
}