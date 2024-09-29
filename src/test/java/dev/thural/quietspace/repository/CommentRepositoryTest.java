package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;

    private final User user = User.builder()
            .email("user@email.com")
            .username("user")
            .firstname("firstname")
            .lastname("lastname")
            .password("78921731")
            .accountLocked(false)
            .role(Role.USER)
            .username("test user")
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private final Post post = Post.builder()
            .text("sample text")
            .user(user)
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private final Comment comment = Comment.builder()
            .user(user)
            .post(post)
            .text("sample text")
            .parentId(UUID.randomUUID())
            .build();

    private User savedUser;
    private Post savedPost;
    private Comment savedComment;

    @BeforeEach
    void setUp() {
        this.savedUser = userRepository.save(user);
        this.savedPost = postRepository.save(post);
        this.savedComment = commentRepository.save(comment);
    }

    @AfterEach
    void tearDown() {
        userRepository.delete(user);
        postRepository.delete(post);
        commentRepository.delete(comment);
    }

    @Test
    void findAllByPostId() {
        Page<Comment> commentPage = commentRepository.findAllByPostId(post.getId(), null);
        assertThat(commentPage.toList().size()).isEqualTo(1);
        assertThat(commentPage.toList().get(0)).isEqualTo(savedComment);
    }

    @Test
    void countByParentIdAndPost() {
        Integer commentCount = commentRepository.countByParentIdAndPost(comment.getParentId(), savedPost);
        assertThat(commentCount).isEqualTo(1);
    }

    @Test
    void deleteAllByParentId() {
        commentRepository.deleteAllByParentId(comment.getParentId());
        Integer commentCount = commentRepository.countByParentIdAndPost(comment.getParentId(), savedPost);
        assertThat(commentCount).isEqualTo(0);

    }

    @Test
    void findAllByParentId() {
        Page<Comment> commentPage = commentRepository.findAllByParentId(comment.getParentId(), null);
        assertThat(commentPage.toList().size()).isEqualTo(1);
        assertThat(commentPage.toList().get(0)).isEqualTo(savedComment);
    }

    @Test
    void findAllByUserId() {
        Page<Comment> commentPage = commentRepository.findAllByUserId(user.getId(), null);
        assertThat(commentPage.toList().size()).isEqualTo(1);
        assertThat(commentPage.toList().get(0)).isEqualTo(savedComment);
    }
}