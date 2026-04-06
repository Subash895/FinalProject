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

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody User user) {
		try {
			User savedUser = userService.register(user);
			return ResponseEntity.ok(savedUser);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("Register failed: " + e.getMessage());
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody User user) {
		try {
			User loggedInUser = userService.login(user.getEmail(), user.getPassword());
			return ResponseEntity.ok(loggedInUser);
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body("Login failed: " + e.getMessage());
		}
	}

	@PostMapping("/google")
	public ResponseEntity<?> google(@RequestBody GoogleAuthRequest request) {
		try {
			User loggedInUser = userService.loginWithGoogle(request.credential(), request.role());
			return ResponseEntity.ok(loggedInUser);
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

