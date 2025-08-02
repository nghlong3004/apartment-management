package vn.io.nghlong3004.apartment_management.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vn.io.nghlong3004.apartment_management.model.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AppException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(AppException appException) {
		ErrorResponse errorResponse = new ErrorResponse(appException.getHttpStatus().value(),
				appException.getMessage());
		return new ResponseEntity<>(errorResponse, appException.getHttpStatus());
	}

}
