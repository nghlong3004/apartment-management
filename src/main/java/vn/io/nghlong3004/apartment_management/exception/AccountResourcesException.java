package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.http.HttpStatus;

import vn.io.nghlong3004.apartment_management.constants.MessageConstants;

public class AccountResourcesException extends ResourceException {

	public AccountResourcesException() {
		super(HttpStatus.BAD_REQUEST, MessageConstants.LOGIN_FALSE);
	}

}
