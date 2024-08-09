package dev.thural.quietspace.service;

import dev.thural.quietspace.model.response.NotificationResponse;
import dev.thural.quietspace.utils.enums.NotificationType;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface NotificationService {

    void handleSeen(UUID contentId);

    void processNotification(NotificationType type, UUID contentId);

    Page<NotificationResponse> getAllNotifications(Integer pageNumber, Integer pageSize);

    Page<NotificationResponse> getNotificationsByType(Integer pageNumber, Integer pageSize, String notificationType);

    Integer getCountOfPendingNotifications();

}
