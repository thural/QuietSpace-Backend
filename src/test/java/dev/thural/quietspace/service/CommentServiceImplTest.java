package dev.thural.quietspace.service;

import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.CommentMapper;
import dev.thural.quietspace.model.request.CommentRequest;
import dev.thural.quietspace.model.response.CommentResponse;
import dev.thural.quietspace.repository.CommentRepository;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.service.impl.CommentServiceImpl;
import dev.thural.quietspace.utils.PagingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.BY_CREATED_DATE_ASC;
import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentMapper commentMapper;
    @Mock
    private UserService userService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private UUID userId;
    private User user;
    private Comment comment;
    private CommentResponse commentResponse;
    private CommentRequest commentRequest;
    private Post post;

    @BeforeEach
    void setUp() {
        this.userId = UUID.randomUUID();

        this.user = User.builder()
                .id(userId)
                .username("user")
                .email("user@email.com")
                .password("pAsSword")
                .build();

        this.post = Post.builder()
                .id(UUID.randomUUID())
                .user(user)
                .text("sample text")
                .build();

        this.comment = Comment.builder()
                .id(UUID.randomUUID())
                .parentId(UUID.randomUUID())
                .user(user)
                .post(post)
                .text("sample text")
                .build();

        this.commentRequest = CommentRequest.builder()
                .userId(user.getId())
                .text("sample text")
                .postId(post.getId())
                .build();

        this.commentResponse = CommentResponse.builder()
                .id(UUID.randomUUID())
                .text("sample text")
                .postId(post.getId())
                .username(user.getUsername())
                .userId(user.getId())
                .build();
    }

    @Test
    void testGetCommentsByPost() {
        PageRequest pageRequest = PagingProvider.buildPageRequest(1, 50, BY_CREATED_DATE_ASC);

        when(commentRepository.findAllByPostId(post.getId(), pageRequest)).thenReturn(Page.empty());

        Page<CommentResponse> commentPage = commentService.getCommentsByPostId(post.getId(), 1, 50);
        assertThat(commentPage).isEqualTo(Page.empty());
        verify(commentRepository, times(1)).findAllByPostId(post.getId(), pageRequest);
    }

    @Test
    void testGetCommentsByUser() {
        PageRequest pageRequest = PagingProvider.buildPageRequest(1, 50, null);
        when(commentRepository.findAllByUserId(userId, pageRequest)).thenReturn(Page.empty());
        when(userService.getSignedUser()).thenReturn(user);

        Page<CommentResponse> commentPage = commentService.getCommentsByUserId(userId, 1, 50);

        assertThat(commentPage).isEqualTo(Page.empty());
        verify(commentRepository, times(1)).findAllByUserId(user.getId(), pageRequest);
    }

    @Test
    void testCreateComment() {
        when(userService.getSignedUser()).thenReturn(user);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(postRepository.findById(comment.getPost().getId())).thenReturn(Optional.of(post));
        when(commentMapper.commentRequestToEntity(commentRequest)).thenReturn(comment);
        when(commentMapper.commentEntityToResponse(comment)).thenReturn(commentResponse);

        CommentResponse savedComment = commentService.createComment(commentRequest);

        assertThat(savedComment).isEqualTo(commentResponse);
        verify(commentRepository, times(1)).save(comment);
        verify(postRepository, times(1)).findById(comment.getPost().getId());
        verify(commentMapper, times(1)).commentRequestToEntity(commentRequest);
    }

    @Test
    void testGetCommentById() {
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(commentMapper.commentEntityToResponse(comment)).thenReturn(commentResponse);

        Optional<CommentResponse> foundComment = commentService.getCommentById(comment.getId());

        assertThat(foundComment).isNotEmpty();
        assertThat(foundComment.get()).isEqualTo(commentResponse);
        verify(commentRepository, times(1)).findById(comment.getId());
    }

    @Test
    void testUpdateComment() {
        when(userService.getSignedUser()).thenReturn(user);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(commentMapper.commentEntityToResponse(any(Comment.class))).thenReturn(commentResponse);

        CommentResponse savedComment = commentService.updateComment(comment.getId(), commentRequest);

        assertThat(savedComment).isEqualTo(commentResponse);
        verify(commentRepository, times(1)).findById(comment.getId());
    }

    @Test
    void testDeleteComment() {
        when(userService.getSignedUser()).thenReturn(user);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        commentService.deleteComment(comment.getId());

        verify(commentRepository, times(1)).findById(comment.getId());
        verify(commentRepository, times(1)).deleteById(comment.getId());
        verify(commentRepository, times(1)).deleteAllByParentId(comment.getParentId());
    }

    @Test
    void getRepliesByParentId() {
        PageRequest pageRequest = buildPageRequest(1, 50, null);
        when(commentRepository.findAllByParentId(comment.getId(), pageRequest)).thenReturn(Page.empty());

        Page<CommentResponse> commentPage = commentService.getRepliesByParentId(comment.getId(), 1, 50);

        assertThat(commentPage).isEqualTo(Page.empty());
        verify(commentRepository, times(1)).findAllByParentId(comment.getId(), pageRequest);
    }

    @Test
    void testPatchComment() {
        when(userService.getSignedUser()).thenReturn(user);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(commentMapper.commentEntityToResponse(comment)).thenReturn(commentResponse);

        CommentResponse savedComment = commentService.patchComment(comment.getId(), commentRequest);

        assertThat(savedComment).isEqualTo(commentResponse);
        verify(commentRepository, times(1)).findById(comment.getId());
    }

}