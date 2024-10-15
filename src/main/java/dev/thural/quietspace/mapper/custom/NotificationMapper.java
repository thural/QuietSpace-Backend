package dev.thural.quietspace.mapper.custom;

import dev.thural.quietspace.entity.Notification;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.model.response.NotificationResponse;
import dev.thural.quietspace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final UserRepository userRepository;

    public NotificationResponse toResponse(Notification notification) {
        User foundUser = userRepository.findById(notification.getActorId())
                .orElseThrow(UserNotFoundException::new);

        var response = new NotificationResponse();
        BeanUtils.copyProperties(notification, response);
        response.setActorId(foundUser.getId());
        response.setType(notification.getNotificationType());
        return response;
    }

}
