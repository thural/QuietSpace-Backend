package dev.thural.quietspacebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspacebackend.model.Comment;
import dev.thural.quietspacebackend.repository.CommentRepository;
import dev.thural.quietspacebackend.service.CommentService;
import dev.thural.quietspacebackend.service.CommentServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CommentController.class)
public class CommentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    CommentService commentService;

    @Autowired
    ObjectMapper objectMapper;

    CommentService commentServiceImpl;

    @Autowired
    CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        commentServiceImpl = new CommentServiceImpl(commentRepository);
    }

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

    @Test
    void createComment() throws Exception {
        List<Comment> testComments = commentServiceImpl.getAll();
        Comment testComment = testComments.get(0);
        testComment.setText("testText");

        given(commentService.addOne(any(Comment.class))).willReturn(testComments.get(1));

        mockMvc.perform(post("/api/v1/comments")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testComment)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void updateComment() throws Exception {
        List<Comment> testComments = commentServiceImpl.getAll();

        Comment testComment = testComments.get(0);
        testComment.setText("testText");

        mockMvc.perform(put("/api/v1/comments")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testComment)))
                .andExpect(status().isNoContent());

        verify(commentService).updateOne(any(ObjectId.class), any(Comment.class));
    }
}
