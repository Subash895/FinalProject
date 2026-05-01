package com.smartCity.Web.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.auth.jwt.JwtService;
import com.smartCity.Web.auth.jwt.JwtAuthenticationFilter;
import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AuthController {

  private static final int AUTH_COOKIE_MAX_AGE_SECONDS = 7 * 24 * 60 * 60;

  private final UserService userService;
  private final JwtService jwtService;
  private final ApiDtoMapper apiDtoMapper;

  @PostMapping("/register")
  public ResponseEntity<?> register(
      @RequestBody AuthDtos.RegisterRequest request, HttpServletRequest servletRequest) {
    try {
      return authResponseEntity(userService.register(apiDtoMapper.toUser(request)), servletRequest);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Register failed: " + e.getMessage());
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestBody AuthDtos.LoginRequest request, HttpServletRequest servletRequest) {
    try {
      return authResponseEntity(
          userService.login(request.email(), request.password()), servletRequest);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Login failed: " + e.getMessage());
    }
  }

  @PostMapping("/google")
  public ResponseEntity<?> google(
      @RequestBody GoogleAuthRequest request, HttpServletRequest servletRequest) {
    try {
      return authResponseEntity(
          userService.loginWithGoogle(request.credential(), request.role()), servletRequest);
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .body("Google sign-in is unavailable: " + e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Google sign-in failed: " + e.getMessage());
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, expiredAuthCookie(request).toString())
        .build();
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<?> forgotPassword(@RequestBody AuthDtos.ForgotPasswordRequest request) {
    try {
      userService.sendPasswordResetOtp(request.email());
      return ResponseEntity.ok("OTP sent to your email");
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Forgot password failed: " + e.getMessage());
    }
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(@RequestBody AuthDtos.ResetPasswordRequest request) {
    try {
      userService.resetPasswordWithOtp(request.email(), request.otp(), request.newPassword());
      return ResponseEntity.ok("Password updated successfully");
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Reset password failed: " + e.getMessage());
    }
  }

  @GetMapping("/google/config")
  public ResponseEntity<GoogleAuthConfigResponse> googleConfig() {
    boolean enabled = userService.isGoogleAuthEnabled();
    return ResponseEntity.ok(
        new GoogleAuthConfigResponse(enabled, enabled ? userService.getGoogleClientId() : null));
  }

  private AuthDtos.AuthResponse authResponse(User user) {
    return apiDtoMapper.toAuthResponse(jwtService.generateToken(user), user);
  }

  private ResponseEntity<AuthDtos.AuthResponse> authResponseEntity(
      User user, HttpServletRequest request) {
    AuthDtos.AuthResponse response = authResponse(user);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, authCookie(response.token(), request).toString())
        .body(response);
  }

  private ResponseCookie authCookie(String token, HttpServletRequest request) {
    return ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, token)
        .httpOnly(true)
        .secure(request.isSecure())
        .sameSite("Lax")
        .path("/")
        .maxAge(AUTH_COOKIE_MAX_AGE_SECONDS)
        .build();
  }

  private ResponseCookie expiredAuthCookie(HttpServletRequest request) {
    return ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, "")
        .httpOnly(true)
        .secure(request.isSecure())
        .sameSite("Lax")
        .path("/")
        .maxAge(0)
        .build();
  }

  public record GoogleAuthRequest(String credential, Role role) {}

  public record GoogleAuthConfigResponse(boolean enabled, String clientId) {}
}
