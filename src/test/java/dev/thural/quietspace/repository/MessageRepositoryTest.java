package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.Message;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.utils.enums.RoleType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private MessageRepository messageRepository;

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

    private final Chat chat = Chat.builder()
            .users(List.of(user))
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private final Message message = Message.builder()
            .text("sample text")
            .chat(chat)
            .sender(user)
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private Chat savedChat;
    private Message savedMessage;

    @BeforeEach
    void setUp() {
        this.savedChat = chatRepository.save(chat);
        this.savedMessage = messageRepository.save(message);
    }

    @AfterEach
    void tearDown() {
        this.chatRepository.delete(savedChat);
        this.messageRepository.delete(savedMessage);
    }

    @Test
    void findAllByChatId() {
        Page<Message> messagePage = messageRepository.findAllByChatId(chat.getId(), null);
        assertThat(messagePage.toList()).hasSize(1);
        assertThat(messagePage.toList().get(0).getText()).isEqualTo("sample text");
    }

    @Test
    void findFirstByChatOrderByCreateDateDesc() {
        final Message newMessage = Message.builder()
                .text("latest message")
                .chat(chat)
                .sender(user)
                .createDate(OffsetDateTime.now())
                .updateDate(OffsetDateTime.now())
                .build();
        messageRepository.save(newMessage);

        Message latestMessage = messageRepository.findFirstByChatOrderByCreateDateDesc(chat)
                .orElse(null);

        assertThat(latestMessage).isNotNull();
        assertThat(latestMessage.getText()).isEqualTo(newMessage.getText());
    }
}