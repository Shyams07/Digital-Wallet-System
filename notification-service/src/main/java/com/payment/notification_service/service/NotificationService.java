package com.payment.notification_service.service;

import java.util.List;
import com.payment.notification_service.entity.Notification;

public interface NotificationService {
    Notification sendNotification(Notification notification);
    List<Notification> getNotificationsByUserId(Long userId);
}
