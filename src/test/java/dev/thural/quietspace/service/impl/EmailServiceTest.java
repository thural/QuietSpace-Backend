package dev.thural.quietspace.service.impl;

import dev.thural.quietspace.shared.enums.EmailTemplateName;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendEmail_givenAllParams_shouldSendMimeMessage() throws MessagingException, IOException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        Resource resource = mock(Resource.class);
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<html>Hello {{username}}</html>".getBytes()));

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
    void sendEmail_givenNullTemplate_shouldUseDefaultName() throws MessagingException, IOException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Resource resource = mock(Resource.class);
        when(resourceLoader.getResource("classpath:templates/confirm-email.html")).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<html>Hello {{username}}</html>".getBytes()));

        emailService.sendEmail(
                "recipient@example.com",
                "TestUser",
                null,
                "http://localhost:8080/activate",
                "123456",
                "Account Activation"
        );

        verify(resourceLoader).getResource("classpath:templates/confirm-email.html");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_whenMailSenderFails_shouldPropagate() throws MessagingException, IOException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Resource resource = mock(Resource.class);
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<html>Hello {{username}}</html>".getBytes()));

        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() ->
                emailService.sendEmail(
                        "recipient@example.com",
                        "TestUser",
                        EmailTemplateName.ACTIVATE_ACCOUNT,
                        "http://localhost:8080/activate",
                        "123456",
                        "Account Activation"
                )
        ).isInstanceOf(RuntimeException.class).hasMessageContaining("SMTP error");
    }
}
