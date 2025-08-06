package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.http.HttpStatus;

import vn.io.nghlong3004.apartment_management.constants.MessageConstants;

public class EmailResourceException extends ResourceException {

	public EmailResourceException() {
		super(HttpStatus.BAD_REQUEST, MessageConstants.EXISTS_EMAIL);
	}

}
