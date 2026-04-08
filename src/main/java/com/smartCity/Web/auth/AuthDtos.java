package com.smartCity.Web.auth;

import com.smartCity.Web.user.Role;

public final class AuthDtos {

  private AuthDtos() {}

  public record LoginRequest(String email, String password) {}

  public record RegisterRequest(String name, String email, String password, Role role) {}

  public record ForgotPasswordRequest(String email) {}

  public record ResetPasswordRequest(String email, String otp, String newPassword) {}

  public record AuthResponse(String token, Long id, String name, String email, Role role) {}
}
