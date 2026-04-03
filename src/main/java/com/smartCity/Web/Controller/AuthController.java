package com.smartCity.Web.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.Model.User;
import com.smartCity.Web.Service.UserService;

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
			e.printStackTrace(); //shows real error in console
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
}
