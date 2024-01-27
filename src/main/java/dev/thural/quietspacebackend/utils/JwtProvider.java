package dev.thural.quietspacebackend.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JwtProvider {
    private final static String SECRET_KEY = "I'll keep it beneath my skies, until the day it becomes one";
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    private static Key getSignKey() {
        String SECRET = "356838792F423F4428472C7B6250655368566D597133743677397A2443264629";
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String generateToken(Authentication auth) {
        return generateToken(auth, new HashMap<>());
    }

    public static String generateToken(Authentication auth, Map<String, Object> extraClaims) {
        System.out.println("email on token generation: " + auth.getName());
        System.out.println("principal on token generation: " + auth.getPrincipal());

        return Jwts.builder()
                .setIssuer("thural")
                .setSubject(auth.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 142500000))
                .setClaims(extraClaims)
                .claim("email", auth.getName())
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static String extractEmailFromAuthHeader(String authHeader) {
        String token = authHeader.substring(7);
        Claims claims = getClaims(token);
        return String.valueOf(claims.get("email"));
    }

    public static boolean isTokenValid(String authHeader, UserDetails userDetails) {
        String username = extractEmailFromAuthHeader(authHeader);
        return username.equals(userDetails.getUsername()); // TODO: check expiration logic after fixing the method
    }

    private static boolean isTokenExpired(String authHeader) {
        return extractExpiration(authHeader.substring(7)).before(new Date());
    }

    private static Date extractExpiration(String token) {
        Claims claims = getClaims(token);
        Date expirationDate = claims.getExpiration();
        System.out.println("Expiration date: " + expirationDate);
        return expirationDate;
    }

}
