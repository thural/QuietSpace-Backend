package dev.thural.quietspace.controller.slice;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.controller.CommentController;
import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.enums.Role;
import dev.thural.quietspace.mapper.CommentMapper;
import dev.thural.quietspace.model.request.CommentRequest;
import dev.thural.quietspace.model.response.CommentResponse;
import dev.thural.quietspace.repository.CommentRepository;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.security.JwtService;
import dev.thural.quietspace.service.CommentService;
import dev.thural.quietspace.service.NotificationService;
import dev.thural.quietspace.service.ReactionService;
import dev.thural.quietspace.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CommentService commentService;
    @MockBean
    CommentMapper commentMapper;
    @MockBean
    NotificationService notificationService;
    @MockBean
    UserService userService;
    @MockBean
    CommentRepository commentRepository;
    @MockBean
    PostRepository postRepository;
    @MockBean
    ReactionService reactionService;
    @MockBean
    JwtService jwtService;
    @MockBean
    TokenRepository tokenRepository;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;
    @Captor
    ArgumentCaptor<CommentRequest> commentRequestCaptor;

    private Comment comment;
    private CommentResponse commentResponse;
    private CommentRequest commentRequest;
    private Post post;
    private User user;

    @BeforeEach
    public void setUp() {

        this.user = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .email("user@email.com")
                .role(Role.ADMIN)
                .password("pAsSword")
                .build();

        this.post = Post.builder()
                .id(UUID.randomUUID())
                .user(user)
                .text("sample text")
                .build();

        this.comment = Comment.builder()
                .id(UUID.randomUUID())
                .text("sample text")
                .post(post)
                .user(user)
                .build();

        this.commentRequest = CommentRequest.builder()
                .postId(post.getId())
                .text("sample text")
                .userId(user.getId())
                .build();

        this.commentResponse = CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .username(comment.getUser().getUsername())
                .userId(user.getId())
                .postId(post.getId())
                .build();
    }

    @Test
    void getCommentsByPostId() throws Exception {

        mockMvc.perform(get(CommentController.COMMENT_PATH + "/post/" + post.getId())
                        .param("page-number", "1")
                        .param("page-size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(commentService, times(1)).getCommentsByPostId(post.getId(), 1, 10);
    }

    @Test
    void getCommentsByUserId() throws Exception {

        mockMvc.perform(get(CommentController.COMMENT_PATH + "/user/" + user.getId())
                        .param("page-number", "1")
                        .param("page-size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(commentService).getCommentsByUserId(user.getId(), 1, 10);
    }

    @Test
    void getCommentRepliesById() throws Exception {

        mockMvc.perform(get(CommentController.COMMENT_PATH + "/" + comment.getId() + "/replies")
                        .param("page-number", "1")
                        .param("page-size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(commentService).getRepliesByParentId(comment.getId(), 1, 10);
    }

    @Test
    void getCommentById() throws Exception {
        when(commentService.getCommentById(comment.getId())).thenReturn(Optional.ofNullable(commentResponse));

        mockMvc.perform(get(CommentController.COMMENT_PATH + "/" + comment.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", is(commentResponse.getPostId().toString())))
                .andExpect(jsonPath("$.userId", is(commentResponse.getUserId().toString())))
                .andExpect(jsonPath("$.text", is(commentResponse.getText())));

        verify(commentService, times(1)).getCommentById(uuidArgumentCaptor.capture());
        assertThat(commentResponse.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    @WithUserDetails
    void createComment() throws Exception {
        when(commentService.createComment(any(CommentRequest.class))).thenReturn(commentResponse);

        mockMvc.perform(post(CommentController.COMMENT_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", is(commentResponse.getPostId().toString())))
                .andExpect(jsonPath("$.userId", is(commentResponse.getUserId().toString())))
                .andExpect(jsonPath("$.text", is(commentResponse.getText())));

        verify(commentService, times(1)).createComment(commentRequestCaptor.capture());
        assertThat(commentRequest.getText()).isEqualTo(commentRequestCaptor.getValue().getText());
    }

    @Test
    void putComment() throws Exception {
        when(commentService.updateComment(any(UUID.class), any(CommentRequest.class))).thenReturn(commentResponse);

        mockMvc.perform(put(CommentController.COMMENT_PATH + "/" + comment.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", is(commentResponse.getPostId().toString())))
                .andExpect(jsonPath("$.userId", is(commentResponse.getUserId().toString())))
                .andExpect(jsonPath("$.text", is(commentResponse.getText())));

        verify(commentService).updateComment(uuidArgumentCaptor.capture(), commentRequestCaptor.capture());
        assertThat(commentRequest.getText()).isEqualTo(commentRequestCaptor.getValue().getText());
        assertThat(commentResponse.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void deleteComment() {
    }

    @Test
    void patchComment() throws Exception {
        ArgumentCaptor<CommentRequest> commentRequestCaptor = ArgumentCaptor.forClass(CommentRequest.class);
        when(commentService.patchComment(any(UUID.class), any(CommentRequest.class))).thenReturn(commentResponse);

        mockMvc.perform(patch(CommentController.COMMENT_PATH + "/" + comment.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", is(commentResponse.getPostId().toString())))
                .andExpect(jsonPath("$.userId", is(commentResponse.getUserId().toString())))
                .andExpect(jsonPath("$.text", is(commentResponse.getText())));

        verify(commentService).patchComment(uuidArgumentCaptor.capture(), commentRequestCaptor.capture());
        assertThat(commentRequest.getText()).isEqualTo(commentRequestCaptor.getValue().getText());
        assertThat(commentResponse.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }
}