package com.smartCity.Web.auth.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.smartCity.Web.auth.jwt.verification.JwtVerifier;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reads JWT tokens from incoming requests and populates the Spring Security context when a token is valid.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  public static final String AUTH_COOKIE_NAME = "SMARTCITY_AUTH";

  private final JwtVerifier jwtVerifier;

  public JwtAuthenticationFilter(JwtVerifier jwtVerifier) {
    this.jwtVerifier = jwtVerifier;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = resolveToken(request);

    if (!StringUtils.hasText(token)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      Claims claims = jwtVerifier.verify(token);
      String role = claims.get("role", String.class);
      String email = claims.getSubject();
      Long userId = claims.get("userId", Long.class);
      String name = claims.get("name", String.class);

      JwtUserPrincipal principal = new JwtUserPrincipal(userId, name, email, role);
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (Exception ex) {
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
      return header.substring(7);
    }

    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (AUTH_COOKIE_NAME.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
        return cookie.getValue();
      }
    }

    return null;
  }
}
