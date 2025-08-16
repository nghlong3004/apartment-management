package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;

public record RoomRequest(
		@NotBlank(message = "Room name must not be blank") @Size(max = 20, message = "Room name must be at most 20 characters") String name,
		Long userId, RoomStatus status) {

}
