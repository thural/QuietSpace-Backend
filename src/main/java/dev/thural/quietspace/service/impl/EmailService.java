package dev.thural.quietspace.service.impl;

import dev.thural.quietspace.shared.enums.EmailTemplateName;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;

    public void sendEmail(
            String to,
            String username,
            EmailTemplateName emailTemplate,
            String confirmationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {

        String templateName;
        if (emailTemplate == null) templateName = "confirm-email";
        else templateName = emailTemplate.getName();

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name()
        );

        String templateContent;
        Resource resource = resourceLoader.getResource("classpath:templates/" + templateName + ".html");
        try (InputStream inputStream = resource.getInputStream()) {
            templateContent = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load email template: {}", templateName, e);
            throw new MessagingException("Failed to load email template: " + templateName, e);
        }

        String htmlBody = templateContent
                .replace("{{username}}", username != null ? username : "")
                .replace("{{confirmationUrl}}", confirmationUrl != null ? confirmationUrl : "")
                .replace("{{activation_code}}", activationCode != null ? activationCode : "");

        helper.setFrom("tural.musaibov@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(mimeMessage);
        log.info("sent email to {}", to);
    }
}
