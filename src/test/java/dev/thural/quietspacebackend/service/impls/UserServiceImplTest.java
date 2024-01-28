package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void getUserById() {
        UUID id = UUID.fromString("e18d0c0c-37a4-4e50-8041-bd49ffde8182");
        userService.getUserById(id);

        verify(userRepository, times(1)).findById(id);
    }
}