package dev.thural.quietspace.shared.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.shared.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Profile("prod")
@Service
@Slf4j
@RequiredArgsConstructor
public class PostmarkEmailService implements EmailService {

    private final RestTemplate restTemplate;
    private final SpringTemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    @Value("${spring.mail.username}")
    private String postmarkServerToken;

    @Async("emailExecutor")
    @Override
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlBody = templateEngine.process(templateName, context);

            Map<String, String> body = Map.of(
                    "From", "contact@quietspace.live",
                    "To", to,
                    "Subject", subject,
                    "HtmlBody", htmlBody
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Postmark-Server-Token", postmarkServerToken);

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

            restTemplate.postForEntity("https://api.postmarkapp.com/email", request, String.class);
            log.info("sent email via Postmark to {}", to);
        } catch (Exception e) {
            log.error("failed to send email via Postmark to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email via Postmark", e);
        }
    }
}
