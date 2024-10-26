package dev.thural.quietspace.service.impl;

import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.entity.Notification;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.enums.ContentType;
import dev.thural.quietspace.enums.NotificationType;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.mapper.NotificationMapper;
import dev.thural.quietspace.model.response.NotificationResponse;
import dev.thural.quietspace.repository.CommentRepository;
import dev.thural.quietspace.repository.NotificationRepository;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.service.NotificationService;
import dev.thural.quietspace.service.UserService;
import dev.thural.quietspace.websocket.event.message.NotificationEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.util.UUID;

import static dev.thural.quietspace.controller.NotificationController.NOTIFICATION_EVENT_PATH;
import static dev.thural.quietspace.controller.NotificationController.NOTIFICATION_SUBJECT_PATH;
import static dev.thural.quietspace.enums.EventType.SEEN_NOTIFICATION;
import static dev.thural.quietspace.enums.NotificationType.COMMENT_REACTION;
import static dev.thural.quietspace.enums.NotificationType.POST_REACTION;
import static dev.thural.quietspace.utils.PagingProvider.DEFAULT_SORT_OPTION;
import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final SimpMessagingTemplate template;

    @Override
    @Transactional
    public void handleSeen(UUID notificationId) {
        log.info("setting notification with id {} as seen ...", notificationId);
        User user = userService.getSignedUser();
        var notification = notificationRepository.findById(notificationId).orElseThrow(EntityNotFoundException::new);
        if (!notification.getUserId().equals(user.getId()))
            throw new ResourceAccessException("denied access for requested resource");
        if (!notification.getIsSeen()) notification.setIsSeen(true);
        var event = NotificationEvent.builder()
                .actorId(user.getId())
                .notificationId(notificationId)
                .type(SEEN_NOTIFICATION)
                .build();
        template.convertAndSendToUser(user.getId().toString(), NOTIFICATION_EVENT_PATH, event);
    }

    @Override
    public Page<NotificationResponse> getAllNotifications(Integer pageNumber, Integer pageSize) {
        User signedUser = userService.getSignedUser();
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        return notificationRepository.findAllByUserId(signedUser.getId(), pageRequest)
                .map(notificationMapper::toResponse);
    }

    @Override
    public Page<NotificationResponse> getNotificationsByType(Integer pageNumber, Integer pageSize, String notificationType) {
        NotificationType type = NotificationType.valueOf(notificationType);
        User signedUser = userService.getSignedUser();
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        return notificationRepository
                .findAllByUserIdAndNotificationType(signedUser.getId(), type, pageRequest)
                .map(notificationMapper::toResponse);
    }

    @Override
    public Integer getCountOfPendingNotifications() {
        User signedUser = userService.getSignedUser();
        return notificationRepository.countByUserIdAndIsSeen(signedUser.getId(), false);
    }

    public void processNotification(NotificationType type, UUID contentId) {
        UUID signedUserId = userService.getSignedUser().getId();
        UUID recipientId = getRecipientId(type, contentId);
        userService.getUserById(recipientId).map(User::getUsername).orElseThrow(UserNotFoundException::new);
        var notification = notificationRepository.save(
                Notification
                        .builder()
                        .notificationType(type)
                        .contentId(contentId)
                        .actorId(signedUserId)
                        .userId(recipientId)
                        .build()
        );
        var response = notificationMapper.toResponse(notification);
        try {
            log.info("notified {} user {}", response.getType(), response.getActorId());
            template.convertAndSendToUser(recipientId.toString(), NOTIFICATION_SUBJECT_PATH, response);
        } catch (MessagingException exception) {
            log.info("failed to notify {} user {}", response.getType(), response.getActorId());
        }
    }

    public void processNotificationByReaction(ContentType type, UUID contentId) {
        switch (type) {
            case COMMENT -> processNotification(COMMENT_REACTION, contentId);
            case POST -> processNotification(POST_REACTION, contentId);
        }
    }

    private UUID getRecipientId(NotificationType type, UUID contentId) {
        return switch (type) {
            case COMMENT, REPOST, POST_REACTION -> getUserIdByPostId(contentId);
            case COMMENT_REPLY, COMMENT_REACTION -> getUserIdByCommentId(contentId);
            case FOLLOW_REQUEST -> contentId;
            default -> throw new RuntimeException("(!) implement mention feature");
        };
    }

    private UUID getUserIdByPostId(UUID postId) {
        return postRepository.findById(postId).map(Post::getUser).map(User::getId).orElseThrow();
    }

    private UUID getUserIdByCommentId(UUID commentId) {
        return commentRepository.findById(commentId).map(Comment::getUser).map(User::getId).orElseThrow();
    }
}
