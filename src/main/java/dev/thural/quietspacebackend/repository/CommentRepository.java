package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.model.Comment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, ObjectId> {
}
