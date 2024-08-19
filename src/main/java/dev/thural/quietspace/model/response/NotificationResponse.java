package dev.thural.quietspace.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.thural.quietspace.utils.enums.NotificationType;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {

    private UUID id;
    private UUID actorId;
    private UUID contentId;
    private Boolean isSeen;
    private String username;
    private NotificationType type;
    private OffsetDateTime updateDate;

}
