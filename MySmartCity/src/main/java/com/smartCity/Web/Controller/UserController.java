package com.smartCity.Web.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.Service.UserService;
import com.smartCity.Web.dto.request.LoginRequest;
import com.smartCity.Web.dto.request.UserRequest;
import com.smartCity.Web.dto.response.ApiResponse;
import com.smartCity.Web.dto.response.UserResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        userService.register(request),
                        "User registered successfully"
                )
        );
    }

    // 🔥 LOGIN → RETURNS TOKEN
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        userService.login(request),
                        "Login successful"
                )
        );
    }
}