package dev.thural.quietspace.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.thural.quietspace.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse extends BaseResponse {

    private UUID actorId;
    private UUID contentId;
    private Boolean isSeen;
    private String username;
    private NotificationType type;

}