package com.digitalwallet.notification_service.service;

import com.digitalwallet.notification_service.entity.Notification;

import java.util.List;

public interface NotificationService {

    Notification sendNotification(Notification notification);

    List<Notification> getNotificationByUserId(String userId);
}
