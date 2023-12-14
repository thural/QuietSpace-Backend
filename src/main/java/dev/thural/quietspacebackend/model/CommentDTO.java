package dev.thural.quietspacebackend.model;

import lombok.*;
import org.apache.catalina.User;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {

    private UUID id;

    private OffsetDateTime date = OffsetDateTime.now();

    private String userId;

    private String text;

    private List<User> likes;

}
