package dev.thural.quietspace.service;

import dev.thural.quietspace.config.TestcontainersConfig;
import dev.thural.quietspace.shared.enums.EmailTemplateName;
import dev.thural.quietspace.photo.PhotoService;
import dev.thural.quietspace.service.impl.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
@ActiveProfiles("testcontainers")
class EmailServiceIT {

    @MockitoBean
    private PhotoService photoService;

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void sendEmail_shouldDeliverSuccessfully() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(
                "recipient@test.com",
                "Test User",
                EmailTemplateName.ACTIVATE_ACCOUNT,
                "http://localhost:3000/activate",
                "123456",
                "Account Activation"
        );

        verify(mailSender, timeout(3000)).send(mimeMessage);
    }
}
