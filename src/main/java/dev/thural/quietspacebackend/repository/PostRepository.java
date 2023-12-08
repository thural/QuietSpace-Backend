package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.model.Post;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<Post, ObjectId> {
}