package dev.thural.quietspace.service;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.thural.quietspace.config.TestcontainersConfig;
import dev.thural.quietspace.enums.EmailTemplateName;
import dev.thural.quietspace.service.impl.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@Import(TestcontainersConfig.class)
@ActiveProfiles("testcontainers")
@WireMockTest(httpPort = 1025)
class EmailServiceIT {

    @Autowired
    private EmailService emailService;

    @Test
    void sendEmail_shouldDeliverSuccessfully() throws Exception {
        stubFor(post("/").willReturn(aResponse().withStatus(200)));

        emailService.sendEmail(
                "recipient@test.com",
                "Test User",
                EmailTemplateName.ACTIVATE_ACCOUNT,
                "http://localhost:3000/activate",
                "123456",
                "Account Activation"
        );

        verify(postRequestedFor(urlEqualTo("/")));
    }
}
