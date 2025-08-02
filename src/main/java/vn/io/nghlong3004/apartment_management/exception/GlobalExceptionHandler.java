package vn.io.nghlong3004.apartment_management.exception;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vn.io.nghlong3004.apartment_management.model.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AppException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(AppException appException) {
		ErrorResponse errorResponse = new ErrorResponse(appException.getHttpStatus().value(), appException.getMessage(),
				LocalDateTime.now());
		return new ResponseEntity<>(errorResponse, appException.getHttpStatus());
	}

}
