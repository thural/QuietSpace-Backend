package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Reaction;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.repository.ReactionRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    UserRepository userRepository;
    ReactionRepository reactionRepository;

    @Override
    public void handleReaction(ReactionRequest reaction) {
        User user = getUserFromSecurityContext();
        Optional<Reaction> optionalReaction = reactionRepository
                .findByContentIdAndUserId(reaction.getContentId(), user.getId());

        if (optionalReaction.isEmpty()) {
            reactionRepository.save(Reaction.builder()
                    .contentId(reaction.getContentId())
                    .userId(user.getId())
                    .contentType(reaction.getContentType())
                    .likeType(reaction.getLikeType())
                    .build());
        } else if (reaction.getLikeType().equals(optionalReaction.get().getLikeType())) {
            reactionRepository.deleteById(optionalReaction.get().getId());
        } else {
            Reaction existingReaction = optionalReaction.get();
            existingReaction.setLikeType(reaction.getLikeType());
            reactionRepository.save(existingReaction);
        }
    }

    private User getUserFromSecurityContext() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
    }

}
