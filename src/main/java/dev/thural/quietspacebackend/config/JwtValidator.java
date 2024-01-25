package dev.thural.quietspacebackend.config;

import dev.thural.quietspacebackend.constant.JwtConstant;
import dev.thural.quietspacebackend.exception.UnauthorizedException;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.TokenBlackList;
import dev.thural.quietspacebackend.utils.JwtProvider;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtValidator extends OncePerRequestFilter {
    private final TokenBlackList tokenBlackList;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(JwtConstant.JWT_HEADER);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer")){
            filterChain.doFilter(request, response);
            return;
        }

        if (tokenBlackList.isBlacklisted(authHeader)){
            filterChain.doFilter(request, response);
            return;
        }

        String userEmail = JwtProvider.extractEmailFromAuthHeader(authHeader);

        if (userEmail == null || SecurityContextHolder.getContext().getAuthentication() != null){
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if (JwtProvider.isTokenValid(authHeader, userDetails)) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

}
