package dev.thural.quietspacebackend.model.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private UUID id;
    private Integer version;
    private List<UUID> userIds;
    private List<UserResponse> users;
    private List<MessageResponse> messages;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;

}
