package vn.io.nghlong3004.apartment_management.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessage;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.Floor;
import vn.io.nghlong3004.apartment_management.model.RequestStatus;
import vn.io.nghlong3004.apartment_management.model.RequestType;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.model.dto.FloorCreateRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;
import vn.io.nghlong3004.apartment_management.model.dto.FloorUpdateRequest;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.service.FloorService;
import vn.io.nghlong3004.apartment_management.service.RoomService;
import vn.io.nghlong3004.apartment_management.service.validator.FloorServiceValidator;
import vn.io.nghlong3004.apartment_management.util.SecurityUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloorServiceImpl implements FloorService {

	private final FloorRepository floorRepository;
	private final FloorServiceValidator floorValidator;
	private final RoomService roomService;

	@Override
	public void createJoinRequest(Long floorId, Long roomId) {
		final Long userId = SecurityUtil.getCurrentUserId()
				.orElseThrow(() -> new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessage.ID_NOT_FOUND));
		log.info("Create join request: userId={}, floorId={}, roomId={}", userId, floorId, roomId);

		floorValidator.ensureNoPendingRequestForSelf(userId, RequestType.JOIN);

		Room room = roomService.getRoom(floorId, roomId);
		floorValidator.ensureRoomAvailable(room.getStatus());

		floorRepository.createRequest(userId, floorId, roomId, RequestType.JOIN, RequestStatus.PENDING);
		log.debug("Join request persisted: userId={}, floorId={}, roomId={}, status={}", userId, floorId, roomId,
				RequestStatus.PENDING);

		roomService.reserveRoom(room, userId);
		log.info("Join request created & room reserved: userId={}, roomId={}, newStatus={}", userId, room.getId(),
				RoomStatus.RESERVED);
	}

	@Override
	public void createMoveRequest(Long floorId, Long roomId) {
		final Long userId = SecurityUtil.getCurrentUserId()
				.orElseThrow(() -> new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessage.ID_NOT_FOUND));
		log.info("Create move request: userId={}, floorId={}, roomId={}", userId, floorId, roomId);

		floorValidator.ensureNoPendingRequestForSelf(userId, RequestType.MOVE);

		Room room = roomService.getRoom(floorId, roomId);

		if (userId.equals(room.getUserId())) {
			log.warn("Move request rejected: user tries to move into own room. userId={}, roomId={}", userId, roomId);
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessage.MOVE_TO_OWN_ROOM_NOT_ALLOWED);
		}

		floorValidator.ensureRoomMovable(room.getStatus());

		floorValidator.ensureNoPendingRequestForOther(room.getUserId(), RequestType.MOVE);

		floorRepository.createRequest(userId, floorId, roomId, RequestType.MOVE, RequestStatus.PENDING);
		log.info("Move request created: requesterId={}, targetRoomId={}, status={}", userId, roomId,
				RequestStatus.PENDING);
	}

	@Override
	public FloorResponse getFloorWithRooms(Long floorId) {
		log.info("Retrieving floor details for floorId={}", floorId);

		Floor floor = floorRepository.findById(floorId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessage.FLOOR_NOT_FOUND));

		return FloorResponse.from(floor, roomService.getAllRooms(floorId));
	}

	@Override
	public void deleteFloor(Long floorId) {
		log.info("Deleting floor floorId={}", floorId);

		floorRepository.deleteById(floorId);

		log.debug("Floor deleted: floorId={}", floorId);
	}

	public void updateFloor(Long floorId, FloorUpdateRequest floorUpdateRequest) {
		log.info("Updating floor with floorId={}", floorId);

		Floor existingFloor = floorRepository.findById(floorId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessage.FLOOR_NOT_FOUND));

		existingFloor.setName(floorUpdateRequest.getName());
		existingFloor.setManagerId(floorUpdateRequest.getManagerId());
		existingFloor.setRoomCount(floorUpdateRequest.getRoomCount());

		floorRepository.updateFloor(existingFloor);

		log.debug("Floor updated successfully: floorId={}", floorId);

	}

	@Override
	public void addFloor(FloorCreateRequest floorCreateRequest) {
		log.info("Creating floor name = {}", floorCreateRequest.getName());

		Floor floor = Floor.builder().name(floorCreateRequest.getName())
				.roomCount(floorCreateRequest.getRoomCount() == null ? 0 : floorCreateRequest.getRoomCount()).build();

		floorRepository.insert(floor);

		log.debug("Floor created successfully id={}", floor.getId());
	}
}
