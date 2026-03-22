package com.smartCity.Web.Exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// 🔥 USER NOT FOUND
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {

		ErrorResponse error = new ErrorResponse(false, ex.getMessage(), "NOT_FOUND", LocalDateTime.now());

		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	// 🔥 INVALID LOGIN
	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {

		ErrorResponse error = new ErrorResponse(false, ex.getMessage(), "INVALID_CREDENTIALS", LocalDateTime.now());

		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	// 🔥 BAD REQUEST (fallback for runtime)
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {

		ErrorResponse error = new ErrorResponse(false, ex.getMessage(), "BAD_REQUEST", LocalDateTime.now());

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	// 🔥 FINAL FALLBACK (SYSTEM ERROR)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAll(Exception ex) {

		ErrorResponse error = new ErrorResponse(false, "Something went wrong", "INTERNAL_ERROR", LocalDateTime.now());

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}