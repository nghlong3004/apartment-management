package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vn.io.nghlong3004.apartment_management.model.dto.ErrorResponse;
import vn.io.nghlong3004.apartment_management.util.MessageUtil;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnwantedException(Exception exception) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				MessageUtil.UNWANTED_EXCEPTION);

		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(), MessageUtil.DATABASE_EXCEPTION);

		return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(AppException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(AppException appException) {
		ErrorResponse errorResponse = new ErrorResponse(appException.getHttpStatus().value(),
				appException.getMessage());
		return new ResponseEntity<>(errorResponse, appException.getHttpStatus());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {

		ErrorResponse errorResponse = new ErrorResponse(400, generateMessage(exception));

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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
