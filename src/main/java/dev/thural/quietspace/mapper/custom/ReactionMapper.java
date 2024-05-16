package dev.thural.quietspace.mapper.custom;

import dev.thural.quietspace.entity.Reaction;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReactionMapper {

    private final UserRepository userRepository;

    public Reaction reactionRequestToEntity(ReactionRequest reaction){
        return Reaction.builder()
                .userId(reaction.getUserId())
                .username(getUserNameById(reaction.getUserId()))
                .contentId(reaction.getContentId())
                .contentType(reaction.getContentType())
                .likeType(reaction.getLikeType())
                .build();
    }

    public ReactionResponse reactionEntityToResponse(Reaction reaction){
        return ReactionResponse.builder()
                .id(reaction.getId())
                .contentId(reaction.getContentId())
                .likeType(reaction.getLikeType())
                .userId(reaction.getUserId())
                .username(reaction.getUsername())
                .updateDate(reaction.getUpdateDate())
                .build();
    }

    String getUserNameById(UUID userId){
        return userRepository.findById(userId).map(User::getUsername).orElse(null);
    }

}
