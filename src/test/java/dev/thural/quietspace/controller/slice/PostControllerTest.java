package dev.thural.quietspace.controller.slice;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.controller.PostController;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.request.VoteRequest;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.security.JwtService;
import dev.thural.quietspace.service.PostService;
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
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
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
@WebMvcTest(controllers = PostController.class)
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;
    @MockBean
    PostService postService;
    @MockBean
    ReactionService reactionService;
    @MockBean
    JwtService jwtService;
    @MockBean
    TokenRepository tokenRepository;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;
    @Captor
    ArgumentCaptor<PostRequest> postRequestArgumentCaptor;

    private PostRequest postRequest;
    private VoteRequest voteRequest;
    private PostResponse postResponse;

    @BeforeEach
    public void setUp() {
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .username("user")
                .email("user@email.com")
                .password("pAsSword")
                .build();

        Post post = Post.builder()
                .id(UUID.randomUUID())
                .user(user)
                .text("sample text")
                .build();

        this.postRequest = PostRequest.builder()
                .userId(user.getId())
                .text("sample text")
                .title("sample title")
                .poll(null)
                .build();

        this.postResponse = PostResponse.builder()
                .id(post.getId())
                .text(post.getText())
                .username(post.getUser().getUsername())
                .title(post.getTitle())
                .build();

        this.voteRequest = VoteRequest.builder()
                .postId(post.getId())
                .userId(UUID.randomUUID())
                .build();
    }

    @Test
    void getAllPosts() throws Exception {
        when(postService.getAllPosts(1, 10)).thenReturn(Page.empty());

        mockMvc.perform(get(PostController.POST_PATH)
                        .param("page-number", "1")
                        .param("page-size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(postService, times(1)).getAllPosts(1, 10);

    }

    @Test
    void getPostsByQuery() throws Exception {
        when(postService.getAllByQuery(any(), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get(PostController.POST_PATH + "/search")
                        .param("page-number", "1")
                        .param("page-size", "10")
                        .param("query", "sample")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(postService).getAllByQuery("sample", 1, 10);
    }

    @Test
    void createPost() throws Exception {
        when(postService.addPost(any(PostRequest.class))).thenReturn(postResponse);

        mockMvc.perform(post(PostController.POST_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(jsonPath("$.text", is(postResponse.getText())))
                .andExpect(jsonPath("$.username", is(postResponse.getUsername())))
                .andExpect(jsonPath("$.title", is(postResponse.getTitle())))
                .andExpect(jsonPath("$.id", is(postResponse.getId().toString())))
                .andExpect(jsonPath("$.poll", is(postResponse.getPoll())))
                .andExpect(status().isOk());
    }

    @Test
    void getPostById() throws Exception {
        when(postService.getPostById(any())).thenReturn(Optional.of(postResponse));

        mockMvc.perform(get(PostController.POST_PATH + "/" + postResponse.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(postService).getPostById(uuidArgumentCaptor.capture());
        assertThat(postResponse.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void putPost() throws Exception {
        when(postService.updatePost(any(), any())).thenReturn(postResponse);

        mockMvc.perform(put(PostController.POST_PATH + "/" + postResponse.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(jsonPath("$.text", is(postResponse.getText())))
                .andExpect(jsonPath("$.username", is(postResponse.getUsername())))
                .andExpect(jsonPath("$.title", is(postResponse.getTitle())))
                .andExpect(jsonPath("$.id", is(postResponse.getId().toString())))
                .andExpect(jsonPath("$.poll", is(postResponse.getPoll())))
                .andExpect(status().isOk());

        verify(postService, times(1)).updatePost(uuidArgumentCaptor.capture(), postRequestArgumentCaptor.capture());
        assertThat(postResponse.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(postResponse.getText()).isEqualTo(postRequestArgumentCaptor.getValue().getText());
    }

    @Test
    void deletePost() throws Exception {
        mockMvc.perform(delete(PostController.POST_PATH + "/" + postResponse.getId()))
                .andExpect(status().isNoContent());

        verify(postService, times(1)).deletePost(uuidArgumentCaptor.capture());
        assertThat(postResponse.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void patchPost() throws Exception {
        when(postService.patchPost(any(), any())).thenReturn(postResponse);

        mockMvc.perform(patch(PostController.POST_PATH + "/" + postResponse.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(jsonPath("$.text", is(postResponse.getText())))
                .andExpect(jsonPath("$.username", is(postResponse.getUsername())))
                .andExpect(jsonPath("$.title", is(postResponse.getTitle())))
                .andExpect(jsonPath("$.id", is(postResponse.getId().toString())))
                .andExpect(jsonPath("$.poll", is(postResponse.getPoll())))
                .andExpect(status().isOk());

        verify(postService).patchPost(uuidArgumentCaptor.capture(), postRequestArgumentCaptor.capture());
        assertThat(postResponse.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(postResponse.getText()).isEqualTo(postRequestArgumentCaptor.getValue().getText());
    }

    @Test
    void votePoll() throws Exception {
        ArgumentCaptor<VoteRequest> voteRequestArgumentCaptor = ArgumentCaptor.forClass(VoteRequest.class);

        mockMvc.perform(post(PostController.POST_PATH + "/" + "vote-poll")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isOk());

        verify(postService).votePoll(voteRequestArgumentCaptor.capture());
        assertThat(voteRequestArgumentCaptor.getValue().getPostId()).isEqualTo(voteRequest.getPostId());
    }
}