package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.Poll;
import dev.thural.quietspace.entity.PollOption;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.request.PollRequest;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.request.RepostRequest;
import dev.thural.quietspace.model.response.OptionResponse;
import dev.thural.quietspace.model.response.PollResponse;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.service.ReactionService;
import dev.thural.quietspace.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static dev.thural.quietspace.enums.ReactionType.DISLIKE;
import static dev.thural.quietspace.enums.ReactionType.LIKE;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostMapper {

    private final ReactionService reactionService;
    private final UserService userService;
    private final PostRepository postRepository;

    public Post postRequestToEntity(PostRequest postRequest) {
        Post post = Post.builder()
                .user(getLoggedUser())
                .title(postRequest.getTitle())
                .text(postRequest.getText())
                .build();

        if (postRequest.getPoll() == null) return post;

        PollRequest pollRequest = postRequest.getPoll();

        Poll newPoll = Poll.builder()
                .post(post)
                .dueDate(pollRequest.getDueDate())
                .build();

        List<PollOption> options = pollRequest.getOptions().stream()
                .<PollOption>map(option -> PollOption.builder()
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
        Integer commentCount = post.getComments() != null ? post.getComments().size() : 0;
        Integer likeCount = reactionService.countByContentIdAndReactionType(post.getId(), LIKE);
        Integer dislikeCount = reactionService.countByContentIdAndReactionType(post.getId(), DISLIKE);
        ReactionResponse userReaction = reactionService.getUserReactionByContentId(post.getId())
                .orElse(null);

        PostResponse postResponse = PostResponse.builder()
                .id(post.getId().toString())
                .title(post.getTitle())
                .text(post.getText())
                .commentCount(commentCount)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .userId(post.getUser().getId().toString())
                .username(post.getUser().getUsername())
                .userReaction(userReaction)
                .createDate(post.getCreateDate())
                .updateDate(post.getUpdateDate())
                .build();

        if (post.getRepostId() != null) {
            postResponse.setRepostText(post.getRepostText());
            postResponse.setRepostId(post.getRepostId());
        }

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

    private Integer getVoteCount(Poll poll) {
        return poll.getOptions().stream()
                .map(option -> option.getVotes().size())
                .reduce(0, Integer::sum);
    }

    private String getVoteShare(PollOption option) {
        Integer totalVoteCount = getVoteCount(option.getPoll());
        int optionVoteNum = option.getVotes() != null ? option.getVotes().size() : 0;
        if (totalVoteCount < 1) return "0%";
        return (optionVoteNum * 100 / totalVoteCount) + "%";
    }

    private String getVotedPollOptionLabel(Poll poll, UUID userId) {
        return poll.getOptions().stream()
                .filter(option -> option.getVotes().contains(userId))
                .findFirst()
                .map(PollOption::getLabel)
                .orElse("not voted");
    }

    private User getLoggedUser() {
        return userService.getSignedUser();
    }

    public Post repostRequestToEntity(RepostRequest repost) {
        return Post.builder()
                .user(getLoggedUser())
                .repostId(repost.getPostId().toString())
                .repostText(repost.getText())
                .build();
    }

}
