package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.UserRepository;
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

    User user = User.builder()
            .id(userId)
            .username("user")
            .email("user@email.com")
            .role("admin")
            .password("pAsSword")
            .build();

    @Test
    void findByIdTest(){
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional <UserResponse> foundUser = userService.getUserById(userId);
        assertThat(foundUser).isNotNull();

        verify(userRepository, times(1)).findById(userId);
    }

}