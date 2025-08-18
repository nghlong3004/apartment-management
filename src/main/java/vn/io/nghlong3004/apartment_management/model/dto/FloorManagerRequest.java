package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FloorManagerRequest(
		@NotNull(message = "User ID not null") @Min(value = 1, message = "User ID must be greater than or equal to 1") Long userId) {

}
