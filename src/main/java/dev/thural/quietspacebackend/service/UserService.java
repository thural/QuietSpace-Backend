package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.User;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAll();

    User addOne(User user);

    Optional<User> getById(ObjectId id);

    void updateOne(ObjectId id, User user);

    void deleteOne(ObjectId id);

    void patchOne(ObjectId id, User user);
}
