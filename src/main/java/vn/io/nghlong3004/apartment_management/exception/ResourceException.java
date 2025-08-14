package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ResourceException extends RuntimeException {
	private final HttpStatus status;
	private final String message;

	public ResourceException(HttpStatus status, String message) {
		super(message);
		this.message = message;
		this.status = status;
	}

}
