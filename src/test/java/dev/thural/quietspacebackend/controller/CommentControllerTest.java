package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.Comment;
import dev.thural.quietspacebackend.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CommentController.class)
public class CommentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    CommentService commentService;

    @Autowired
    CommentService commentServiceImpl;

    @Test
    void getAllComments() throws Exception {
        List<Comment> testComments = commentServiceImpl.getAll();

        given(commentService.getAll())
                .willReturn(commentServiceImpl.getAll());

        mockMvc.perform(get("/api/v1/comments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(testComments.size())));
    }

    @Test
    void getCommentById() throws Exception {

        Comment testComment = commentServiceImpl.getAll().get(0);

        given(commentService.getById(testComment.getId()))
                .willReturn(Optional.of(testComment));

        mockMvc.perform(get("/api/v1/comments" + "/" + testComment.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testComment.getId().toString())))
                .andExpect(jsonPath("$.text", is(testComment.getText())));
    }
}
