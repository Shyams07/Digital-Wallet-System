package com.payment.notification_service.kafka;


import com.payment.notification_service.entity.Notification;
import com.payment.notification_service.entity.Transaction;
import com.payment.notification_service.repository.NotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Component
public class NotificationConsumer {
    private final NotificationRepository notificationRepository;
    private final ObjectMapper mapper;

    public NotificationConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        this.mapper = new ObjectMapper();
    }

    @KafkaListener(topics = "txn-initiated", groupId = "notification-group")
    public void consumeTransaction(String message) {

        System.out.println("Received: " + message);

        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setMessage(message);
        notification.setSentAt(LocalDateTime.now());

        notificationRepository.save(notification);
        System.out.println(" Notification saved: " + notification);
    }

}
