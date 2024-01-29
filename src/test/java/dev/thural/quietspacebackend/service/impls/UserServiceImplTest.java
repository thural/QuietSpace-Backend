package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.UserDto;
import dev.thural.quietspacebackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    UserServiceImpl userService;

    UUID userId = UUID.fromString("e18d0c0c-37a4-4e50-8041-bd49ffde8182");

    @Test
    void getUserById() {
        userService.getUserById(userId);

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findByIdTest(){
        UserEntity user = new UserEntity();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional <UserDto> foundUser = userService.getUserById(userId);
        assertThat(foundUser).isNotNull();

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getByUUID(){
        userService.getUserById(userId);

        verify(userRepository).findById(any(UUID.class));
    }

}