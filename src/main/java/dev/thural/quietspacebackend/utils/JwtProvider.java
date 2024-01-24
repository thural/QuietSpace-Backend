package dev.thural.quietspacebackend.utils;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

import static dev.thural.quietspacebackend.constant.JwtConstant.SECRET_KEY;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final UserRepository userRepository;
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    public static String generatedToken(Authentication auth){
        return Jwts.builder()
                .setIssuer("thural")
                .setSubject(auth.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 142500000))
                .claim("email", auth.getName())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String getEmailFromJwtToken(String jwt){
        // Bearer token
        String emailSubstring = jwt.substring(7);

        Claims claims = getClaims(emailSubstring);

        return String.valueOf(claims.get("email"));
    }

    private static Claims getClaims(String emailSubstring) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(emailSubstring)
                .getBody();
    }

    public Optional<UserEntity> findUserByJwt(String jwt) {
        String email = JwtProvider.getEmailFromJwtToken(jwt);
        UserEntity userEntity = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user with this email not found"));
        return Optional.of(userEntity);
    }



}
