package com.smartCity.Web.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.Service.UserService;
import com.smartCity.Web.dto.request.LoginRequest;
import com.smartCity.Web.dto.request.UserRequest;
import com.smartCity.Web.dto.response.ApiResponse;
import com.smartCity.Web.dto.response.AuthResponse;
import com.smartCity.Web.dto.response.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody UserRequest request) {

		UserResponse response = userService.register(request);

		return ResponseEntity.ok(new ApiResponse<>(true, response, "User registered successfully"));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {

		String token = userService.login(request);

		AuthResponse authResponse = new AuthResponse(token);

		return ResponseEntity.ok(new ApiResponse<>(true, authResponse, "Login successful"));
	}
}