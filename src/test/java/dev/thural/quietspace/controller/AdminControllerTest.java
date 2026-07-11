package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminController.class)
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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