package dev.thural.quietspace.mapper.custom;

import dev.thural.quietspace.entity.Notification;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.model.response.NotificationResponse;
import dev.thural.quietspace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final UserRepository userRepository;

    public NotificationResponse toResponse(Notification notification) {
        User foundUser = userRepository.findById(notification.getActorId())
                .orElseThrow(UserNotFoundException::new);

        return NotificationResponse.builder()
                .id(notification.getId())
                .actorId(foundUser.getId())
                .contentId(notification.getContentId())
                .type(notification.getNotificationType())
                .username(foundUser.getUsername())
                .updateDate(notification.getUpdateDate())
                .build();
    }

}
