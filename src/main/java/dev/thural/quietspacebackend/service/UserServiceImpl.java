package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    UserMapper userMapper;

    UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDTO> listUsers(String userName) {

        List <UserEntity> userList;

        if(StringUtils.hasText(userName)){
            userList = listUsersByName(userName);
        } else {
            userList = userRepository.findAll();
        }

        return userList.stream()
                .map(userMapper::userEntityToDto)
                .collect(Collectors.toList());
    }

    public List<UserEntity> listUsersByName(String userName){
        return userRepository.findAllByUsernameIsLikeIgnoreCase("%" + userName + "%");
    }

    @Override
    public UserDTO addOne(UserDTO user) {
        UserEntity userEntity = userMapper.userDtoToEntity(user);
        return userMapper.userEntityToDto(userRepository.save(userEntity));
    }

    @Override
    public Optional<UserDTO> getById(UUID id) {
        UserEntity userEntity = userRepository.findById(id).orElse(null);
        UserDTO userDTO = userMapper.userEntityToDto(userEntity);
        return Optional.ofNullable(userDTO);
    }

    @Override
    public Optional<UserDTO> updateOne(UUID id, UserDTO user) {
        AtomicReference<Optional<UserDTO>> atomicReference = new AtomicReference<>();

        userRepository.findById(id).ifPresentOrElse(foundUser -> {

            UserDTO userDTO = userMapper.userEntityToDto(foundUser);
            userDTO.setUsername(user.getUsername());
            userDTO.setPassword(user.getPassword());
            userDTO.setFriendIds(user.getFriendIds());

            UserEntity updatedUser = userRepository.save(userMapper.userDtoToEntity(userDTO));
            atomicReference.set(Optional.of(userMapper.userEntityToDto(updatedUser)));
        }, () -> atomicReference.set(Optional.empty()));

        return atomicReference.get();
    }

    @Override
    public Boolean deleteOne(UUID id) {
        if(userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public void patchOne(UUID id, UserDTO user) {
        UserEntity userEntity = userRepository.findById(id).orElse(null);
        UserDTO foundUser = userMapper.userEntityToDto(userEntity);
        if (StringUtils.hasText(user.getUsername()))
            foundUser.setUsername(user.getUsername());
        if (StringUtils.hasText(user.getPassword()))
            foundUser.setPassword(user.getPassword());
        if (user.getFriendIds() != null)
            foundUser.setFriendIds(user.getFriendIds());
        userRepository.save(userMapper.userDtoToEntity(foundUser));
    }

}