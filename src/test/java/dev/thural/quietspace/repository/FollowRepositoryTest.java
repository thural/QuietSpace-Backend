package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Follow;
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
class FollowRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    FollowRepository followRepository;

    private final User user1 = User.builder()
            .email("user1@email.com")
            .username("user1")
            .firstname("firstname1")
            .lastname("lastname1")
            .password("78921731")
            .accountLocked(false)
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private final User user2 = User.builder()
            .email("user2@email.com")
            .username("user2")
            .firstname("firstname2")
            .lastname("lastname2")
            .password("78921733")
            .accountLocked(false)
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private final Follow follow = Follow.builder()
            .following(user1)
            .follower(user2)
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private User savedUser;
    private Follow savedFollow;

    @BeforeEach
    void setUp() {
        this.savedUser = userRepository.save(user1);
        userRepository.save(user2);
        this.savedFollow = followRepository.save(follow);
    }

    @AfterEach
    void tearDown() {
        this.userRepository.delete(user1);
        this.userRepository.delete(user2);
        this.followRepository.delete(follow);
    }

    @Test
    void findAllByFollowerId() {
        Page<Follow> followPage = followRepository.findAllByFollowerId(user2.getId(), null);
        assertThat(followPage).isNotNull();
        assertThat(followPage.toList().get(0)).isEqualTo(savedFollow);
    }

    @Test
    void findAllByFollowingId() {
        Page<Follow> followPage = followRepository.findAllByFollowingId(user1.getId(), null);
        assertThat(followPage).isNotNull();
        assertThat(followPage.toList().get(0)).isEqualTo(savedFollow);
    }

    @Test
    void existsByFollowerIdAndFollowingId() {
        Boolean isExists = followRepository.existsByFollowerIdAndFollowingId(user2.getId(), user1.getId());
        assertThat(isExists).isTrue();
    }

    @Test
    void deleteByFollowerIdAndFollowingId() {
        followRepository.deleteByFollowerIdAndFollowingId(user1.getId(), user2.getId());
        Boolean isExists = followRepository.existsByFollowerIdAndFollowingId(user1.getId(), user2.getId());
        assertThat(isExists).isFalse();
    }
}