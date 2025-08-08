package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorState {
	UNWANTED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred, please try again later."),
	EXISTS_EMAIL(HttpStatus.BAD_REQUEST, "Email may already be in use."),
	REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "Refresh token was expired. Please make a new signin request."),
	DATABASE_EXCEPTION(HttpStatus.CONFLICT, "Data conflicts or constraint violations."),
	LOGIN_FALSE(HttpStatus.BAD_REQUEST, "Incorrect email or password."),
	ERROR_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "Refresh Token expired or not available."),
	ERROR_GENERATE_TOKEN(HttpStatus.BAD_REQUEST, "Token generation error.");

	private HttpStatus status;
	private String message;

}
