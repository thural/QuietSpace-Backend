package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.entity.PostLikeEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.model.PostLikeDTO;
import dev.thural.quietspacebackend.repository.PostLikeRepository;
import dev.thural.quietspacebackend.repository.PostRepository;
import dev.thural.quietspacebackend.service.PostLikeService;
import dev.thural.quietspacebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    @Override
    public List<PostLikeDTO> getAllByPostId(UUID postId) {
        return postLikeRepository.findAllByPostId(postId);
    }

    @Override
    public List<PostLikeDTO> getAllByUserId(UUID userId) {
        return postLikeRepository.findAllByUserId(userId);
    }

    @Override
    public void togglePostLike(String authHeader, UUID postId) {
        UserEntity userEntity = userService.findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("logged user was not found"));
        boolean isPostLikeExists = postLikeRepository.existsByPostIdAndUserId(postId, userEntity.getId());

        if (isPostLikeExists) postLikeRepository.deleteById(postId);
        else {
            PostEntity postEntity = postRepository.findById(postId)
                    .orElseThrow(() -> new UserNotFoundException("post not found"));
            postLikeRepository.save(PostLikeEntity.builder().post(postEntity).user(userEntity).build());
        }
    }

}
