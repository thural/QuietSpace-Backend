package dev.thural.quietspacebackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtValidator extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = request.getHeader(JwtConstant.JWT_HEADER);

        if (StringUtils.hasText(jwt)) try {
            String email = JwtProvider.getEmailFromJwtToken(jwt);
            List<GrantedAuthority> authorityList = new ArrayList<>();
            Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorityList);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            throw new BadCredentialsException("invalid token");
        }
//        else throw new BadCredentialsException("please provide a valid token");

        filterChain.doFilter(request, response);
    }

}
