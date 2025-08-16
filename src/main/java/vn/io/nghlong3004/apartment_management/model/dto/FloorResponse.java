package vn.io.nghlong3004.apartment_management.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import vn.io.nghlong3004.apartment_management.model.Floor;
import vn.io.nghlong3004.apartment_management.model.Room;

@Data
@AllArgsConstructor
public class FloorResponse {
	private Long id;
	private Long managerId;
	private String name;
	private Integer roomCount;
	private List<RoomResponse> roomResponses;

	public static FloorResponse from(Floor floor, List<Room> rooms) {

		List<RoomResponse> roomResponses = rooms.stream().map(RoomResponse::from).toList();

		return new FloorResponse(floor.getId(), floor.getManagerId(), floor.getName(), floor.getRoomCount(),
				roomResponses);
	}

}
