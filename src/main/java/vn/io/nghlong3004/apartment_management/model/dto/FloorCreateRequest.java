package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FloorCreateRequest {

	@NotBlank(message = "Floor name must not be blank")
	private String name;

	@Min(value = 0, message = "Room count must be greater than or equal to 0")
	private Integer roomCount;
}
