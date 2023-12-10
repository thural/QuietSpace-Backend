package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.User;
import dev.thural.quietspacebackend.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
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

    @Override
    public void deleteOne(ObjectId id) {
        userRepository.deleteById(id);
    }

    @Override
    public void patchOne(ObjectId id, User user) {
        Optional<User> optionalUser = userRepository.findById(id);
        User foundUser = optionalUser.get();
        if (StringUtils.hasText(user.getUsername()))
            foundUser.setUsername(user.getUsername());
        if (StringUtils.hasText(user.getPassword()))
            foundUser.setPassword(user.getPassword());
        if (user.getFriendIds() != null)
            foundUser.setFriendIds(user.getFriendIds());
        userRepository.save(foundUser);
    }

}