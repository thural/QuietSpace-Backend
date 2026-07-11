package dev.thural.quietspace.service.impl;

import dev.thural.quietspace.enums.EmailTemplateName;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendEmail_givenAllParams_shouldSendMimeMessage() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>email body</html>");

        emailService.sendEmail(
                "recipient@example.com",
                "TestUser",
                EmailTemplateName.ACTIVATE_ACCOUNT,
                "http://localhost:8080/activate",
                "123456",
                "Account Activation"
        );

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_givenNullTemplate_shouldUseDefaultName() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("confirm-email"), any())).thenReturn("<html>email body</html>");

        emailService.sendEmail(
                "recipient@example.com",
                "TestUser",
                null,
                "http://localhost:8080/activate",
                "123456",
                "Account Activation"
        );

        verify(templateEngine).process(eq("confirm-email"), any());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_whenMailSenderFails_shouldPropagate() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>body</html>");
        doThrow(new MessagingException("SMTP error")).when(mailSender).send(mimeMessage);

        assertThatThrownBy(() ->
                emailService.sendEmail(
                        "recipient@example.com",
                        "TestUser",
                        EmailTemplateName.ACTIVATE_ACCOUNT,
                        "http://localhost:8080/activate",
                        "123456",
                        "Account Activation"
                )
        ).isInstanceOf(MessagingException.class).hasMessageContaining("SMTP error");
    }
}
