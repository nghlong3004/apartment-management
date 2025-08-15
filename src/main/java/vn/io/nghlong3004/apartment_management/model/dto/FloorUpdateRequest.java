package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FloorUpdateRequest {

	@NotBlank(message = "Floor name must not be blank")
	@Size(max = 10, message = "Floor name must be at most 10 characters")
	private String name;

	@Min(value = 1, message = "Manager ID must be greater than or equal to 1")
	private Long managerId;

	@NotNull(message = "Room count is required")
	@Min(value = 0, message = "Room count must be greater than or equal to 0")
	private Integer roomCount;
}
