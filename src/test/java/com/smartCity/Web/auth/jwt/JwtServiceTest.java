package com.smartCity.Web.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

  private static final String SECRET = "01234567890123456789012345678901";

  @Test
  void generateTokenIncludesExpectedClaims() {
    JwtService jwtService = new JwtService(SECRET, 60_000L);
    User user = new User("Subash", "subash@example.com", "secret", Role.ADMIN);
    user.setId(42L);

    String token = jwtService.generateToken(user);
    Claims claims = jwtService.parse(token);

    assertEquals("subash@example.com", claims.getSubject());
    assertEquals(42L, claims.get("userId", Long.class));
    assertEquals("Subash", claims.get("name", String.class));
    assertEquals("ADMIN", claims.get("role", String.class));
  }

  @Test
  void parseRejectsTokenSignedWithDifferentKey() {
    JwtService firstService = new JwtService(SECRET, 60_000L);
    JwtService secondService = new JwtService("abcdefghijklmnopqrstuvwxyz123456", 60_000L);
    User user = new User("Subash", "subash@example.com", "secret", Role.USER);
    user.setId(7L);

    String token = firstService.generateToken(user);

    assertThrows(JwtException.class, () -> secondService.parse(token));
  }
}
