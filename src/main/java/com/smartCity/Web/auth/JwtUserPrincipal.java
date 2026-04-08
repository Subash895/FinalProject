package com.smartCity.Web.auth;

public record JwtUserPrincipal(Long id, String name, String email, String role) {}
