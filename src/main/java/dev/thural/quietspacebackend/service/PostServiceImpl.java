package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.mapper.PostMapper;
import dev.thural.quietspacebackend.model.PostDTO;
import dev.thural.quietspacebackend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    PostMapper postMapper;
    PostRepository postRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public List<PostDTO> getAll() {
        return postRepository.findAll()
                .stream()
                .map(postMapper::postEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PostDTO addOne(PostDTO post) {
        PostEntity postEntity = postMapper.postDtoToEntity(post);
        return postMapper.postEntityToDto(postRepository.save(postEntity));
    }

    @Override
    public Optional<PostDTO> getById(UUID id) {
        PostEntity postEntity = postRepository.findById(id).orElse(null);
        PostDTO postDTO = postMapper.postEntityToDto(postEntity);
        return Optional.of(postDTO);
    }

    @Override
    public void updateOne(UUID id, PostDTO post) {
        PostEntity postEntity = postRepository.findById(id).orElse(null);
        PostDTO postDTO = postMapper.postEntityToDto(postEntity);
        postDTO.setUsername(post.getUsername());
        postDTO.setText(post.getText());
        postDTO.setComments(post.getComments());
        postDTO.setLikes(post.getLikes());
        postRepository.save(postMapper.postDtoToEntity(postDTO));
    }

    @Override
    public void deleteOne(UUID id) {
        postRepository.deleteById(id);
    }

    @Override
    public void patchOne(UUID id, PostDTO post) {
        PostEntity postEntity = postRepository.findById(id).orElse(null);
        PostDTO postDTO = postMapper.postEntityToDto(postEntity);
        if (StringUtils.hasText(post.getUsername()))
            postDTO.setUsername(post.getUsername());
        if (StringUtils.hasText(post.getText()))
            postDTO.setText(post.getText());
        if (post.getComments() != null)
            postDTO.setComments(post.getComments());
        if (post.getLikes() != null)
            postDTO.setLikes(post.getLikes());
        postRepository.save(postMapper.postDtoToEntity(postDTO));
    }

}
