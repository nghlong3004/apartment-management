package vn.io.nghlong3004.apartment_management.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;

@Data
@AllArgsConstructor
public class RoomResponse {
	private Long id;
	private Long floorId;
	private Long userId;
	private String name;
	private RoomStatus status;

	public static RoomResponse from(Room room) {
		return new RoomResponse(room.getId(), room.getFloorId(), room.getUserId(), room.getName(), room.getStatus());
	}

}
