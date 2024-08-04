package dev.thural.quietspace.model.response;

import dev.thural.quietspace.utils.enums.LikeType;
import dev.thural.quietspace.utils.enums.NotificationType;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private UUID actorId;
    private UUID contentId;
    private String username;
    private NotificationType type;
    private OffsetDateTime updateDate;

}
