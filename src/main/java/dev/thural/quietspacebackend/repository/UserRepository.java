package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, ObjectId> {
}
