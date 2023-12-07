package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.User;
import dev.thural.quietspacebackend.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    @Autowired
    UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User addOne(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getById(ObjectId id) {
        return userRepository.findById(id);
    }

    @Override
    public void updateOne(ObjectId id, User user) {
        Optional<User> optionalUser = userRepository.findById(id);
        User foundUser = optionalUser.get();
        foundUser.setUsername(user.getUsername());
        foundUser.setPassword(user.getPassword());
        foundUser.setFriendIds(user.getFriendIds());
        userRepository.save(foundUser);
    }
}
