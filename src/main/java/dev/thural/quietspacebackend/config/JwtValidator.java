package dev.thural.quietspacebackend.config;

import dev.thural.quietspacebackend.constant.JwtConstant;
import dev.thural.quietspacebackend.service.TokenBlackList;
import dev.thural.quietspacebackend.service.impls.TokenBlackListImpl;
import dev.thural.quietspacebackend.utils.JwtProvider;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtValidator extends OncePerRequestFilter {
    private final TokenBlackList tokenBlackList;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = request.getHeader(JwtConstant.JWT_HEADER);

        if (StringUtils.hasText(jwt) && !tokenBlackList.isBlacklisted(jwt)) try {
            String email = JwtProvider.getEmailFromJwtToken(jwt);
            List<GrantedAuthority> authorityList = new ArrayList<>();
            Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorityList);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            throw new BadCredentialsException("invalid token");
        }

        filterChain.doFilter(request, response);
    }

}
