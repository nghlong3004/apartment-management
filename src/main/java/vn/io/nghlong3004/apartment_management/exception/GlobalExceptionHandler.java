package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.model.dto.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnwantedException(Exception exception) {
		log.error("An unexpected error occurred: ", exception);

		ErrorState errorState = ErrorState.UNWANTED_EXCEPTION;
		return handleException(errorState.getStatus(), errorState.getMessage());
	}

	@ExceptionHandler(MissingRequestCookieException.class)
	public ResponseEntity<ErrorResponse> handleMissingRequestCookieException(MissingRequestCookieException exception) {
		log.warn("Missing required cookie '{}'. Message: {}", exception.getCookieName(), exception.getMessage());

		ErrorState errorState = ErrorState.ERROR_REFRESH_TOKEN;
		return handleException(errorState.getStatus(), errorState.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
		log.error("A database integrity violation occurred: ", exception);

		ErrorState errorState = ErrorState.DATABASE_EXCEPTION;
		return handleException(errorState.getStatus(), errorState.getMessage());
	}

	@ExceptionHandler(ResourceException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(ResourceException exception) {
		log.warn("A resource exception was handled: Status={}, Message='{}'", exception.getErrorState().getStatus(),
				exception.getMessage());

		return handleException(exception.getErrorState().getStatus(), exception.getMessage());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
		log.warn("A resource exception was handled: Status={}, Message='{}'", exception.getStatusCode(),
				exception.getMessage());

		return handleException(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
		String validationErrors = generateMessage(exception);
		log.warn("Validation failed for request: {}", validationErrors);

		return handleException(HttpStatus.BAD_REQUEST, validationErrors);
	}

	private ResponseEntity<ErrorResponse> handleException(HttpStatus httpStatus, String message) {
		return new ResponseEntity<>(ErrorResponse.builder().code(httpStatus.value()).message(message).build(),
				httpStatus);
	}

	private String generateMessage(MethodArgumentNotValidException exception) {
		StringBuilder message = new StringBuilder();
		exception.getBindingResult().getAllErrors().forEach((error) -> {
			String errorMessage = error.getDefaultMessage();
			message.append(errorMessage).append(", ");
		});

		if (message.length() > 0) {
			message.setLength(message.length() - 2);
		}
		return message.toString();
	}
}
