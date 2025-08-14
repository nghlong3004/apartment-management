package vn.io.nghlong3004.apartment_management.service;

import vn.io.nghlong3004.apartment_management.model.Room;

public interface RoomService {

	Room getRoomOrThrow(Long floorId, Long roomId);

	void reserveRoom(Room room, Long userId);
}
