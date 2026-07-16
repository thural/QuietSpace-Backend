package dev.thural.quietspace.security;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            @Nullable HttpServletRequest request,
            @Nullable HttpServletResponse response,
            @Nullable AuthenticationException authException
    ) throws IOException {
        if (request == null || response == null || authException == null) return;
        SecurityErrorHandler.handleSecurityError(
                request,
                response,
                authException,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                "Authentication failed",
                objectMapper
        );
    }
}