package vn.io.nghlong3004.apartment_management.service.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.RoomRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomResponse;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.repository.RoomRepository;
import vn.io.nghlong3004.apartment_management.service.RoomService;
import vn.io.nghlong3004.apartment_management.util.HelperUtil;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;
	private final FloorRepository floorRepository;

	@Override
	@Transactional(readOnly = true)
	public Room getRoom(Long floorId, Long roomId) {
		return roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId).map(room -> {
			log.debug("Room found: floorId={}, roomId={}, status={}, userId={}", floorId, roomId, room.getStatus(),
					room.getUserId());
			return room;
		}).orElseThrow(() -> {
			log.warn("Room not found: floorId={}, roomId={}", floorId, roomId);
			return new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.ROOM_NOT_FOUND);
		});
	}

	@Override
	@Transactional
	public void reserveRoom(Room room, Long userId) {
		log.debug("Reserving room: roomId={}, currentStatus={}, newUserId={}", room.getId(), room.getStatus(), userId);
		room.setStatus(RoomStatus.RESERVED);
		room.setUserId(userId);
		roomRepository.updateRoom(room);
		log.info("Room reserved: roomId={}, newStatus={}, userId={}", room.getId(), room.getStatus(), userId);
	}

	@Override
	@Transactional(readOnly = true)
	public RoomResponse getRoomResponse(Long floorId, Long roomId) {

		Room room = getRoom(floorId, roomId);

		return RoomResponse.from(room);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Room> getAllRooms(Long floorId) {
		log.info("Fetching all rooms for floorId={}", floorId);
		List<Room> rooms = roomRepository.findAllRoomsByFloorId(floorId);
		if (rooms == null || rooms.isEmpty()) {
			return List.of();
		}
		return rooms;
	}

	@Override
	@Transactional
	public void createRoom(Long floorId, RoomRequest roomCreateRequest) {
		log.info("Creating room '{}' in floorId={}", roomCreateRequest.name(), floorId);

		validatorRoom(floorId, roomCreateRequest);

		Room room = Room.builder().floorId(floorId).name(roomCreateRequest.name()).status(RoomStatus.AVAILABLE).build();

		roomRepository.insert(room);
		floorRepository.incrementRoomCount(floorId);
	}

	@Override
	@Transactional
	public void updateRoom(Long floorId, Long roomId, RoomRequest req) {
		log.info("Updating roomId={} in floorId={} (name='{}', userId={}, status={})", roomId, floorId, req.name(),
				req.userId(), req.status());

		roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.ROOM_NOT_FOUND));

		if (roomRepository.existsByFloorIdAndNameExcludingId(floorId, req.name(), roomId).orElse(false)) {
			throw new ResourceException(HttpStatus.CONFLICT, ErrorMessageConstant.ROOM_ALREADY_NAME);
		}

		Room room = Room.builder().id(roomId).floorId(floorId).name(req.name()).userId(req.userId())
				.status(req.status()).build();
		roomRepository.updateRoom(room);

	}

	@Override
	@Transactional
	public void deleteRoom(Long floorId, Long roomId) {
		log.info("Deleting roomId={} in floorId={}", roomId, floorId);

		roomRepository.deleteByIdAndFloorId(roomId, floorId);
		floorRepository.decrementRoomCount(floorId);

		log.info("Room deleted: roomId={}, floorId={}", roomId, floorId);
	}

	@Override
	@Transactional
	public PagedResponse<RoomResponse> getRooms(Long floorId, String name, int page, int size, String sort) {
		log.info("Rooms query: floorId={}, name='{}', page={}, size={}, sort={}", floorId, name, page, size, sort);

		floorRepository.floorExists(floorId).filter(Boolean::booleanValue)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.FLOOR_NOT_FOUND));

		if (name != null && !name.isBlank()) {
			return getRoomByName(name, floorId);
		}

		return getListRoom(floorId, page, size, sort);
	}

	private void validatorRoom(Long floorId, RoomRequest roomCreateRequest) {
		floorRepository.floorExists(floorId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.FLOOR_NOT_FOUND));
		if (roomRepository.existsByFloorIdAndName(floorId, roomCreateRequest.name()).orElse(false)) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ROOM_ALREADY_NAME);
		}

	}

	private PagedResponse<RoomResponse> getListRoom(Long floorId, int page, int size, String sort) {
		long t0 = System.nanoTime();
		String orderBy = HelperUtil.normalizeSort(sort);
		int safePage = Math.max(page, 0);
		int safeSize = Math.max(size, 1);
		int offset = safePage * safeSize;

		long total = roomRepository.countByFloorId(floorId);
		List<Room> rooms = total == 0 ? List.of()
				: roomRepository.findPageByFloorId(floorId, orderBy, safeSize, offset);

		List<RoomResponse> content = rooms.stream().map(RoomResponse::from).toList();

		int totalPages = (int) Math.ceil(total / (double) safeSize);

		PagedResponse<RoomResponse> resp = PagedResponse.<RoomResponse>builder().content(content).page(safePage)
				.size(safeSize).totalElements(total).totalPages(Math.max(totalPages, 1)).build();

		log.debug("Rooms(list) -> fetched={}, total={}, timeMs={}", content.size(), total,
				(System.nanoTime() - t0) / 1_000_000.0);
		return resp;
	}

	private PagedResponse<RoomResponse> getRoomByName(String currentName, Long floorId) {
		long t0 = System.nanoTime();
		final String name = currentName.trim();
		Room room = roomRepository.findByFloorIdAndName(floorId, name)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.ROOM_NOT_FOUND));

		RoomResponse dto = RoomResponse.from(room);

		PagedResponse<RoomResponse> resp = PagedResponse.<RoomResponse>builder().content(List.of(dto)).page(0).size(1)
				.totalElements(1L).totalPages(1).build();

		log.debug("Rooms(byName) -> 1 item, elapsedMs={}", (System.nanoTime() - t0) / 1_000_000.0);
		return resp;
	}
}
