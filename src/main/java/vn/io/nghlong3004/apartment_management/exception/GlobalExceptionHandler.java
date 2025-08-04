package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vn.io.nghlong3004.apartment_management.model.dto.ErrorResponse;
import vn.io.nghlong3004.apartment_management.util.MessageConstants;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnwantedException(Exception exception) {
		exception.printStackTrace();

		return handleException(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.UNWANTED_EXCEPTION);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

		return handleException(HttpStatus.CONFLICT, MessageConstants.DATABASE_EXCEPTION);
	}

	@ExceptionHandler(AppException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(AppException appException) {

		return handleException(appException.getHttpStatus(), appException.getMessage());
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
