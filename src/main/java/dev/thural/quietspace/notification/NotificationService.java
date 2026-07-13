package dev.thural.quietspace.notification;

import dev.thural.quietspace.shared.enums.EntityType;
import dev.thural.quietspace.shared.enums.NotificationType;
import dev.thural.quietspace.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface NotificationService {

    void handleSeen(UUID notificationId);

    void processNotification(NotificationType type, UUID contentId);

    void processNotificationByReaction(EntityType type, UUID contentId);

    Page<NotificationResponse> getAllNotifications(Integer pageNumber, Integer pageSize);

    Page<NotificationResponse> getNotificationsByType(Integer pageNumber, Integer pageSize, String notificationType);

    Integer getCountOfPendingNotifications();

}
