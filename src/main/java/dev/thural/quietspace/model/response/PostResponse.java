package dev.thural.quietspace.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponse extends BaseResponse {

    private String userId;
    private String repostId;
    private String username;
    private String title;
    private String text;
    private String repostText;
    private PollResponse poll;
    private PhotoResponse photo;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer commentCount;
    private ReactionResponse userReaction;

}