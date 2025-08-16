package vn.io.nghlong3004.apartment_management.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessage;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.RoomRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomResponse;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.repository.RoomRepository;
import vn.io.nghlong3004.apartment_management.service.RoomService;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;
	private final FloorRepository floorRepository;

	private static final Map<String, String> SORT_WHITELIST = Map.of("id", "id", "name", "name", "status", "status",
			"userId", "user_id", "created", "created", "updated", "updated");

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
	public PagedResponse<RoomResponse> getRoomsByFloor(Long floorId, int page, int size, String sort) {
		log.info("List rooms floorId={}, page={}, size={}, sort={}", floorId, page, size, sort);

		floorRepository.floorExists(floorId).filter(Boolean::booleanValue)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessage.FLOOR_NOT_FOUND));

		String orderBy = normalizeSort(sort);
		int offset = Math.max(page, 0) * Math.max(size, 1);

		long total = roomRepository.countByFloorId(floorId);
		List<Room> rooms = roomRepository.findPageByFloorId(floorId, orderBy, size, offset);

		List<RoomResponse> content = rooms.stream()
				.map(r -> new RoomResponse(r.getId(), r.getFloorId(), r.getUserId(), r.getName(), r.getStatus()))
				.toList();

		int totalPages = (int) Math.ceil(total / (double) size);

		return PagedResponse.<RoomResponse>builder().content(content).page(page).size(size).totalElements(total)
				.totalPages(totalPages).build();
	}

	@Override
	public void createRoom(Long floorId, RoomRequest roomCreateRequest) {
		log.info("Creating room '{}' in floorId={}", roomCreateRequest.getName(), floorId);

		validatorRoom(floorId, roomCreateRequest);

		Room room = Room.builder().floorId(floorId).name(roomCreateRequest.getName()).status(RoomStatus.AVAILABLE)
				.build();

		roomRepository.insert(room);
		floorRepository.incrementRoomCount(floorId);
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
		floorRepository.decrementRoomCount(floorId);

		log.info("Room deleted: roomId={}, floorId={}", roomId, floorId);
	}

	@Override
	public RoomResponse getRoomByName(Long floorId, String roomName) {
		log.info("Start retrieving room '{}' for floor {}", roomName, floorId);

		floorRepository.floorExists(floorId).filter(Boolean::booleanValue)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessage.FLOOR_NOT_FOUND));

		Room room = roomRepository.findByFloorIdAndName(floorId, roomName)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessage.ROOM_NOT_FOUND));

		log.debug("Retrieved room details: {}", room);

		return new RoomResponse(room.getId(), room.getFloorId(), room.getUserId(), room.getName(), room.getStatus());
	}

	private void validatorRoom(Long floorId, RoomRequest roomCreateRequest) {
		floorRepository.floorExists(floorId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessage.FLOOR_NOT_FOUND));
		if (roomRepository.existsByFloorIdAndName(floorId, roomCreateRequest.getName()).orElse(false)) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, "Room name already exists in this floor");
		}

	}

	private String normalizeSort(String sort) {
		if (sort == null || sort.isBlank())
			return "id ASC";
		String[] parts = sort.split(",");
		String field = parts[0].trim();
		String dir = (parts.length > 1 ? parts[1].trim() : "asc");

		String column = SORT_WHITELIST.getOrDefault(field, "id");
		String direction = switch (dir.toLowerCase()) {
		case "desc" -> "DESC";
		default -> "ASC";
		};

		return column + " " + direction;
	}
}
