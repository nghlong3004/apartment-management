package vn.io.nghlong3004.apartment_management.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
	private int code;
	private String message;
}
