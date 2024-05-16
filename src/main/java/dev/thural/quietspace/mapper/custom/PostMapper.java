package dev.thural.quietspace.mapper.custom;

import dev.thural.quietspace.entity.Poll;
import dev.thural.quietspace.entity.PollOption;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.model.request.PollRequest;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.response.OptionResponse;
import dev.thural.quietspace.model.response.PollResponse;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final ReactionService reactionService;

    public Post postRequestToEntity(PostRequest postRequest) {
        Post post = Post.builder()
                .title(postRequest.getTitle())
                .text(postRequest.getText())
                .build();

        if(postRequest.getPoll() == null) return post;

        PollRequest pollRequest = postRequest.getPoll();

        Poll newPoll = Poll.builder()
                .post(post)
                .dueDate(pollRequest.getDueDate())
                .build();

        List<PollOption> options = pollRequest.getOptions().stream()
                .map(option -> PollOption.builder()
                        .label(option)
                        .poll(newPoll)
                        .votes(new HashSet<>())
                        .build())
                .toList();

        newPoll.setOptions(options);
        post.setPoll(newPoll);

        return post;
    }

   public PostResponse postEntityToResponse(Post post) {
        Integer commentCount = post.getComments().size();
        Integer postLikeCount = reactionService.getLikeCountByContentId(post.getId());
        Integer dislikeCount = reactionService.getDislikeCountByContentId(post.getId());
        ReactionResponse userReaction = reactionService.getUserReactionByContentId(post.getId())
                .orElse(null);

        PostResponse postResponse = PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .text(post.getText())
                .commentCount(commentCount)
                .likeCount(postLikeCount)
                .dislikeCount(dislikeCount)
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .userReaction(userReaction)
                .createDate(post.getCreateDate())
                .updateDate(post.getUpdateDate())
                .build();

        if (post.getPoll() == null) return postResponse;

        List<OptionResponse> options = post.getPoll().getOptions().stream()
                .map(option -> OptionResponse.builder()
                        .id(option.getId())
                        .label(option.getLabel())
                        .voteShare(getVoteShare(option))
                        .build())
                .toList();

        PollResponse pollResponse = PollResponse.builder()
                .id(post.getPoll().getId())
                .options(options)
                .votedOption(getVotedPollOptionLabel(post.getPoll(), post.getUser().getId()))
                .voteCount(getVoteCount(post.getPoll()))
                .build();

        postResponse.setPoll(pollResponse);
        return postResponse;
    }

    private Integer getVoteCount(Poll poll){
        return poll.getOptions().stream()
                .map(option -> option.getVotes().size())
                .reduce(0, Integer::sum);
    }

    private String getVoteShare(PollOption option){
        Integer totalVoteNum = option.getPoll().getOptions().stream()
                .map(pollOption -> pollOption.getVotes().size())
                .reduce(0,Integer::sum);

        Integer optionVoteNum = option.getVotes().size();
        if (totalVoteNum < 1) return "0%";
        return optionVoteNum/totalVoteNum +  "%";
    }

    public String getVotedPollOptionLabel(Poll poll, UUID userId){
        return poll.getOptions().stream()
                .filter(option -> option.getVotes().contains(userId))
                .findAny()
                .map(PollOption::getLabel).orElse("not voted");
    }
}
