package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.entity.Notification;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.mapper.custom.NotificationMapper;
import dev.thural.quietspace.model.response.NotificationResponse;
import dev.thural.quietspace.repository.CommentRepository;
import dev.thural.quietspace.repository.NotificationRepository;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.service.NotificationService;
import dev.thural.quietspace.service.UserService;
import dev.thural.quietspace.utils.enums.ContentType;
import dev.thural.quietspace.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

    private static final String NOTIFICATION_SUBJECT_PATH = "/private/notifications";

    @Override
    public void handleSeen(UUID contentId) {
        User signedUser = userService.getSignedUser();
        notificationRepository.findByContentIdAndUserId(contentId, signedUser.getId())
                .map(entity -> {
                    entity.setIsSeen(true);
                    return entity;
                }).ifPresent(notificationRepository::save);
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
        String recipientName = userService.getUserById(recipientId)
                .map(User::getUsername).orElseThrow(UserNotFoundException::new);

        var notification = notificationRepository.save(
                Notification
                        .builder()
                        .notificationType(type)
                        .contentId(contentId)
                        .actorId(signedUserId)
                        .userId(recipientId)
                        .build()
        );

        try {
            log.info("notified {} user {}", notification.getNotificationType(), notification.getUserId());
            template.convertAndSendToUser(recipientName, "/notifications", notification);
            System.out.println("notification was sent to user: " + recipientName);
        } catch (MessagingException exception) {
            log.info("failed to notify {} user {}", notification.getNotificationType(), notification.getUserId());
        }
    }

    public void processNotificationByReaction(ContentType type, UUID contentId) {
        switch (type) {
            case COMMENT -> processNotification(NotificationType.COMMENT_REACTION, contentId);
            case POST -> processNotification(NotificationType.POST_REACTION, contentId);
        }
    }

    private UUID getRecipientId(NotificationType type, UUID contentId) {
        return switch (type) {
            case COMMENT, REPOST, POST_REACTION -> getUserIdByPostId(contentId);
            case COMMENT_REPLY, COMMENT_REACTION -> getUserIdByCommentId(contentId);
            case FOLLOW_REQUEST -> contentId;
        };
    }

    private UUID getUserIdByPostId(UUID postId) {
        return postRepository.findById(postId)
                .map(Post::getUser).map(User::getId).orElseThrow();
    }

    private UUID getUserIdByCommentId(UUID commentId) {
        return commentRepository.findById(commentId)
                .map(Comment::getUser).map(User::getId).orElseThrow();
    }
}
