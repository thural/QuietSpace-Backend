package dev.thural.quietspace.controller;

import dev.thural.quietspace.enums.ContentType;
import dev.thural.quietspace.enums.NotificationType;
import dev.thural.quietspace.model.response.NotificationResponse;
import dev.thural.quietspace.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    public static final String NOTIFICATION_PATH = "/api/v1/notifications";
    public static final String NOTIFICATION_PATH_ID = "/{notificationId}";

    public static final String NOTIFICATION_SUBJECT_PATH = "/private/notifications";
    public static final String NOTIFICATION_EVENT_PATH = NOTIFICATION_SUBJECT_PATH + "/event";
    public static final String NOTIFICATION_SEEN_PATH = NOTIFICATION_SUBJECT_PATH + "/seen/{notificationId}";

    private final NotificationService notificationService;


    @PostMapping("/seen/{contentId}")
    ResponseEntity<?> handleSeen(@PathVariable UUID contentId) {
        notificationService.handleSeen(contentId);
        return ResponseEntity.accepted().build();
    }

    @MessageMapping(NOTIFICATION_SEEN_PATH)
    void markMessageSeen(@DestinationVariable UUID notificationId) {
        notificationService.handleSeen(notificationId);
    }

    @GetMapping
    Page<NotificationResponse> getAllNotifications(
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize
    ) {
        return notificationService.getAllNotifications(pageNumber, pageSize);
    }

    @GetMapping("/type/{notificationType}")
    Page<NotificationResponse> getNotificationsByType(
            @RequestParam Integer pageNumber,
            @RequestParam Integer pageSize,
            @PathVariable String notificationType
    ) {
        return notificationService.getNotificationsByType(pageNumber, pageSize, notificationType);
    }

    @GetMapping("/count-pending")
    ResponseEntity<Integer> getCountOfPendingNotifications() {
        return ResponseEntity.ok(notificationService.getCountOfPendingNotifications());
    }

    @PostMapping("/process")
    ResponseEntity<?> processNotification(NotificationType type, UUID contentId) {
        notificationService.processNotification(type, contentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/process-reaction")
    ResponseEntity<?> processNotificationByReaction(@RequestParam ContentType type, @RequestParam UUID contentId) {
        notificationService.processNotificationByReaction(type, contentId);
        return ResponseEntity.ok().build();
    }

}
