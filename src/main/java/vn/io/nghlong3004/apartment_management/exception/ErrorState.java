package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorState {
	// Server
	UNWANTED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later."),
	ERROR_GENERATE_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate token."),

	// Auth / Security
	LOGIN_FALSE(HttpStatus.BAD_REQUEST, "Invalid email or password."),
	ACCESS_TOKEN_IS_WRONG(HttpStatus.BAD_REQUEST, "Invalid or malformed access token."),
	REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "Refresh token has expired. Please sign in again."),
	ERROR_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "Refresh token is invalid or expired."),
	ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "Account is inactive."),
	UPDATE_USER_FORBIDDEN(HttpStatus.FORBIDDEN, "You do not have permission to change this profile."),

	// Resource / Data
	NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found."),
	API_DOES_NOT_EXISTS(HttpStatus.NOT_FOUND, "Endpoint not found."),
	EXISTS_EMAIL(HttpStatus.BAD_REQUEST, "Email is already in use."),
	DATABASE_EXCEPTION(HttpStatus.CONFLICT, "Data conflict or constraint violation."),

	// Room / Business rules
	ROOM_NOT_EXISTS(HttpStatus.NOT_FOUND, "Room does not exist."),
	ROOM_EXCEPTION(HttpStatus.BAD_REQUEST, "Room is already reserved or sold."),
	ROOM_MOVE_EXCEPTION(HttpStatus.BAD_REQUEST, "Cannot move to a reserved room."),
	UNABLE_TO_MOVE_TO_OWN_ROOM(HttpStatus.BAD_REQUEST, "Cannot move to your own room."),

	// Requests
	USER_HAS_PENDING_REQUEST(HttpStatus.BAD_REQUEST, "You already have a pending request."),
	USER_OR_OTHER_HAS_PENDING_REQUEST(HttpStatus.BAD_REQUEST, "You or another user already has a pending request."),

	// HTTP semantics
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Content-Type.");

	private final HttpStatus status;
	private final String message;
}
