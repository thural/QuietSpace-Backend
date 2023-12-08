package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.Post;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface PostService {
    List<Post> getAll();

    Post addOne(Post post);

    Optional<Post> getById(ObjectId id);

    void updateOne(ObjectId id, Post post);

    void deleteOne(ObjectId id);

    void patchOne(ObjectId id, Post post);
}
