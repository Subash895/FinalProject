package com.smartCity.Web.auth.jwt;

/**
 * Adapts the application user model to the Spring Security principal contract.
 */
public record JwtUserPrincipal(Long id, String name, String email, String role) {}
