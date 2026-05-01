package com.smartCity.Web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.smartCity.Web.auth.jwt.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Defines the Spring Security rules, authentication flow, and endpoint access policy.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, authException) ->
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/", "/index.html", "/*.html", "/css/**", "/js/**", "/error")
                    .permitAll()
                    .requestMatchers("/api/auth/**", "/api/config/public")
                    .permitAll()
                    .requestMatchers("/api/payments/webhook")
                    .permitAll()
                    .requestMatchers("/api/chat/**")
                    .authenticated()
                    .requestMatchers("/api/users/me")
                    .authenticated()
                    .requestMatchers(
                        "/api/subscriptions/checkout",
                        "/api/subscriptions/confirm",
                        "/api/subscriptions/failure",
                        "/api/subscriptions/my")
                    .authenticated()
                    .requestMatchers("/api/subscriptions/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/cities/**", "/api/places/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/cities/**", "/api/places/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/cities/**", "/api/places/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/**")
                    .permitAll()
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
