package dev.thural.quietspace.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.config.TestcontainersConfig;
import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.PhotoService;
import dev.thural.quietspace.utils.IntegrationTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
@ActiveProfiles("testcontainers")
class WebSocketFlowIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PhotoService photoService;

    private IntegrationTestHelper helper;
    private String user1Jwt;
    private String user2Jwt;
    private UUID user1Id;
    private UUID user2Id;
    private UUID chatId;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        helper = new IntegrationTestHelper(mockMvc, objectMapper, userRepository, passwordEncoder);
        user1Jwt = helper.registerAndLogin("wsuser1@test.com", "password123");
        user1Id = userRepository.findUserEntityByEmail("wsuser1@test.com").orElseThrow().getId();
        user2Jwt = helper.registerAndLogin("wsuser2@test.com", "password456");
        user2Id = userRepository.findUserEntityByEmail("wsuser2@test.com").orElseThrow().getId();

        var createChatReq = dev.thural.quietspace.model.request.CreateChatRequest.builder()
                .isGroupChat(false)
                .recipientId(user2Id)
                .text("Chat for WS test")
                .userIds(List.of(user1Id, user2Id))
                .build();

        String chatResponse = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/chats")
                                .header("Authorization", "Bearer " + user1Jwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createChatReq)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();

        chatId = UUID.fromString(objectMapper.readTree(chatResponse).get("id").asText());
    }

    private StompSession connectStomp(String jwt) throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.set("Authorization", "Bearer " + jwt);

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        return stompClient.connectAsync("ws://localhost:" + port + "/ws", handshakeHeaders, connectHeaders,
                        new StompSessionHandlerAdapter() {})
                .get(10, TimeUnit.SECONDS);
    }

    @Test
    void connectAndReceivePublicMessage_shouldWork() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.set("Authorization", "Bearer " + user1Jwt);

        CompletableFuture<String> receivedMessage = new CompletableFuture<>();

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        stompClient.connectAsync("ws://localhost:" + port + "/ws", handshakeHeaders, connectHeaders,
                        new StompSessionHandlerAdapter() {
                            @Override
                            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                                session.subscribe("/public/chat", new StompFrameHandler() {
                                    @Override
                                    public Type getPayloadType(StompHeaders headers) {
                                        return String.class;
                                    }

                                    @Override
                                    public void handleFrame(StompHeaders headers, Object payload) {
                                        receivedMessage.complete(payload.toString());
                                    }
                                });

                                session.send("/app/public/chat", MessageRequest.builder()
                                        .chatId(UUID.randomUUID())
                                        .senderId(UUID.randomUUID())
                                        .recipientId(UUID.randomUUID())
                                        .text("Hello from WebSocket test!")
                                        .build());
                            }
                        })
                .get(10, TimeUnit.SECONDS);

        String result = receivedMessage.get(10, TimeUnit.SECONDS);
        assertThat(result).contains("Hello from WebSocket test!");
    }

    @Test
    void sendPrivateMessage_shouldDeliver() throws Exception {
        StompSession user1Session = connectStomp(user1Jwt);
        StompSession user2Session = connectStomp(user2Jwt);

        CompletableFuture<String> user2Received = new CompletableFuture<>();

        user2Session.subscribe("/user/private/chat", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                user2Received.complete(payload.toString());
            }
        });

        user1Session.send("/app/private/chat", MessageRequest.builder()
                .chatId(chatId)
                .senderId(user1Id)
                .recipientId(user2Id)
                .text("Private message test")
                .build());

        String result = user2Received.get(10, TimeUnit.SECONDS);
        assertThat(result).contains("Private message test");
    }

    @Test
    void deleteMessage_shouldNotifyParticipants() throws Exception {
        StompSession user1Session = connectStomp(user1Jwt);
        StompSession user2Session = connectStomp(user2Jwt);

        CompletableFuture<String> eventReceived = new CompletableFuture<>();

        user2Session.subscribe("/user/private/chat/event", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                eventReceived.complete(payload.toString());
            }
        });

        CompletableFuture<String> messageSent = new CompletableFuture<>();

        user1Session.subscribe("/user/private/chat", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageSent.complete(payload.toString());
            }
        });

        user1Session.send("/app/private/chat", MessageRequest.builder()
                .chatId(chatId)
                .senderId(user1Id)
                .recipientId(user2Id)
                .text("Message to delete")
                .build());

        String sentJson = messageSent.get(10, TimeUnit.SECONDS);
        String messageId = objectMapper.readTree(sentJson).get("id").asText();

        user1Session.send("/app/private/chat/delete/" + messageId, null);

        String event = eventReceived.get(10, TimeUnit.SECONDS);
        assertThat(event).contains("DELETE_MESSAGE");
    }

    @Test
    void markMessageAsSeen_shouldNotifyParticipants() throws Exception {
        StompSession user1Session = connectStomp(user1Jwt);
        StompSession user2Session = connectStomp(user2Jwt);

        CompletableFuture<String> eventReceived = new CompletableFuture<>();

        user1Session.subscribe("/user/private/chat/event", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                eventReceived.complete(payload.toString());
            }
        });

        CompletableFuture<String> messageSent = new CompletableFuture<>();

        user1Session.subscribe("/user/private/chat", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageSent.complete(payload.toString());
            }
        });

        user1Session.send("/app/private/chat", MessageRequest.builder()
                .chatId(chatId)
                .senderId(user1Id)
                .recipientId(user2Id)
                .text("Message to mark seen")
                .build());

        String sentJson = messageSent.get(10, TimeUnit.SECONDS);
        String messageId = objectMapper.readTree(sentJson).get("id").asText();

        user2Session.send("/app/private/chat/seen/" + messageId, null);

        String event = eventReceived.get(10, TimeUnit.SECONDS);
        assertThat(event).contains("SEEN_MESSAGE");
    }

    @Test
    void leaveChat_shouldNotifyParticipants() throws Exception {
        StompSession user1Session = connectStomp(user1Jwt);
        StompSession user2Session = connectStomp(user2Jwt);

        CompletableFuture<String> notification = new CompletableFuture<>();

        Predicate<String> leavesMatcher = json -> json.contains("LEFT_CHAT") || json.contains("left the chat");

        StompFrameHandler handler = new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                String json = payload.toString();
                if (leavesMatcher.test(json)) {
                    notification.complete(json);
                }
            }
        };

        user2Session.subscribe("/user/private/chat", handler);
        user1Session.subscribe("/user/private/chat/event", handler);

        var chatEvent = dev.thural.quietspace.websocket.event.message.ChatEvent.builder()
                .chatId(chatId)
                .actorId(user1Id)
                .type(dev.thural.quietspace.enums.EventType.LEFT_CHAT)
                .build();

        user1Session.send("/app/private/chat/leave", chatEvent);

        String event = notification.get(10, TimeUnit.SECONDS);
        assertThat(event).contains("LEFT_CHAT");
    }

    @Test
    void joinChat_shouldNotifyParticipants() throws Exception {
        StompSession user1Session = connectStomp(user1Jwt);
        StompSession user2Session = connectStomp(user2Jwt);

        CompletableFuture<String> notification = new CompletableFuture<>();

        Predicate<String> joinedMatcher = json -> json.contains("JOINED_CHAT") || json.contains("added");

        StompFrameHandler handler = new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                String json = payload.toString();
                if (joinedMatcher.test(json)) {
                    notification.complete(json);
                }
            }
        };

        user1Session.subscribe("/user/private/chat", handler);
        user1Session.subscribe("/user/private/chat/event", handler);

        var chatEvent = dev.thural.quietspace.websocket.event.message.ChatEvent.builder()
                .chatId(chatId)
                .actorId(user1Id)
                .recipientId(user2Id)
                .type(dev.thural.quietspace.enums.EventType.JOINED_CHAT)
                .build();

        user1Session.send("/app/private/chat/join", chatEvent);

        String event = notification.get(10, TimeUnit.SECONDS);
        assertThat(event).contains("JOINED_CHAT");
    }
}
