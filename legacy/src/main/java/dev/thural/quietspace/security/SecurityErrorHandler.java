package dev.thural.quietspace.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.model.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;

public final class SecurityErrorHandler {

    private SecurityErrorHandler() {
        throw new AssertionError("cannot be instantiated");
    }

    public static void handleSecurityError(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception,
            int statusCode,
            String error,
            String defaultMessage,
            ObjectMapper objectMapper
    ) throws IOException {
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        var errorResponse = ErrorResponse.of(
                statusCode,
                error,
                exception.getMessage() != null ? exception.getMessage() : defaultMessage,
                request.getRequestURI()
        );

        try (var writer = response.getWriter()) {
            objectMapper.writeValue(writer, errorResponse);
        } catch (IOException e) {
            throw new IOException("Failed to write error response", e);
        }
    }
}