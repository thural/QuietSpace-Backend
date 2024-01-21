package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.exception.NotFoundException;
import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.entity.PostLikeEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.model.PostLikeDTO;
import dev.thural.quietspacebackend.repository.PostLikeRepository;
import dev.thural.quietspacebackend.repository.PostRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.PostLikeService;
import dev.thural.quietspacebackend.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Override
    public List<PostLikeDTO> getAllByPostId(UUID postId) {
        return postLikeRepository.findAllByPostId(postId);
    }

    @Override
    public void togglePostLike(String jwtToken, PostLikeDTO postLikeDTO) {
        UserEntity loggedUser = getUserEntityByToken(jwtToken);

        if (!postLikeDTO.getUserId().equals(loggedUser.getId()))
            throw new AccessDeniedException("post like does not belong to logged user");

        UUID likeUserId = postLikeDTO.getUserId();
        UUID likePostId = postLikeDTO.getPostId();

        boolean isPostLikeExists = postLikeRepository.existsByPostIdAndUserId(likePostId, likeUserId);

        if (isPostLikeExists) {
            postLikeRepository.deleteById(postLikeDTO.getId());
        } else {
            UserEntity userEntity = userRepository.findById(likeUserId).orElseThrow(NotFoundException::new);
            PostEntity postEntity = postRepository.findById(likePostId).orElseThrow(NotFoundException::new);

            PostLikeEntity postLikeEntity = new PostLikeEntity();

            postLikeEntity.setUser(userEntity);
            postLikeEntity.setPost(postEntity);

            postLikeRepository.save(postLikeEntity);
        }

    }

    @Override
    public List<PostLikeDTO> getAllByUserId(UUID userId) {
        return postLikeRepository.findAllByUserId(userId);
    }

    private UserEntity getUserEntityByToken(String jwtToken) {
        String loggedUserEmail = JwtProvider.getEmailFromJwtToken(jwtToken);
        return userRepository.findUserEntityByEmail(loggedUserEmail).orElseThrow(NotFoundException::new);
    }
}
