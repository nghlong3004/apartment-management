package vn.io.nghlong3004.apartment_management.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
	private int code;
	private String message;
}
