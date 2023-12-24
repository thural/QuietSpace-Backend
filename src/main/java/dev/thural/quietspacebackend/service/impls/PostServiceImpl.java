package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.config.JwtProvider;
import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.PostMapper;
import dev.thural.quietspacebackend.model.PostDTO;
import dev.thural.quietspacebackend.repository.PostRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.PostService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public List<PostDTO> getAll() {
        return postRepository.findAll()
                .stream()
                .map(postMapper::postEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PostDTO addOne(PostDTO post, String token) {
        String loggedUserEmail = JwtProvider.getEmailFromJwtToken(token);
        UserEntity loggedUser = userRepository.findUserEntityByEmail(loggedUserEmail).orElse(null);

        PostEntity postEntity = postMapper.postDtoToEntity(post);
        postEntity.setUser(loggedUser);
        return postMapper.postEntityToDto(postRepository.save(postEntity));
    }

    @Override
    public Optional<PostDTO> getById(UUID postId) {
        PostEntity postEntity = postRepository.findById(postId).orElse(null);
        PostDTO postDTO = postMapper.postEntityToDto(postEntity);
        return Optional.of(postDTO);
    }

    @Override
    public void updateOne(UUID postId, PostDTO post) {
        PostEntity postEntity = postRepository.findById(postId).orElse(null);
        PostDTO postDTO = postMapper.postEntityToDto(postEntity);
        postDTO.setText(post.getText());
        postRepository.save(postMapper.postDtoToEntity(postDTO));
    }

    @Override
    public void deleteOne(UUID id) {
        postRepository.deleteById(id);
    }

    @Override
    public void patchOne(UUID postId, PostDTO post) {
        PostEntity postEntity = postRepository.findById(postId).orElse(null);
        PostDTO postDTO = postMapper.postEntityToDto(postEntity);
        if (StringUtils.hasText(post.getText()))
            postDTO.setText(post.getText());
        postRepository.save(postMapper.postDtoToEntity(postDTO));
    }

}
