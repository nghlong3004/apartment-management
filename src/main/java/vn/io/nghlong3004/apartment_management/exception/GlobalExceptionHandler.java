package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.model.dto.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnwantedException(Exception exception) {
		log.error("Unhandled exception: {}", exception);

		return handleException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageConstant.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(MissingRequestCookieException.class)
	public ResponseEntity<ErrorResponse> handleMissingRequestCookieException(MissingRequestCookieException exception) {
		log.warn("Missing required cookie '{}'. Message: {}", exception.getCookieName(), exception.getMessage());

		return handleException(HttpStatus.BAD_REQUEST, "Missing required cookie '" + exception.getCookieName() + "'.");

	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
		log.error("A database integrity violation occurred: ", exception);

		return handleException(HttpStatus.CONFLICT, ErrorMessageConstant.DATA_CONFLICT);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
			HttpMediaTypeNotSupportedException exception) {
		String unsupported = exception.getContentType() != null ? exception.getContentType().toString() : "unknown";

		String supported = exception.getSupportedMediaTypes().isEmpty() ? "none"
				: exception.getSupportedMediaTypes().toString();

		log.warn("Unsupported Content-Type: {}. Supported: {}", unsupported, supported);

		return handleException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorMessageConstant.UNSUPPORTED_MEDIA_TYPE);
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
			HandlerMethodValidationException exception) {
		String msg = exception.getParameterValidationResults().stream().flatMap(r -> r.getResolvableErrors().stream())
				.map(err -> err.getDefaultMessage()).distinct().reduce((a, b) -> a + ", " + b)
				.orElse("Validation failure");

		log.warn("Handler method validation failed: {}", msg);
		return handleException(HttpStatus.BAD_REQUEST, msg);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
			MethodArgumentTypeMismatchException exception) {
		log.warn("Calling an API that doesn't exist");

		return handleException(HttpStatus.BAD_REQUEST, "Parameter '" + exception.getName() + "' has invalid type.");
	}

	@ExceptionHandler(ResourceException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(ResourceException exception) {
		log.warn("An error occurred: {}", exception.getMessage());

		return handleException(exception.getStatus(), exception.getMessage());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
		log.warn("A resource exception was handled: Status={}, Message='{}'", exception.getStatusCode(),
				exception.getMessage());

		return handleException(HttpStatus.NOT_FOUND, ErrorMessageConstant.ENDPOINT_NOT_FOUND);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
			HttpRequestMethodNotSupportedException exception) {
		return handleException(HttpStatus.METHOD_NOT_ALLOWED, exception.getMessage());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
			HttpMessageNotReadableException exception) {
		log.warn("Http message not readable exception: {}", exception.getMessage());

		return handleException(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
			ConstraintViolationException exception) {
		log.warn("Constraint Violation Exception: {}", exception.getMessage());

		return handleException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ENDPOINT_NOT_FOUND);
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
			String ErrorMessageConstant = error.getDefaultMessage();
			message.append(ErrorMessageConstant).append(", ");
		});

		if (message.length() > 0) {
			message.setLength(message.length() - 2);
		}
		return message.toString();
	}
}
