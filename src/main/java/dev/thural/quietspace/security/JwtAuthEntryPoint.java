package dev.thural.quietspace.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException e
    ) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        String responseToClient = "{\"code\":" + response.getStatus() + ",\"message\":\"" + e.getMessage() + "\"}";
        response.setHeader("Content-Type", "application/json");
        response.getWriter().write(responseToClient);
        response.getWriter().flush();
        response.getWriter().close();
    }
}
