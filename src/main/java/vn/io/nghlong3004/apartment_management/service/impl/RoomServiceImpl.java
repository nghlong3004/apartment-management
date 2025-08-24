package vn.io.nghlong3004.apartment_management.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.Floor;
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

	@Value("${apartment.management.room.max-number}")
	private long maxRoomNumber;

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
	@Transactional(readOnly = true)
	public RoomResponse getRoomResponse(Long floorId, Long roomId) {
		log.info("Fetching room response for floorId={}, roomId={}", floorId, roomId);

		Room room = getRoom(floorId, roomId);
		log.debug("Found room: id={}, name='{}', status={}", room.getId(), room.getName(), room.getStatus());

		RoomResponse response = RoomResponse.from(room);
		log.info("Successfully built RoomResponse for floorId={}, roomId={}", floorId, roomId);

		return response;
	}

	@Override
	@Transactional
	public void createRoom(Long floorId) {
		log.info("Start creating room in floorId={}", floorId);

		String nameRoom = getRoomName(floorId);

		Room room = Room.builder().floorId(floorId).name(nameRoom).status(RoomStatus.AVAILABLE).build();

		roomRepository.insert(room);
		log.info("Inserted new room with name='{}' into floorId={}", nameRoom, floorId);

		floorRepository.incrementRoomCount(floorId);

		log.info("Successfully created room name='{}' in floorId={}", nameRoom, floorId);
	}

	@Override
	@Transactional
	public void updateRoom(Long floorId, Long roomId, RoomRequest roomRequest) {
		log.info("Updating roomId={} in floorId={} with request(status={})", roomId, floorId, roomRequest.status());

		Room room = loadRoomByFloorIdAndRoomId(floorId, roomId);

		RoomStatus newStatus = resolveStatus(roomRequest, room);

		validateBasic(newStatus);
		validateConsistency(newStatus);

		room.setStatus(newStatus);

		roomRepository.updateRoom(room);

		log.info("Updated roomId={} in floorId={} -> userId={}, status={}", roomId, floorId, newStatus);
	}

	@Override
	@Transactional
	public void deleteRoom(Long floorId, Long roomId) {
		log.info("Deleting roomId={} in floorId={}", roomId, floorId);

		if (!roomRepository.existsByFloorIdAndRoomId(floorId, roomId).orElse(false)) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ROOM_NOT_FOUND);
		}

		roomRepository.deleteByIdAndFloorId(roomId, floorId);
		floorRepository.decrementRoomCount(floorId);

		log.info("Room deleted: roomId={}, floorId={}", roomId, floorId);
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<RoomResponse> getRooms(Long floorId, String name, int page, int size, String sort) {
		log.info("Rooms query: floorId={}, name='{}', page={}, size={}, sort={}", floorId, name, page, size, sort);

		floorRepository.floorExists(floorId).filter(Boolean::booleanValue)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.FLOOR_NOT_FOUND));

		if (name != null && !name.isBlank()) {
			return getRoomByName(name, floorId);
		}
		return getListRoom(floorId, page, size, sort);
	}

	private PagedResponse<RoomResponse> getListRoom(Long floorId, int page, int size, String sort) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		log.info("Fetching room list for floorId={}, page={}, size={}, sort={}", floorId, page, size, sort);

		String orderBy = HelperUtil.normalizeSort(sort);
		int safePage = Math.max(page, 0);
		int safeSize = Math.max(size, 1);
		int offset = safePage * safeSize;

		log.debug("Normalized params: safePage={}, safeSize={}, orderBy='{}', offset={}", safePage, safeSize, orderBy,
				offset);

		long total = roomRepository.countByFloorId(floorId);
		log.debug("Total rooms found for floorId={}: {}", floorId, total);

		List<Room> rooms = total == 0 ? List.of()
				: roomRepository.findPageByFloorId(floorId, orderBy, safeSize, offset);

		List<RoomResponse> content = rooms.stream().map(RoomResponse::from).toList();

		int totalPages = (int) Math.ceil(total / (double) safeSize);

		PagedResponse<RoomResponse> resp = PagedResponse.<RoomResponse>builder().content(content).page(safePage)
				.size(safeSize).totalElements(total).totalPages(Math.max(totalPages, 1)).build();

		stopWatch.stop();
		log.info("Successfully fetched {} rooms (of total={}) for floorId={}, totalPages={}, Elapsed time={} ms",
				content.size(), total, floorId, totalPages, stopWatch.getTotalTimeMillis());

		return resp;
	}

	private PagedResponse<RoomResponse> getRoomByName(String currentName, Long floorId) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		log.info("Fetching room by name='{}' in floorId={}", currentName, floorId);

		final String name = currentName.trim();
		log.debug("Normalized room name='{}'", name);

		Room room = roomRepository.findByFloorIdAndName(floorId, name).orElseThrow(() -> {
			log.error("Room not found with name='{}' in floorId={}", name, floorId);
			return new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.ROOM_NOT_FOUND);
		});

		log.debug("Found room: id={}, name='{}', floorId={}", room.getId(), room.getName(), room.getFloorId());

		RoomResponse dto = RoomResponse.from(room);

		PagedResponse<RoomResponse> resp = PagedResponse.<RoomResponse>builder().content(List.of(dto)).page(0).size(1)
				.totalElements(1L).totalPages(1).build();

		stopWatch.stop();
		log.info("Successfully fetched room by name='{}' in floorId={}, elapsedMs={}", name, floorId,
				stopWatch.getTotalTimeMillis());

		return resp;
	}

	private String getRoomName(Long floorId) {
		log.info("Start generating room name for floorId={}", floorId);

		Floor floor = floorRepository.findById(floorId).orElseThrow(() -> {
			log.error("Floor not found with id={}", floorId);
			return new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.FLOOR_NOT_FOUND);
		});

		Integer roomNumber = floor.getRoomCount() + 1;
		if (roomNumber >= maxRoomNumber) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ROOM_NUMBER_EXCEEDS_LIMIT);
		}
		int floorNumber = HelperUtil.parseFloorNumber(floor.getName());

		log.debug("Parsed floor info: id={}, name={}, floorNumber={}, currentRoomCount={}, nextRoomNumber={}",
				floor.getId(), floor.getName(), floorNumber, floor.getRoomCount(), roomNumber);

		if (floorNumber == 0) {
			log.error("Invalid floor number parsed from floorName='{}'", floor.getName());
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_FLOOR_NUMBER);
		}

		String roomName = HelperUtil.generateRoomName(floorNumber, roomNumber);
		log.info("Generated roomName='{}' for floorId={}", roomName, floorId);

		return roomName;
	}

	private Room loadRoomByFloorIdAndRoomId(Long floorId, Long roomId) {
		return roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId).orElseThrow(() -> {
			log.error("Room not found: floorId={}, roomId={}", floorId, roomId);
			return new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.ROOM_NOT_FOUND);
		});
	}

	private RoomStatus resolveStatus(RoomRequest req, Room room) {
		return (req.status() != null) ? req.status() : room.getStatus();
	}

	private void validateBasic(RoomStatus status) {
		if (status == null) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_ROOM_STATUS);
		}
	}

	private void validateConsistency(RoomStatus status) {
		if (status == RoomStatus.SOLD) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INCONSISTENT_ROOM_STATE);
		}
	}

}
