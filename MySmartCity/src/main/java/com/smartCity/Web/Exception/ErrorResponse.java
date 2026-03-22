package com.smartCity.Web.Exception;

import java.time.LocalDateTime;

public class ErrorResponse {

	private boolean success;
	private String message;
	private String errorCode;
	private LocalDateTime timestamp;

	public ErrorResponse(boolean success, String message, String errorCode, LocalDateTime timestamp) {
		this.success = success;
		this.message = message;
		this.errorCode = errorCode;
		this.timestamp = timestamp;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}
}