package com.medev.hrstream.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;


@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Expiration in milliseconds.
     */
    @Value("${jwt.expiration:86400000}")
    private long expiration;


    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                        .collect(Collectors.toList()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenWithClaims(UserDetails userDetails, String firstname, String lastname) {
        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                        .collect(Collectors.toList()));
                        
        if (firstname != null) {
            builder.claim("firstname", firstname);
        }
        if (lastname != null) {
            builder.claim("lastname", lastname);
        }
        
        return builder
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is missing. Configure jwt.secret or JWT_SECRET with at least 32 bytes.");
        }

        // Use raw text by default so plain secrets that happen to be Base64-like are not misinterpreted.
        byte[] rawBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = rawBytes;

        // If raw text is too short, allow a Base64 encoded secret as a fallback.
        if (rawBytes.length < 32) {
            try {
                byte[] decoded = Decoders.BASE64.decode(secret.trim());
                if (decoded.length >= 32) {
                    keyBytes = decoded;
                }
            } catch (Exception ignored) {
                // Keep raw bytes and fail with a clear message below if still too short.
            }
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret is too short for HS256. Provide at least 32 bytes (256 bits), either raw text or Base64-encoded."
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
