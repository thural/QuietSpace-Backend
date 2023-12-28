package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.controller.NotFoundException;
import dev.thural.quietspacebackend.utils.JwtProvider;
import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.PostMapper;
import dev.thural.quietspacebackend.model.PostDTO;
import dev.thural.quietspacebackend.repository.PostRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.PostService;
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
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public Page<PostDTO> getAll(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        return postRepository.findAll(pageRequest).map(postMapper::postEntityToDto);
    }

    @Override
    public PostDTO addOne(PostDTO post, String token) {
        UserEntity loggedUser = getUserEntityByToken(token);
        PostEntity postEntity = postMapper.postDtoToEntity(post);
        postEntity.setUser(loggedUser);

        postEntity.setUser(loggedUser);
        return postMapper.postEntityToDto(postRepository.save(postEntity));
    }

    @Override
    public Optional<PostDTO> getById(UUID postId) {
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(NotFoundException::new);
        PostDTO postDTO = postMapper.postEntityToDto(postEntity);
        return Optional.of(postDTO);
    }

    @Override
    public void updateOne(UUID postId, PostDTO post, String jwtToken) {
        UserEntity loggedUser = getUserEntityByToken(jwtToken);
        PostEntity existingPostEntity = postRepository.findById(postId).orElseThrow(NotFoundException::new);

        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPostEntity, loggedUser);

        if (postExistsByLoggedUser) {
            existingPostEntity.setText(post.getText());
            postRepository.save(existingPostEntity);
        } else throw new AccessDeniedException("post author does not belong to current user");
    }

    @Override
    public void deleteOne(UUID postId, String jwtToken) {
        UserEntity loggedUser = getUserEntityByToken(jwtToken);
        PostEntity existingPostEntity = postRepository.findById(postId).orElseThrow(NotFoundException::new);

        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPostEntity, loggedUser);

        if (postExistsByLoggedUser) postRepository.deleteById(postId);
        else throw new AccessDeniedException("post author does not belong to current user");
    }

    @Override
    public void patchOne(String jwtToken, UUID postId, PostDTO post) {
        UserEntity loggedUser = getUserEntityByToken(jwtToken);
        PostEntity existingPostEntity = postRepository.findById(postId).orElseThrow(NotFoundException::new);

        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPostEntity, loggedUser);

        if (postExistsByLoggedUser) {
            if (StringUtils.hasText(post.getText())) existingPostEntity.setText(post.getText());
            postRepository.save(existingPostEntity);
        } else throw new AccessDeniedException("post author does not belong to current user");
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

    private UserEntity getUserEntityByToken(String jwtToken) {
        String loggedUserEmail = JwtProvider.getEmailFromJwtToken(jwtToken);
        return userRepository.findUserEntityByEmail(loggedUserEmail).orElseThrow(NotFoundException::new);
    }

    private boolean isPostExistsByLoggedUser(PostEntity existingPostEntity, UserEntity loggedUser) {
        return existingPostEntity.getUser().equals(loggedUser);
    }

}
