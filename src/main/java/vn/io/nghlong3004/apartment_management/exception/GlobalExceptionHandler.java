package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vn.io.nghlong3004.apartment_management.constants.MessageConstants;
import vn.io.nghlong3004.apartment_management.model.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnwantedException(Exception exception) {
		exception.printStackTrace();

		return handleException(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.UNWANTED_EXCEPTION);
	}

	@ExceptionHandler(MissingRequestCookieException.class)
	public ResponseEntity<ErrorResponse> handleMissingRequestCookieException(MissingRequestCookieException exception) {

		return handleException(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_REFRESH_TOKEN);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {

		return handleException(HttpStatus.CONFLICT, MessageConstants.DATABASE_EXCEPTION);
	}

	@ExceptionHandler(ResourceException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(ResourceException appException) {

		return handleException(appException.getHttpStatus(), appException.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {

		return handleException(HttpStatus.BAD_REQUEST, generateMessage(exception));
	}

	@ExceptionHandler(TokenRefreshException.class)
	public ResponseEntity<ErrorResponse> handleTokenRefreshException(TokenRefreshException exception) {

		return handleException(HttpStatus.BAD_REQUEST, MessageConstants.REFRESH_TOKEN_EXPIRED);
	}

	private ResponseEntity<ErrorResponse> handleException(HttpStatus httpStatus, String message) {

		return new ResponseEntity<>(ErrorResponse.builder().code(httpStatus.value()).message(message).build(),
				httpStatus);
	}

	private String generateMessage(MethodArgumentNotValidException exception) {
		StringBuilder message = new StringBuilder();
		exception.getBindingResult().getAllErrors().forEach((error) -> {
			String errorMessage = error.getDefaultMessage();
			message.append(errorMessage + ",");
		});

		message.deleteCharAt(message.length() - 1);
		return new String(message);
	}

}
