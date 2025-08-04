package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends AppException {

	public DuplicateResourceException(String message) {
		super(HttpStatus.BAD_REQUEST, message);
	}

}
