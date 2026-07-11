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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        helper = new IntegrationTestHelper(mockMvc, objectMapper, userRepository, passwordEncoder);
        user1Jwt = helper.registerAndLogin("wsuser@test.com", "password123");
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
}
