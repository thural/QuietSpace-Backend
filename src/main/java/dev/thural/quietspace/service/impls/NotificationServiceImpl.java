package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Notification;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.custom.NotificationMapper;
import dev.thural.quietspace.model.response.NotificationResponse;
import dev.thural.quietspace.repository.NotificationRepository;
import dev.thural.quietspace.service.NotificationService;
import dev.thural.quietspace.service.UserService;
import dev.thural.quietspace.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.DEFAULT_SORT_OPTION;
import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

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

}
