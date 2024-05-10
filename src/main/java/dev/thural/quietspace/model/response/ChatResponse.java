package dev.thural.quietspace.model.response;

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
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;

}
