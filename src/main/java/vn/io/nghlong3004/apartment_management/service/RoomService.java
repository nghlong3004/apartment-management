package vn.io.nghlong3004.apartment_management.service;

import java.util.List;

import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.dto.RoomRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomResponse;

public interface RoomService {

	Room getRoom(Long floorId, Long roomId);

	List<Room> getAllRooms(Long floorId);

	void reserveRoom(Room room, Long userId);

	void createRoom(Long floorId, RoomRequest roomCreateRequest);

	List<RoomResponse> getRoomsByFloor(Long floorId);

	RoomResponse getRoomResponse(Long floorId, Long roomId);

	void updateRoom(Long floorId, Long roomId, RoomRequest req);

	void deleteRoom(Long floorId, Long roomId);
}
