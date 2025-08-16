package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FloorRequest(
		@NotBlank(message = "Floor name must not be blank") @Size(max = 10, message = "Floor name must be at most 10 characters") String name,
		@Min(value = 1, message = "Manager ID must be greater than or equal to 1") Long managerId) {
}
