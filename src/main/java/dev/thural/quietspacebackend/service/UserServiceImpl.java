package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDTO> getAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDTO addOne(UserDTO user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<UserDTO> getById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public void updateOne(UUID id, UserDTO user) {
        Optional<UserDTO> optionalUser = userRepository.findById(id);
        UserDTO foundUser = optionalUser.get();
        foundUser.setUsername(user.getUsername());
        foundUser.setPassword(user.getPassword());
        foundUser.setFriendIds(user.getFriendIds());
        userRepository.save(foundUser);
    }

    @Override
    public void deleteOne(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    public void patchOne(UUID id, UserDTO user) {
        Optional<UserDTO> optionalUser = userRepository.findById(id);
        UserDTO foundUser = optionalUser.get();
        if (StringUtils.hasText(user.getUsername()))
            foundUser.setUsername(user.getUsername());
        if (StringUtils.hasText(user.getPassword()))
            foundUser.setPassword(user.getPassword());
        if (user.getFriendIds() != null)
            foundUser.setFriendIds(user.getFriendIds());
        userRepository.save(foundUser);
    }

}