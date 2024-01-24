package dev.thural.quietspacebackend.config;

import dev.thural.quietspacebackend.constant.JwtConstant;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.TokenBlackList;
import dev.thural.quietspacebackend.utils.JwtProvider;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtValidator extends OncePerRequestFilter {
    private final TokenBlackList tokenBlackList;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = request.getHeader(JwtConstant.JWT_HEADER);

        if (StringUtils.hasText(jwt) && jwt.startsWith("Bearer") && !tokenBlackList.isBlacklisted(jwt)) try {
            String email = JwtProvider.getEmailFromJwtToken(jwt);

            Optional<UserEntity> optionalUser = userRepository.findUserEntityByEmail(email);

            UserEntity user = optionalUser.orElseThrow(
                    () -> new UsernameNotFoundException("user not found with the email"));

            List<GrantedAuthority> authorityList = Arrays.stream(user.getRole().split(","))
                    .map(role -> new SimpleGrantedAuthority(role.toUpperCase()))
                    .collect(Collectors.toList());

            Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorityList);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            throw new BadCredentialsException("invalid token");
        }

        filterChain.doFilter(request, response);
    }

}
