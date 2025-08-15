package vn.io.nghlong3004.apartment_management.service.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessage;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.repository.RoomRepository;
import vn.io.nghlong3004.apartment_management.service.RoomService;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;

	public Room getRoomOrThrow(Long floorId, Long roomId) {
		return roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId).map(room -> {
			log.debug("Room found: floorId={}, roomId={}, status={}, userId={}", floorId, roomId, room.getStatus(),
					room.getUserId());
			return room;
		}).orElseThrow(() -> {
			log.warn("Room not found: floorId={}, roomId={}", floorId, roomId);
			return new ResourceException(HttpStatus.NOT_FOUND, ErrorMessage.ROOM_NOT_FOUND);
		});
	}

	public void reserveRoom(Room room, Long userId) {
		log.debug("Reserving room: roomId={}, currentStatus={}, newUserId={}", room.getId(), room.getStatus(), userId);
		room.setStatus(RoomStatus.RESERVED);
		room.setUserId(userId);
		roomRepository.updateRoom(room);
		log.info("Room reserved: roomId={}, newStatus={}, userId={}", room.getId(), room.getStatus(), userId);
	}

	@Override
	public List<Room> getAllRooms(Long floorId) {
		log.info("Fetching all rooms for floorId={}", floorId);
		return roomRepository.findAllRoomsByFloorId(floorId);
	}
}
