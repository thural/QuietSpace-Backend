package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.service.UserService;
import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.PostMapper;
import dev.thural.quietspacebackend.model.PostDTO;
import dev.thural.quietspacebackend.repository.PostRepository;
import dev.thural.quietspacebackend.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspacebackend.utils.CustomPageProvider.buildCustomPageRequest;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserService userService;
    private final PostRepository postRepository;

    @Override
    public Page<PostDTO> getAllPosts(Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);

        return postRepository.findAll(pageRequest).map(postMapper::postEntityToDto);
    }

    @Override
    public PostDTO addPost(PostDTO post, String token) {
        UserEntity loggedUserEntity = userService.findUserByJwt(token)
                .orElseThrow(() -> new UserNotFoundException("logged user was not found"));
        PostEntity postEntity = postMapper.postDtoToEntity(post);

        postEntity.setUser(loggedUserEntity);
        return postMapper.postEntityToDto(postRepository.save(postEntity));
    }

    @Override
    public Optional<PostDTO> getPostById(UUID postId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(EntityNotFoundException::new);

        PostDTO postDTO = postMapper.postEntityToDto(postEntity);
        return Optional.of(postDTO);
    }

    @Override
    public void updatePost(UUID postId, PostDTO post, String authHeader) {
        UserEntity loggedUserEntity = userService.findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("logged user was not found"));

        PostEntity existingPostEntity = postRepository.findById(postId)
                .orElseThrow(EntityNotFoundException::new);

        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPostEntity, loggedUserEntity);

        if (postExistsByLoggedUser) {
            existingPostEntity.setText(post.getText());
            postRepository.save(existingPostEntity);
        } else throw new AccessDeniedException("post author does not belong to current user");
    }

    @Override
    public void patchPost(String authHeader, UUID postId, PostDTO post) {
        UserEntity loggedUserEntity = userService.findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("logged user was not found"));

        PostEntity existingPostEntity = postRepository.findById(postId)
                .orElseThrow(EntityNotFoundException::new);

        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPostEntity, loggedUserEntity);

        if (postExistsByLoggedUser) {
            if (StringUtils.hasText(post.getText())) existingPostEntity.setText(post.getText());
            postRepository.save(existingPostEntity);
        } else throw new AccessDeniedException("post author does not belong to current user");
    }

    @Override
    public void deletePost(UUID postId, String authHeader) {
        UserEntity loggedUserEntity = userService.findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("logged user was not found"));

        PostEntity existingPostEntity = postRepository.findById(postId)
                .orElseThrow(EntityNotFoundException::new);

        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPostEntity, loggedUserEntity);

        if (postExistsByLoggedUser) postRepository.deleteById(postId);
        else throw new AccessDeniedException("post author does not belong to current user");
    }

    @Override
    public Page<PostDTO> getPostsByUserId(UUID userId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);

        Page<PostEntity> postPage;

        if (userId != null) {
            postPage = postRepository.findAllByUserId(userId, pageRequest);
        } else {
            postPage = postRepository.findAll(pageRequest);
        }

        return postPage.map(postMapper::postEntityToDto);
    }

    private boolean isPostExistsByLoggedUser(PostEntity existingPostEntity, UserEntity loggedUser) {
        return existingPostEntity.getUser().equals(loggedUser);
    }

}
