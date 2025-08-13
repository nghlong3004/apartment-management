package vn.io.nghlong3004.apartment_management.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.exception.ErrorState;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.RequestStatus;
import vn.io.nghlong3004.apartment_management.model.RequestType;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.service.FloorService;
import vn.io.nghlong3004.apartment_management.util.SecurityUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloorServiceImpl implements FloorService {

	private final FloorRepository floorRepository;

	@Override
	public void createJoinRequest(Long floorId, Long roomId) {
		Long userId = SecurityUtil.getCurrentUserId()
				.orElseThrow(() -> new ResourceException(ErrorState.ACCESS_TOKEN_IS_WRONG));

		log.info("Create join request: userId={}, floorId={}, roomId={}", userId, floorId, roomId);

		validateNoPendingRequest(userId, RequestType.JOIN, true);

		Room room = getRoomOrThrow(floorId, roomId);
		log.debug("Room fetched for join request: id={}, status={}, currentUserId={}", room.getId(), room.getStatus(),
				room.getUserId());

		validateRoomAvailable(room.getStatus());

		floorRepository.createRequest(userId, floorId, roomId, RequestType.JOIN, RequestStatus.PENDING);
		log.debug("Join request persisted: userId={}, floorId={}, roomId={}, status={}", userId, floorId, roomId,
				RequestStatus.PENDING);

		room.setStatus(RoomStatus.RESERVED);
		room.setUserId(userId);
		floorRepository.updateRoom(room);

		log.info("Join request created and room reserved: userId={}, roomId={}, newStatus={}", userId, room.getId(),
				room.getStatus());
	}

	@Override
	public void createMoveRequest(Long floorId, Long roomId) {
		Long userId = SecurityUtil.getCurrentUserId()
				.orElseThrow(() -> new ResourceException(ErrorState.ACCESS_TOKEN_IS_WRONG));

		log.info("Create move request: userId={}, floorId={}, roomId={}", userId, floorId, roomId);

		validateNoPendingRequest(userId, RequestType.MOVE, true);

		Room room = getRoomOrThrow(floorId, roomId);
		log.debug("Room fetched for move request: id={}, status={}, currentUserId={}", room.getId(), room.getStatus(),
				room.getUserId());

		if (userId.equals(room.getUserId())) {
			log.warn("Move request rejected: user tries to move into own room. userId={}, roomId={}", userId, roomId);
			throw new ResourceException(ErrorState.UNABLE_TO_MOVE_TO_OWN_ROOM);
		}

		validateRoomMovable(room.getStatus());

		validateNoPendingRequest(room.getUserId(), RequestType.MOVE, false);

		floorRepository.createRequest(userId, floorId, roomId, RequestType.MOVE, RequestStatus.PENDING);
		log.info("Move request created: requesterId={}, targetRoomId={}, status={}", userId, roomId,
				RequestStatus.PENDING);
	}

	private void validateNoPendingRequest(Long userId, RequestType type, boolean self) {
		boolean exists = floorRepository.existsPendingRequest(userId, type).orElse(false);
		if (exists) {
			if (self) {
				log.warn("Validation failed: user already has a pending {} request. userId={}", type, userId);
				if (type == RequestType.JOIN) {
					throw new ResourceException(ErrorState.USER_HAS_PENDING_REQUEST);
				} else {
					throw new ResourceException(ErrorState.USER_OR_OTHER_HAS_PENDING_REQUEST);
				}
			} else {
				log.warn("Validation failed: target user already has a pending {} request. userId={}", type, userId);
				throw new ResourceException(ErrorState.USER_OR_OTHER_HAS_PENDING_REQUEST);
			}
		}
		log.debug("Validation passed: no pending {} request for userId={}", type, userId);
	}

	private void validateRoomAvailable(RoomStatus status) {
		if (status != RoomStatus.AVAILABLE) {
			log.warn("Validation failed: room is not AVAILABLE. currentStatus={}", status);
			throw new ResourceException(ErrorState.ROOM_EXCEPTION);
		}
		log.debug("Validation passed: room is AVAILABLE");
	}

	private void validateRoomMovable(RoomStatus status) {
		if (status == RoomStatus.RESERVED) {
			log.warn("Validation failed: cannot move to a RESERVED room.");
			throw new ResourceException(ErrorState.ROOM_MOVE_EXCEPTION);
		}
		log.debug("Validation passed: room is movable with status={}", status);
	}

	private Room getRoomOrThrow(Long floorId, Long roomId) {
		Room room = floorRepository.findByFloorIdAndRoomId(floorId, roomId).orElseThrow(() -> {
			log.warn("Room not found: floorId={}, roomId={}", floorId, roomId);
			return new ResourceException(ErrorState.ROOM_NOT_EXISTS);
		});
		return room;
	}
}
