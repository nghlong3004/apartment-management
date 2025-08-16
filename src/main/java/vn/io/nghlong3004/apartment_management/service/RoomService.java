package vn.io.nghlong3004.apartment_management.service;

import java.util.List;

import jakarta.validation.constraints.Min;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.RoomRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomResponse;

public interface RoomService {

	List<Room> getAllRooms(Long floorId);

	void reserveRoom(Room room, Long userId);

	void createRoom(Long floorId, RoomRequest roomCreateRequest);

	RoomResponse getRoomResponse(Long floorId, Long roomId);

	void updateRoom(Long floorId, Long roomId, RoomRequest req);

	void deleteRoom(Long floorId, Long roomId);

	PagedResponse<RoomResponse> getRooms(@Min(1) Long floorId, String name, int page, int size, String sort);

	Room getRoom(Long floorId, Long roomId);

}
