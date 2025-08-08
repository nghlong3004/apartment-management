package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vn.io.nghlong3004.apartment_management.model.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnwantedException(Exception exception) {
		exception.printStackTrace();

		ErrorState errorState = ErrorState.UNWANTED_EXCEPTION;

		return handleException(errorState.getStatus(), errorState.getMessage());
	}

	@ExceptionHandler(MissingRequestCookieException.class)
	public ResponseEntity<ErrorResponse> handleMissingRequestCookieException(MissingRequestCookieException exception) {

		ErrorState errorState = ErrorState.ERROR_REFRESH_TOKEN;

		return handleException(errorState.getStatus(), errorState.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {

		ErrorState errorState = ErrorState.DATABASE_EXCEPTION;

		return handleException(errorState.getStatus(), errorState.getMessage());
	}

	@ExceptionHandler(ResourceException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(ResourceException exception) {

		return handleException(exception.getErrorState().getStatus(), exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {

		return handleException(HttpStatus.BAD_REQUEST, generateMessage(exception));
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
