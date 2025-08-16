package vn.io.nghlong3004.apartment_management.service.validator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.RequestType;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class FloorServiceValidator {

	private final FloorRepository floorRepository;

	public void ensureNoPendingRequestForSelf(Long userId, RequestType type) {
		if (floorRepository.existsPendingRequest(userId, type).orElse(false)) {
			log.warn("Validation failed: userId={} already has a pending {} request", userId, type);
			if (type == RequestType.JOIN) {
				throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.PENDING_REQUEST_EXISTS);
			}
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.USER_PENDING_REQUEST);
		}
		log.debug("Validation passed: no pending {} request for userId={}", type, userId);
	}

	public void ensureNoPendingRequestForOther(Long userId, RequestType type) {
		if (floorRepository.existsPendingRequest(userId, type).orElse(false)) {
			log.warn("Validation failed: target userId={} already has a pending {} request", userId, type);
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.PERSON_PENDING_REQUEST);
		}
		log.debug("Validation passed: no pending {} request for target userId={}", type, userId);
	}

	public void ensureRoomAvailable(RoomStatus status) {
		if (status != RoomStatus.AVAILABLE) {
			log.warn("Validation failed: room is not AVAILABLE. currentStatus={}", status);
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ROOM_ALREADY_RESERVED);
		}
		log.debug("Validation passed: room is AVAILABLE");
	}

	public void ensureRoomMovable(RoomStatus status) {
		if (status == RoomStatus.RESERVED) {
			log.warn("Validation failed: cannot move to a RESERVED room.");
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ROOM_MOVE_NOT_ALLOWED);
		}
		log.debug("Validation passed: room is movable with status={}", status);
	}
}
