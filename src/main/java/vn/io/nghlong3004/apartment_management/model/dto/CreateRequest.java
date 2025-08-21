package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateRequest(
		@NotNull(message = "User ID cannot be null.") @Min(value = 1, message = "User ID must be at least 1.") Long requesterId,
		Long counterpartId, Long requesterRoomId,
		@NotNull(message = "Room ID cannot be null.") @Min(value = 1, message = "Room ID must be at least 1.") Long counterpartRoomId) {

}
