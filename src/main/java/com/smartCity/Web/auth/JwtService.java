package com.smartCity.Web.auth;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.smartCity.Web.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Creates and validates JWT tokens used by the API authentication layer.
 */
@Service
public class JwtService {

  private final Key signingKey;
  private final long expirationMs;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-ms}") long expirationMs) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  public String generateToken(User user) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
        .subject(user.getEmail())
        .claim("userId", user.getId())
        .claim("name", user.getName())
        .claim("role", user.getRole().name())
        .issuedAt(now)
        .expiration(expiry)
        .signWith(signingKey)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser()
        .verifyWith((javax.crypto.SecretKey) signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
