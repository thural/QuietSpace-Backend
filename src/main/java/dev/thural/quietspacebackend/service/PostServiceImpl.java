package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.Post;
import dev.thural.quietspacebackend.repository.PostRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    PostRepository postRepository;

    @Autowired
    PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public List<Post> getAll() {
        return postRepository.findAll();
    }

    @Override
    public Post addOne(Post post) {
        return postRepository.save(post);
    }

    @Override
    public Optional<Post> getById(ObjectId id) {
        return postRepository.findById(id);
    }

    @Override
    public void updateOne(ObjectId id, Post post) {
        Optional<Post> optionalPost = postRepository.findById(id);
        Post foundPost = optionalPost.get();
        foundPost.setUsername(post.getUsername());
        foundPost.setText(post.getText());
        foundPost.setComments(post.getComments());
        foundPost.setLikes(post.getLikes());
        postRepository.save(foundPost);
    }

    @Override
    public void deleteOne(ObjectId id) {
        postRepository.deleteById(id);
    }

    @Override
    public void patchOne(ObjectId id, Post post) {
        Optional<Post> optionalPost = postRepository.findById(id);
        Post foundPost = optionalPost.get();
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
