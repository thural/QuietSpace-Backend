package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Reaction;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.custom.ReactionMapper;
import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.repository.ReactionRepository;
import dev.thural.quietspace.service.ReactionService;
import dev.thural.quietspace.service.UserService;
import dev.thural.quietspace.utils.enums.ContentType;
import dev.thural.quietspace.utils.enums.LikeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final ReactionMapper reactionMapper;
    private final UserService userService;

    @Override
    public void handleReaction(ReactionRequest reaction) {
        User user = userService.getLoggedUser();
        Reaction foundReaction = reactionRepository
                .findByContentIdAndUserId(reaction.getContentId(), user.getId())
                .orElse(null);

        if (foundReaction == null) {
            reactionRepository.save(reactionMapper.reactionRequestToEntity(reaction));
        } else if (reaction.getLikeType().equals(foundReaction.getLikeType())) {
            reactionRepository.deleteById(foundReaction.getId());
        } else {
            foundReaction.setLikeType(reaction.getLikeType());
            reactionRepository.save(foundReaction);
        }
    }

    @Override
    public Optional<ReactionResponse> getUserReactionByContentId(UUID contentId) {
        User user = userService.getLoggedUser();
        Optional<Reaction> userReaction = reactionRepository.findByContentIdAndUserId(contentId, user.getId());
        return userReaction.map(reactionMapper::reactionEntityToResponse);
    }

    @Override
    public List<ReactionResponse> getLikesByContentId(UUID contentId) {
        return reactionRepository.findAllByContentIdAndLikeType(contentId, LikeType.LIKE)
                .stream().map(reactionMapper::reactionEntityToResponse)
                .toList();
    }

    @Override
    public List<ReactionResponse> getDislikesByContentId(UUID contentId) {
        return reactionRepository.findAllByContentIdAndLikeType(contentId, LikeType.DISLIKE)
                .stream().map(reactionMapper::reactionEntityToResponse)
                .toList();
    }

    @Override
    public Integer getLikeCountByContentId(UUID contentId) {
        return reactionRepository.countByContentIdAndLikeType(contentId, LikeType.LIKE);
    }

    @Override
    public Integer getDislikeCountByContentId(UUID contentId) {
        return reactionRepository.countByContentIdAndLikeType(contentId, LikeType.DISLIKE);
    }

    @Override
    public List<ReactionResponse> getReactionsByContentId(UUID contentId, ContentType type) {
        return reactionRepository.findAllByContentIdAndContentType(contentId, type)
                .stream().map(reactionMapper::reactionEntityToResponse)
                .toList();
    }

    @Override
    public List<ReactionResponse> getReactionsByUserId(UUID userId, ContentType contentType) {
        return reactionRepository.findAllByUserIdAndContentType(userId, contentType)
                .stream().map(reactionMapper::reactionEntityToResponse)
                .toList();
    }

}
