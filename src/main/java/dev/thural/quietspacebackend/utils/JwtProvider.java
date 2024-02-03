package dev.thural.quietspacebackend.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtProvider {

    private static String secretKey = "356838792F423F4428472C7B6250655368566D597133743677397A2443264629";
    private static long jwtExpiration = 86400000;
    private long refreshExpiration = 604800000;

    public static String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public static String generateToken(Authentication auth) {
        return generateToken(new HashMap<>(), auth);
    }

    public static String generateToken(Map<String, Object> extraClaims, Authentication auth) {
        return buildToken(extraClaims, auth, jwtExpiration);
    }

    public String generateRefreshToken(Authentication auth) {
        return buildToken(new HashMap<>(), auth, refreshExpiration);
    }

    private static String buildToken(Map<String, Object> extraClaims,
                                     Authentication auth,
                                     long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(auth.getName())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private static boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private static Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private static Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private static Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
