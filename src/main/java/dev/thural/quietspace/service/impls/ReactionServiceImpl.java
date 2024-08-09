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
import dev.thural.quietspace.utils.enums.ReactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.DEFAULT_SORT_OPTION;
import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final ReactionMapper reactionMapper;
    private final UserService userService;

    @Override
    public void handleReaction(ReactionRequest reaction) {
        User user = userService.getSignedUser();
        Reaction foundReaction = reactionRepository
                .findByContentIdAndUserId(reaction.getContentId(), user.getId())
                .orElse(null);

        if (foundReaction == null) {
            reactionRepository.save(reactionMapper.reactionRequestToEntity(reaction));
        } else if (reaction.getReactionType().equals(foundReaction.getReactionType())) {
            reactionRepository.deleteById(foundReaction.getId());
        } else {
            foundReaction.setReactionType(reaction.getReactionType());
            reactionRepository.save(foundReaction);
        }
    }

    @Override
    public Optional<ReactionResponse> getUserReactionByContentId(UUID contentId) {
        User user = userService.getSignedUser();
        Optional<Reaction> userReaction = reactionRepository.findByContentIdAndUserId(contentId, user.getId());
        return userReaction.map(reactionMapper::reactionEntityToResponse);
    }

    @Override
    public Page<ReactionResponse> getReactionsByContentIdAndReactionType(UUID contentId, ReactionType reactionType, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        return reactionRepository.findAllByContentIdAndReactionType(contentId, ReactionType.LIKE, pageRequest)
                .map(reactionMapper::reactionEntityToResponse);
    }

    @Override
    public Integer getLikeCountByContentIdAndReactionType(UUID contentId, ReactionType reactionType) {
        return reactionRepository.countByContentIdAndReactionType(contentId, ReactionType.LIKE);
    }

    @Override
    public Page<ReactionResponse> getReactionsByContentIdAndContentType(UUID contentId, ContentType type, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        return reactionRepository.findAllByContentIdAndContentType(contentId, type, pageRequest)
                .map(reactionMapper::reactionEntityToResponse);
    }

    @Override
    public Page<ReactionResponse> getReactionsByUserIdAndContentType(UUID userId, ContentType contentType, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        return reactionRepository.findAllByUserIdAndContentType(userId, contentType, pageRequest)
                .map(reactionMapper::reactionEntityToResponse);
    }

}
