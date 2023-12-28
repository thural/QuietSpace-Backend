package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.controller.NotFoundException;
import dev.thural.quietspacebackend.utils.JwtProvider;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.request.LoginRequest;
import dev.thural.quietspacebackend.response.AuthResponse;
import dev.thural.quietspacebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspacebackend.utils.CustomPageProvider.buildCustomPageRequest;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public Page<UserDTO> listUsers(String username, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);

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
        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);

        Page<UserEntity> userPage;

        if (StringUtils.hasText(query)) {
            userPage = userRepository.findAllByQuery(query, pageRequest);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }

        return userPage.map(userMapper::userEntityToDto);
    }

    @Override
    public AuthResponse addOne(UserDTO userDTO) {
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        UserEntity userEntity = userMapper.userDtoToEntity(userDTO);
        UserDTO savedUser = userMapper.userEntityToDto(userRepository.save(userEntity));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDTO.getEmail(), userDTO.getPassword());
        String token = JwtProvider.generatedToken(authentication);
        return new AuthResponse(token, "register success", savedUser.getId().toString());
    }

    @Override
    public AuthResponse getByLoginRequest(LoginRequest loginRequest) {
        Authentication authentication = authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        String token = JwtProvider.generatedToken(authentication);

        Optional<UserEntity> optionalUser = userRepository.findUserEntityByEmail(loginRequest.getEmail());
        String userId = optionalUser.isPresent() ? optionalUser.get().getId().toString() : "null";
        return new AuthResponse(token, "login success", userId);
    }

    @Override
    public Optional<UserEntity> findUserByJwt(String jwt) {
        String email = JwtProvider.getEmailFromJwtToken(jwt);
        UserEntity userEntity = userRepository.findUserEntityByEmail(email).orElseThrow(NotFoundException::new);
        return Optional.of(userEntity);
    }

    @Override
    public Optional<UserDTO> findUserDtoByJwt(String jwt) {
        UserEntity founUserEntity = findUserByJwt(jwt).orElseThrow(NotFoundException::new);
        return Optional.of(userMapper.userEntityToDto(founUserEntity));
    }

    Authentication authenticate(String email, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (userDetails == null)
            throw new BadCredentialsException("invalid username");

        if (!passwordEncoder.matches(password, userDetails.getPassword()))
            throw new BadCredentialsException("invalid password");

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
    }

    @Override
    public Optional<UserDTO> getById(UUID id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(NotFoundException::new);
        UserDTO userDTO = userMapper.userEntityToDto(userEntity);
        return Optional.of(userDTO);
    }

    @Override
    public Optional<UserDTO> updateOne(UserEntity loggedUser, UserDTO userDTO) {

        loggedUser.setUsername(userDTO.getUsername());
        loggedUser.setEmail(userDTO.getEmail());
        loggedUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        UserEntity updatedUser = userRepository.save(loggedUser);

        return Optional.of(userMapper.userEntityToDto(updatedUser));
    }

    @Override
    public Boolean deleteOne(UUID userId, String jwtToken) {

        String loggedUserEmail = JwtProvider.getEmailFromJwtToken(jwtToken);
        UserEntity loggedUser = userRepository.findUserEntityByEmail(loggedUserEmail)
                .orElseThrow(NotFoundException::new);

        if (loggedUser != null) {
            userRepository.deleteById(userId);
            return true;
        }

        return false;
    }

    @Override
    public void patchOne(UserDTO userDTO, String jwtToken) {
        UserEntity loggedUserEntity = findUserByJwt(jwtToken).orElseThrow(NotFoundException::new);

        if (StringUtils.hasText(userDTO.getUsername()))
            loggedUserEntity.setUsername(userDTO.getUsername());
        if (StringUtils.hasText(userDTO.getEmail()))
            loggedUserEntity.setEmail(userDTO.getEmail());
        if (StringUtils.hasText(userDTO.getPassword()))
            loggedUserEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        userRepository.save(loggedUserEntity);
    }

}