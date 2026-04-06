package com.smartCity.Web.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserService;
import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.auth.AuthDtos;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

	private final UserService userService;
	private final JwtService jwtService;
	private final ApiDtoMapper apiDtoMapper;

	public AuthController(UserService userService, JwtService jwtService, ApiDtoMapper apiDtoMapper) {
		this.userService = userService;
		this.jwtService = jwtService;
		this.apiDtoMapper = apiDtoMapper;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody AuthDtos.RegisterRequest request) {
		try {
			User savedUser = userService.register(apiDtoMapper.toUser(request));
			String token = jwtService.generateToken(savedUser);
			return ResponseEntity.ok(apiDtoMapper.toAuthResponse(token, savedUser));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("Register failed: " + e.getMessage());
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody AuthDtos.LoginRequest request) {
		try {
			User loggedInUser = userService.login(request.email(), request.password());
			String token = jwtService.generateToken(loggedInUser);
			return ResponseEntity.ok(apiDtoMapper.toAuthResponse(token, loggedInUser));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body("Login failed: " + e.getMessage());
		}
	}

	@PostMapping("/google")
	public ResponseEntity<?> google(@RequestBody GoogleAuthRequest request) {
		try {
			User loggedInUser = userService.loginWithGoogle(request.credential(), request.role());
			String token = jwtService.generateToken(loggedInUser);
			return ResponseEntity.ok(apiDtoMapper.toAuthResponse(token, loggedInUser));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body("Google sign-in is unavailable: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.badRequest()
					.body("Google sign-in failed: " + e.getMessage());
		}
	}

	@GetMapping("/google/config")
	public ResponseEntity<GoogleAuthConfigResponse> googleConfig() {
		boolean enabled = userService.isGoogleAuthEnabled();
		return ResponseEntity.ok(new GoogleAuthConfigResponse(enabled, enabled ? userService.getGoogleClientId() : null));
	}

	public record GoogleAuthRequest(String credential, Role role) {
	}

	public record GoogleAuthConfigResponse(boolean enabled, String clientId) {
	}
}

