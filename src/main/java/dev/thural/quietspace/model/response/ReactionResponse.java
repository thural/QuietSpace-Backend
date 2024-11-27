package dev.thural.quietspace.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.thural.quietspace.enums.ReactionType;
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
public class ReactionResponse extends BaseResponse {

    private UUID userId;
    private UUID contentId;
    private String username;
    private ReactionType reactionType;

}
