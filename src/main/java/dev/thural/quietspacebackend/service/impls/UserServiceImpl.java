package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.config.JwtProvider;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.response.AuthResponse;
import dev.thural.quietspacebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class UserServiceImpl implements UserService {
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    UserRepository userRepository;

    private final static Integer DEFAULT_PAGE = 0;
    private final static Integer DEFAULT_PAGE_SIZE = 25;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<UserDTO> listUsers(String userName, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        Page<UserEntity> userPage;

        if (StringUtils.hasText(userName)) {
            userPage = listUsersByName(userName);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }

        return userPage.map(userMapper::userEntityToDto);
    }

    public PageRequest buildPageRequest(Integer pageNumber, Integer pageSize) {
        Integer queryPageNumber;
        Integer queryPageSize;

        if (pageNumber != null && pageNumber > 0) queryPageNumber = pageNumber - 1;
        else queryPageNumber = DEFAULT_PAGE;

        if (pageSize == null) queryPageSize = DEFAULT_PAGE_SIZE;
        else queryPageSize = pageSize > 1000 ? 1000 : pageSize;

        Sort sort = Sort.by(Sort.Order.asc("userName"));

        return PageRequest.of(queryPageNumber, queryPageSize, sort);
    }

    public Page<UserEntity> listUsersByName(String userName) {
        return userRepository.findAllByUsernameIsLikeIgnoreCase("%" + userName + "%", null);
    }

    @Override
    public AuthResponse addOne(UserDTO user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        UserEntity userEntity = userMapper.userDtoToEntity(user);
        UserDTO savedUser = userMapper.userEntityToDto(userRepository.save(userEntity));

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
        String token = JwtProvider.generatedToken(authentication);
        return new AuthResponse(token, "Register Success", savedUser.getId().toString());
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

            UserEntity updatedUser = userRepository.save(userMapper.userDtoToEntity(userDTO));
            atomicReference.set(Optional.of(userMapper.userEntityToDto(updatedUser)));
        }, () -> atomicReference.set(Optional.empty()));

        return atomicReference.get();
    }

    @Override
    public Boolean deleteOne(UUID id) {
        if (userRepository.existsById(id)) {
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
        userRepository.save(userMapper.userDtoToEntity(foundUser));
    }

    @Override
    public Page<UserDTO> listUsersByQuery(String query, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        Page<UserEntity> userPage;

        if (StringUtils.hasText(query)) {
            userPage = userRepository.findAllByQuery(query, pageRequest);
        } else {
            userPage = Page.empty();
        }

        return userPage.map(userMapper::userEntityToDto);
    }

}