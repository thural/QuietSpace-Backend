package dev.thural.quietspacebackend.config;
import dev.thural.quietspacebackend.service.TokenBlackList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
    private final TokenBlackList tokenBlackList;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }

        tokenBlackList.addToBlacklist(authHeader);
        SecurityContextHolder.clearContext();
    }
}

