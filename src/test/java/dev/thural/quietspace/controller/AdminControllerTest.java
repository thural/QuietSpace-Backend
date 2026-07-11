package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    void sayHello_shouldReturn200WithMessage() throws Exception {
        mockMvc.perform(get("/api/v1/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello admin"));
    }

    @Test
    void deleteUserById_shouldReturn204() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/{userId}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getPagedUsers_shouldReturn200WithPage() throws Exception {
        when(userService.listUsers(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(new UserResponse())));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
