package dev.thural.quietspacebackend.utils;

import dev.thural.quietspacebackend.constant.JwtConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtProvider {
    private static final SecretKey key = Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());
    public static String generatedToken(Authentication auth){
        return Jwts.builder()
                .setIssuer("thural")
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 142500000))
                .claim("email", auth.getName())
                .signWith(key)
                .compact();
    }

    public static String getEmailFromJwtToken(String jwt){
        // Bearer token
        String emailSubstring = jwt.substring(7);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(emailSubstring)
                .getBody();

        return String.valueOf(claims.get("email"));
    }
}
