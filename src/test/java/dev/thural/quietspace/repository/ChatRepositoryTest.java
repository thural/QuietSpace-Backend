package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;

    private final User user = User.builder()
            .email("user@email.com")
            .username("user")
            .firstname("firstname")
            .lastname("lastname")
            .password("78921731")
            .accountLocked(false)
            .username("test user")
            .role(Role.USER)
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private final Chat chat = Chat.builder()
            .users(List.of(user))
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private User savedUser;
    private Chat savedChat;

    @BeforeEach
    void setUp() {
        this.savedUser = userRepository.save(user);
        this.savedChat = chatRepository.save(chat);
    }

    @AfterEach
    void tearDown() {
        this.userRepository.delete(user);
        this.chatRepository.delete(savedChat);
    }

    @Test
    void findAllByUsersId() {
        List<Chat> chats = chatRepository.findAllByUsersId(savedUser.getId());
        assertThat(chats.size()).isEqualTo(1);
        assertThat(chats.get(0)).isEqualTo(chat);
    }

    @Test
    void findAllByUsersIn() {
        List<Chat> chats = chatRepository.findAllByUsersIn(List.of(savedUser));
        assertThat(chats.size()).isEqualTo(1);
        assertThat(chats.get(0)).isEqualTo(chat);
    }
}