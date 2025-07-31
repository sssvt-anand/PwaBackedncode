package com.room.app.config;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import com.room.app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final SecretKey secretKey;
    private final long expirationMs;
    private final long refreshExpirationMs;
    private final UserService userService;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationMs,
                   @Value("${jwt.refreshExpiration}") long refreshExpirationMs,
                   @Lazy UserService userService) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.userService = userService;
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority().startsWith("ROLE_") ? auth.getAuthority() : "ROLE_" + auth.getAuthority())
                .collect(Collectors.toList()));

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username != null &&
                username.equals(userDetails.getUsername()) &&
                !isTokenExpired(token));
    }

    public boolean canTokenBeRefreshed(String token) {
        return !isTokenExpired(token) ||
                (isTokenExpired(token) &&
                        getClaims(token).getExpiration().after(new Date(System.currentTimeMillis() - refreshExpirationMs)));
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public List<GrantedAuthority> extractAuthorities(String token) {
        Claims claims = getClaims(token);
        List<String> roles = claims.get("roles", List.class);

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                .collect(Collectors.toList());
    }

    public boolean shouldRefreshToken(String token) {
        Claims claims = getClaims(token);
        Date expiration = claims.getExpiration();
        return expiration.before(new Date(System.currentTimeMillis() + 300000)); // 5 minutes
    }

    public boolean isRefreshTokenValid(String refreshToken, UserDetails userDetails) {
        final String username = extractUsername(refreshToken);
        return (username != null &&
                username.equals(userDetails.getUsername()) &&
                !isTokenExpired(refreshToken));
    }

}