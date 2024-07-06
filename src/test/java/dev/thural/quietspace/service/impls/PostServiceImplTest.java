package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Poll;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.custom.PostMapper;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.entity.PollOption;
import dev.thural.quietspace.model.request.VoteRequest;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.service.UserService;
import dev.thural.quietspace.utils.PagingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {

    @Mock
    private PostMapper postMapper;
    @Mock
    private UserService userService;
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private User user;
    private Post post;
    private Poll poll;
    private PostRequest postRequest;
    private VoteRequest voteRequest;
    private PostResponse postResponse;

    @BeforeEach
    public void setUp() {
        UUID userId = UUID.randomUUID();

        this.user = User.builder()
                .id(userId)
                .username("user")
                .email("user@email.com")
                .role("admin")
                .password("pAsSword")
                .build();

        this.post = Post.builder()
                .id(UUID.randomUUID())
                .user(user)
                .text("sample text")
                .poll(poll)
                .build();

        Set<UUID> userIds = new HashSet<>();
        userIds.add(userId);

        PollOption option = PollOption.builder()
                .label("sample label")
                .poll(poll)
                .votes(userIds)
                .build();

        this.poll = Poll.builder()
                .options(List.of(option))
                .build();

        this.post.setPoll(this.poll);

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
    void testGetAllPosts() {
        PageRequest pageRequest = PagingProvider.buildPageRequest(1, 50, null);
        when(postRepository.findAll(pageRequest)).thenReturn(Page.empty());

        Page<PostResponse> posts = postService.getAllPosts(1, 50);

        assertThat(posts.getContent()).isEmpty();
        verify(postRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void testGetPostByUserId() {
        PageRequest pageRequest = PagingProvider.buildPageRequest(1, 50, null);
        when(postRepository.findAllByUserId(user.getId(), pageRequest)).thenReturn(Page.empty());

        Page<PostResponse> posts = postService.getPostsByUserId(user.getId(), 1, 50);
        assertThat(posts.getContent()).isEmpty();

        verify(postRepository, times(1)).findAllByUserId(user.getId(), pageRequest);
    }

    @Test
    void testAddPostById() {
        when(userService.getLoggedUser()).thenReturn(user);
        when(postMapper.postRequestToEntity(any(PostRequest.class))).thenReturn(post);
        when(postMapper.postEntityToResponse(post)).thenReturn(postResponse);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponse postResponse = postService.addPost(postRequest);
        assertThat(postResponse).isInstanceOf(PostResponse.class);

        verify(postRepository, times(1)).save(post);
    }

    @Test
    void testGetVotedOptionLabel() {
        when(userService.getLoggedUser()).thenReturn(user);

        String optionLabel = postService.getVotedPollOptionLabel(poll);

        assertThat(optionLabel).isEqualTo("sample label");
    }

    @Test
    void testGetPostResponseById() {
        when(postRepository.findById(post.getId())).thenReturn(Optional.ofNullable(post));
        when(postMapper.postEntityToResponse(post)).thenReturn(postResponse);

        PostResponse foundPost = postService.getPostById(post.getId()).orElse(null);
        assertThat(foundPost).isEqualTo(postResponse);

        verify(postRepository, times(1)).findById(post.getId());
    }

    @Test
    void testUpdatePost() {
        when(userService.getLoggedUser()).thenReturn(user);
        when(postRepository.findById(post.getId())).thenReturn(Optional.ofNullable(post));
        when(postMapper.postEntityToResponse(post)).thenReturn(postResponse);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponse responseBody = postService.updatePost(post.getId(), postRequest);
        assertThat(responseBody).isEqualTo(postResponse);

        verify(postRepository, times(1)).findById(post.getId());
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void testVotePoll() {
        when(postRepository.findById(post.getId())).thenReturn(Optional.ofNullable(post));

        postService.votePoll(voteRequest);

        verify(postRepository, times(1)).findById(post.getId());
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void testDeletePost() {
        when(userService.getLoggedUser()).thenReturn(user);
        when(postRepository.findById(post.getId())).thenReturn(Optional.ofNullable(post));

        postService.deletePost(post.getId());

        verify(postRepository, times(1)).findById(post.getId());
        verify(postRepository, times(1)).deleteById(post.getId());
    }

}
