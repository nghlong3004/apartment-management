package vn.io.nghlong3004.apartment_management.service;

import java.util.List;

import vn.io.nghlong3004.apartment_management.model.Room;

public interface RoomService {

	Room getRoomOrThrow(Long floorId, Long roomId);

	List<Room> getAllRooms(Long floorId);

	void reserveRoom(Room room, Long userId);
}
