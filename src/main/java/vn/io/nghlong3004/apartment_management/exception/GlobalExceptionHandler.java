package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vn.io.nghlong3004.apartment_management.model.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AppException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(AppException appException) {
		System.out.println(appException.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(appException.getHttpStatus().value(),
				appException.getMessage());
		return new ResponseEntity<>(errorResponse, appException.getHttpStatus());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {
		StringBuilder message = new StringBuilder();
		exception.getBindingResult().getAllErrors().forEach((error) -> {
			String errorMessage = error.getDefaultMessage();
			message.append(errorMessage + " ");

		});
		ErrorResponse errorResponse = new ErrorResponse(400, new String(message));

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

}
