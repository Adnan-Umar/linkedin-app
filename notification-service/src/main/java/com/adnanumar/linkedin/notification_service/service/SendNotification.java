package com.adnanumar.linkedin.notification_service.service;

import com.adnanumar.linkedin.notification_service.entity.Notification;
import com.adnanumar.linkedin.notification_service.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class SendNotification {

    final NotificationRepository notificationRepository;

    public void send(Long userId, String message) {
        log.info("Sending notification to user with id: '{}'", userId);
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);

        notificationRepository.save(notification);
        log.info("Notification sent and save for user with id: '{}'", userId);
    }

}
