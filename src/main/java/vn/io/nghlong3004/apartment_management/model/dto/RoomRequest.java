package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;

@Data
public class RoomRequest {
	@NotBlank(message = "Room name must not be blank")
	@Size(max = 20, message = "Room name must be at most 20 characters")
	private String name;

	private Long userId;

	private RoomStatus status;
}
