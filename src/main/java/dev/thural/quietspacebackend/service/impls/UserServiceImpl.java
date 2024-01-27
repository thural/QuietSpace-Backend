package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.utils.CustomPageProvider;
import dev.thural.quietspacebackend.utils.JwtProvider;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public Page<UserDTO> listUsers(String username, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = CustomPageProvider.buildCustomPageRequest(pageNumber, pageSize);
        Page<UserEntity> userPage;

        if (StringUtils.hasText(username)) {
            userPage = userRepository.findAllByUsernameIsLikeIgnoreCase("%" + username + "%", pageRequest);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }

        return userPage.map(userMapper::userEntityToDto);
    }

    @Override
    public Page<UserDTO> listUsersByQuery(String query, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = CustomPageProvider.buildCustomPageRequest(pageNumber, pageSize);
        Page<UserEntity> userPage;

        if (StringUtils.hasText(query)) {
            userPage = userRepository.findAllByQuery(query, pageRequest);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }

        return userPage.map(userMapper::userEntityToDto);
    }

    @Override
    public Optional<UserEntity> findUserByJwt(String authHeader) {

        String email = JwtProvider.extractEmailFromAuthHeader(authHeader);

        UserEntity userEntity = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        return Optional.of(userEntity);
    }

    @Override
    public Optional<UserDTO> findUserDtoByJwt(String authHeader) {

        UserEntity founUserEntity = findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("user does not exist"));

        return Optional.of(userMapper.userEntityToDto(founUserEntity));
    }

    @Override
    public Optional<UserDTO> getUserById(UUID id) {

        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        UserDTO userDTO = userMapper.userEntityToDto(userEntity);
        return Optional.of(userDTO);
    }

    @Override
    public Optional<UserDTO> updateUser(UserEntity loggedUser, UserDTO userDTO) {

        loggedUser.setUsername(userDTO.getUsername());
        loggedUser.setEmail(userDTO.getEmail());
        loggedUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        UserEntity updatedUser = userRepository.save(loggedUser);

        return Optional.of(userMapper.userEntityToDto(updatedUser));
    }

    @Override
    public Boolean deleteUser(UUID userId, String authHeader) {

        UserEntity loggedUser = findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("user does not exist"));

        if (loggedUser.getRole().equals("admin")){
            userRepository.deleteById(userId);
            return true;
        }

        if (loggedUser.getId().equals(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    @Override
    public void patchUser(UserDTO userDTO, String authHeader) {

        UserEntity loggedUserEntity = findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("user does not exist"));

        if (StringUtils.hasText(userDTO.getUsername()))
            loggedUserEntity.setUsername(userDTO.getUsername());

        if (StringUtils.hasText(userDTO.getEmail()))
            loggedUserEntity.setEmail(userDTO.getEmail());

        if (StringUtils.hasText(userDTO.getPassword()))
            loggedUserEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        userRepository.save(loggedUserEntity);
    }

}