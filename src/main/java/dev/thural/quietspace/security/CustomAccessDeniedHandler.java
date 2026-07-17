package dev.thural.quietspace.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            @Nullable HttpServletRequest request,
            @Nullable HttpServletResponse response,
            @Nullable AccessDeniedException accessDeniedException
    ) throws IOException {
        if (request == null || response == null || accessDeniedException == null) return;
        SecurityErrorHandler.handleSecurityError(
                request,
                response,
                accessDeniedException,
                HttpServletResponse.SC_FORBIDDEN,
                "Forbidden",
                "Access denied",
                objectMapper
        );
    }
}