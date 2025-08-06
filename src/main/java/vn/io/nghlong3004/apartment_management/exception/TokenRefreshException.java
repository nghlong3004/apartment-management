package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.http.HttpStatus;

import vn.io.nghlong3004.apartment_management.constants.MessageConstants;

public class TokenRefreshException extends ResourceException {

	public TokenRefreshException() {
		super(HttpStatus.BAD_GATEWAY, MessageConstants.REFRESH_TOKEN_EXPIRED);
	}

}
