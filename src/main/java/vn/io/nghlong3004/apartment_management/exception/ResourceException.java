package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ResourceException extends RuntimeException {
	private final HttpStatus httpStatus;
	private final String message;

	public ResourceException(HttpStatus httpStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
		this.message = message;
	}

}
