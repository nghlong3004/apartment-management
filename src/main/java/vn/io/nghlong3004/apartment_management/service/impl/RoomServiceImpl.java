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
import vn.io.nghlong3004.apartment_management.model.dto.RoomRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomResponse;
import vn.io.nghlong3004.apartment_management.repository.RoomRepository;
import vn.io.nghlong3004.apartment_management.service.RoomService;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;

	public Room getRoom(Long floorId, Long roomId) {
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
	public RoomResponse getRoomResponse(Long floorId, Long roomId) {

		Room room = getRoom(floorId, roomId);

		return RoomResponse.from(room);
	}

	@Override
	public List<Room> getAllRooms(Long floorId) {
		log.info("Fetching all rooms for floorId={}", floorId);
		List<Room> rooms = roomRepository.findAllRoomsByFloorId(floorId);
		if (rooms == null || rooms.isEmpty()) {
			return List.of();
		}
		return rooms;
	}

	@Override
	public List<RoomResponse> getRoomsByFloor(Long floorId) {
		log.info("Fetching rooms for floorId={}", floorId);

		roomRepository.floorExists(floorId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessage.FLOOR_NOT_FOUND));

		List<Room> rooms = roomRepository.findAllRoomsByFloorId(floorId);
		if (rooms.isEmpty() || rooms == null) {
			log.info("No room for floorId={}", floorId);
			return List.of();
		}
		return rooms.stream().map(RoomResponse::from).toList();
	}

	@Override
	public void createRoom(Long floorId, RoomRequest roomCreateRequest) {
		log.info("Creating room '{}' in floorId={} (userId={}, status={})", roomCreateRequest.getName(), floorId,
				roomCreateRequest.getUserId(), roomCreateRequest.getStatus());

		validatorRoom(floorId, roomCreateRequest);

		Room room = Room.builder().floorId(floorId).userId(roomCreateRequest.getUserId())
				.name(roomCreateRequest.getName())
				.status(roomCreateRequest.getStatus() == null ? RoomStatus.AVAILABLE : roomCreateRequest.getStatus())
				.build();

		roomRepository.insert(room);
	}

	@Override
	public void updateRoom(Long floorId, Long roomId, RoomRequest req) {
		log.info("Updating roomId={} in floorId={} (name='{}', userId={}, status={})", roomId, floorId, req.getName(),
				req.getUserId(), req.getStatus());

		roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessage.ROOM_NOT_FOUND));

		if (roomRepository.existsByFloorIdAndNameExcludingId(floorId, req.getName(), roomId).orElse(false)) {
			throw new ResourceException(HttpStatus.CONFLICT, "Room name already exists in this floor");
		}

		Room room = Room.builder().id(roomId).floorId(floorId).name(req.getName()).userId(req.getUserId())
				.status(req.getStatus()).build();
		roomRepository.updateRoom(room);

	}

	@Override
	public void deleteRoom(Long floorId, Long roomId) {
		log.info("Deleting roomId={} in floorId={}", roomId, floorId);
		roomRepository.deleteByIdAndFloorId(roomId, floorId);

		log.info("Room deleted: roomId={}, floorId={}", roomId, floorId);
	}

	private void validatorRoom(Long floorId, RoomRequest roomCreateRequest) {
		roomRepository.floorExists(floorId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessage.FLOOR_NOT_FOUND));

		if (roomRepository.existsByFloorIdAndName(floorId, roomCreateRequest.getName()).orElse(false)) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, "Room name already exists in this floor");
		}

	}
}
