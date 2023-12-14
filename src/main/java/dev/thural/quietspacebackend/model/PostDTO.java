package dev.thural.quietspacebackend.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDTO {

    private UUID id;

    private OffsetDateTime date = OffsetDateTime.now();

    private String username;

    private String text;

    private List<UserDTO> likes;

    private List<CommentDTO> comments;

}
