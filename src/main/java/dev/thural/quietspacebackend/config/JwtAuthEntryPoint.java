package dev.thural.quietspacebackend.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        log.error(StringUtils.trimAllWhitespace("Responding with unauthorized error. Message - {}"), StringUtils.trimAllWhitespace(e.getMessage()));
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        String responseToClient = "{\"code\":" + response.getStatus() +
                ",\"message\":\"" + e.getMessage() + "\"}";
        response.setHeader("Content-Type", "application/json");
        System.out.println(response.getStatus());
        response.getWriter().write(responseToClient);
        response.getWriter().flush();
        response.getWriter().close();
    }
}
