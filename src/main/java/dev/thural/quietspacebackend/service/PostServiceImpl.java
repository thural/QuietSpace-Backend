package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.PostDTO;
import dev.thural.quietspacebackend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PostServiceImpl implements PostService {

    PostRepository postRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public List<PostDTO> getAll() {
        return postRepository.findAll();
    }

    @Override
    public PostDTO addOne(PostDTO post) {
        return postRepository.save(post);
    }

    @Override
    public Optional<PostDTO> getById(UUID id) {
        return postRepository.findById(id);
    }

    @Override
    public void updateOne(UUID id, PostDTO post) {
        Optional<PostDTO> optionalPost = postRepository.findById(id);
        PostDTO foundPost = optionalPost.get();
        foundPost.setUsername(post.getUsername());
        foundPost.setText(post.getText());
        foundPost.setComments(post.getComments());
        foundPost.setLikes(post.getLikes());
        postRepository.save(foundPost);
    }

    @Override
    public void deleteOne(UUID id) {
        postRepository.deleteById(id);
    }

    @Override
    public void patchOne(UUID id, PostDTO post) {
        Optional<PostDTO> optionalPost = postRepository.findById(id);
        PostDTO foundPost = optionalPost.get();
        if (StringUtils.hasText(post.getUsername()))
            foundPost.setUsername(post.getUsername());
        if (StringUtils.hasText(post.getText()))
            foundPost.setText(post.getText());
        if (post.getComments() != null)
            foundPost.setComments(post.getComments());
        if (post.getLikes() != null)
            foundPost.setLikes(post.getLikes());
        postRepository.save(foundPost);
    }

}
