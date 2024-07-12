package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.Reaction;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.utils.enums.ContentType;
import dev.thural.quietspace.utils.enums.LikeType;
import dev.thural.quietspace.utils.enums.RoleType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReactionRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    ReactionRepository reactionRepository;

    private final User user = User.builder()
            .email("user@email.com")
            .role(RoleType.USER.toString())
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

    private Reaction reaction = Reaction.builder()
            .userId(null)
            .contentId(null)
            .contentType(ContentType.POST)
            .likeType(LikeType.LIKE)
            .username(user.getUsername())
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private User savedUser;
    private Post savedPost;
    private Reaction savedReaction;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(user);
        savedPost = postRepository.save(post);
        reaction.setContentId(savedPost.getId());
        reaction.setUserId(savedUser.getId());
        savedReaction = reactionRepository.save(reaction);
    }

    @AfterEach
    void tearDown() {
        userRepository.delete(savedUser);
        postRepository.delete(savedPost);
        reactionRepository.delete(savedReaction);
    }

    @Test
    void findAllByContentId() {
        List<Reaction> reactions = reactionRepository.findAllByContentId(savedReaction.getContentId());
        assertThat(reactions.size()).isEqualTo(1);
        assertThat(reactions.get(0)).isEqualTo(savedReaction);
    }

    @Test
    void findAllByUserId() {
        List<Reaction> reactions = reactionRepository.findAllByUserId(savedUser.getId());
        assertThat(reactions.size()).isEqualTo(1);
        assertThat(reactions.get(0)).isEqualTo(savedReaction);
    }

    @Test
    void existsByContentIdAndUserId() {
        boolean exists = reactionRepository.existsByContentIdAndUserId(post.getId(), user.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void findByContentIdAndUserId() {
        Optional<Reaction> reaction = reactionRepository.findByContentIdAndUserId(post.getId(), user.getId());
        assertThat(reaction.isPresent()).isTrue();
        assertThat(reaction.get()).isEqualTo(savedReaction);
    }

    @Test
    void findAllByContentTypeAndUserId() {
        List<Reaction> reactions = reactionRepository.findAllByContentTypeAndUserId(ContentType.POST, savedUser.getId());
        assertThat(reactions.size()).isEqualTo(1);
        assertThat(reactions.get(0)).isEqualTo(savedReaction);
    }

    @Test
    void findAllByContentIdAndContentType() {
        List<Reaction> reactions = reactionRepository.findAllByContentId(savedReaction.getContentId());
        assertThat(reactions.size()).isEqualTo(1);
        assertThat(reactions.get(0)).isEqualTo(savedReaction);
    }

    @Test
    void findAllByUserIdAndContentType() {
        List<Reaction> reactions = reactionRepository.findAllByUserIdAndContentType(savedUser.getId(), ContentType.POST);
        assertThat(reactions.size()).isEqualTo(1);
        assertThat(reactions.get(0)).isEqualTo(savedReaction);
    }

    @Test
    void countByContentIdAndLikeType() {
        long count = reactionRepository.countByContentIdAndLikeType(post.getId(), LikeType.LIKE);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void findAllByContentIdAndLikeType() {
        List<Reaction> reactions = reactionRepository.findAllByContentIdAndLikeType(post.getId(), LikeType.LIKE);
        assertThat(reactions.size()).isEqualTo(1);
        assertThat(reactions.get(0)).isEqualTo(savedReaction);
    }
}