package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PostRepository postRepository;

    private final User user = User.builder()
            .email("user@email.com")
            .username("user")
            .firstname("firstname")
            .lastname("lastname")
            .password("78921731")
            .accountLocked(false)
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

    private User savedUser;
    private Post savedPost;

    @BeforeEach
    void setUp() {
        this.savedUser = userRepository.save(user);
        this.savedPost = postRepository.save(post);
    }

    @AfterEach
    void tearDown() {
        userRepository.delete(user);
        postRepository.delete(post);
    }

    @Test
    void testGetPostsByUserId() {
        Page<Post> list = postRepository.findAllByUserId(user.getId(), null);

        assertThat(list.toList().size()).isEqualTo(1);
        assertThat(list.toList().get(0)).isEqualTo(savedPost);
    }

    @Test
    void testFindAllByQuery() {
        Page<Post> list = postRepository.findAllByQuery("sample", null);

        assertThat(list.toList().size()).isEqualTo(1);
        assertThat(list.toList().get(0)).isEqualTo(savedPost);
    }
}