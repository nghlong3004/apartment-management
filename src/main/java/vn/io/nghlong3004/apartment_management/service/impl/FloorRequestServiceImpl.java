package vn.io.nghlong3004.apartment_management.service.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.Floor;
import vn.io.nghlong3004.apartment_management.model.FloorRequest;
import vn.io.nghlong3004.apartment_management.model.RequestStatus;
import vn.io.nghlong3004.apartment_management.model.Role;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.dto.CreateRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorRequestSummary;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.UpdateRequest;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.repository.FloorRequestRepository;
import vn.io.nghlong3004.apartment_management.repository.RoomRepository;
import vn.io.nghlong3004.apartment_management.repository.UserRepository;
import vn.io.nghlong3004.apartment_management.service.FloorRequestService;
import vn.io.nghlong3004.apartment_management.util.HelperUtil;
import vn.io.nghlong3004.apartment_management.util.SecurityUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloorRequestServiceImpl implements FloorRequestService {

	private final FloorRequestRepository floorRequestRepository;
	private final FloorRepository floorRepository;
	private final RoomRepository roomRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public void create(CreateRequest request) {
		log.info("Start creating request by requesterId={}, counterpartRoomId={}, counterpartId={}",
				request.requesterId(), request.counterpartRoomId(), request.counterpartId());

		if (request.counterpartId() != null && request.counterpartId().equals(request.requesterId())) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.MOVE_TO_OWN_ROOM_NOT_ALLOWED);
		}

		validateUserPermission(request.requesterId(), request.counterpartRoomId());
		validateRequesterRoom(request.requesterId(), request.requesterRoomId());
		validateCounterpartRoom(request.counterpartRoomId(), request.counterpartId());

		validateNoActiveRequestsForRequester(request.requesterId());
		validateNoActiveRequestsForCounterpart(request.requesterId());
		validateNoActiveRequestsForRequester(request.counterpartId());
		validateNoActiveRequestsForCounterpart(request.counterpartId());

		FloorRequest floorRequest = FloorRequest.of(request);
		floorRequestRepository.insert(floorRequest);

		log.info("Successfully created request with requesterId={}, counterpartRoomId={}", request.requesterId(),
				request.counterpartRoomId());
	}

	@Override
	@Transactional
	public void update(Long floorRequestId, UpdateRequest request) {
		log.info("Update request {} -> {}", floorRequestId, request.status());

		FloorRequest floorRequest = floorRequestRepository.findById(floorRequestId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.REQUEST_NOT_FOUND));
		if (floorRequest.getStatus() == request.status()) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.REQUEST_ALREADY_IN_STATUS);
		}
		switch (request.status()) {
		case CANCELLED -> {
			assertCanCancel(floorRequest);
			applyCancel(floorRequest);
		}
		case ACCEPTED -> {
			assertCanAccept(floorRequest);
			applyAccept(floorRequest);
		}
		case DECLINED -> {
			assertCanDecline(floorRequest);
			applyDecline(floorRequest, request.reason());
		}
		case REJECTED -> {
			assertCanReject(floorRequest);
			applyReject(floorRequest, request.reason());
		}
		case APPROVED -> {
			assertCanApprove(floorRequest);
			performDomainActionOnApprove(floorRequest);
			applyApprove(floorRequest);
		}
		default -> throw new ResourceException(HttpStatus.BAD_REQUEST, "Unsupported status: " + request.status());
		}
		floorRequestRepository.update(floorRequest);
		log.info("Updated request {} to status {}", floorRequestId, floorRequest.getStatus());
	}

	private void performDomainActionOnApprove(FloorRequest floorRequest) {
		log.info("Start change user{} -> room{}", floorRequest.getCounterpartId(), floorRequest.getRequesterRoomId());
		changeRoomOwner(floorRequest.getRequesterRoomId(), floorRequest.getCounterpartId());
		log.info("Success change user{} -> room{}", floorRequest.getCounterpartId(), floorRequest.getRequesterRoomId());
		log.info("Start change user{} -> room{}", floorRequest.getRequesterId(), floorRequest.getCounterpartRoomId());
		changeRoomOwner(floorRequest.getCounterpartRoomId(), floorRequest.getRequesterId());
		log.info("Success change user{} -> room{}", floorRequest.getRequesterId(), floorRequest.getCounterpartRoomId());
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<FloorRequestSummary> getRequests(int page, int size, String sort) {
		log.info("Listing requests page={}, size={}, sort={}", page, size, sort);

		long t0 = System.nanoTime();
		int safePage = Math.max(page, 0);
		int safeSize = Math.max(size, 1);
		int offset = safePage * safeSize;
		String orderBy = HelperUtil.normalizeSort(sort);

		long total = floorRequestRepository.countAll();
		List<FloorRequest> rows = total == 0 ? List.of() : floorRequestRepository.findPage(orderBy, safeSize, offset);

		List<FloorRequestSummary> content = rows.stream().map(FloorRequestSummary::from).toList();

		int totalPages = (int) Math.ceil(total / (double) safeSize);

		log.debug("Requests(list) -> fetched={}, total={}, timeMs={}", content.size(), total,
				(System.nanoTime() - t0) / 1_000_000.0);

		return PagedResponse.<FloorRequestSummary>builder().content(content).page(safePage).size(safeSize)
				.totalElements(total).totalPages(Math.max(totalPages, 1)).build();
	}

	private void changeRoomOwner(Long roomId, Long newOwnerRoomId) {
		if (roomId == null) {
			return;
		}
		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.ROOM_NOT_FOUND));
		room.setUserId(newOwnerRoomId);
		if (room.getStatus() == RoomStatus.AVAILABLE) {
			room.setStatus(RoomStatus.SOLD);
		}
		if (newOwnerRoomId == null) {
			room.setStatus(RoomStatus.AVAILABLE);
			roomRepository.updateRoom(room);
			return;
		}
		roomRepository.updateRoom(room);
		User user = userRepository.findById(newOwnerRoomId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.ID_NOT_FOUND));
		user.setFloorId(room.getFloorId());
		userRepository.update(user);
	}

	private void assertCanApprove(FloorRequest floorRequest) {
		if (!isAdmin() && !isManager(floorRequest.getCounterpartRoomId())
				&& !isManager(floorRequest.getRequesterId())) {
			throw new ResourceException(HttpStatus.FORBIDDEN, ErrorMessageConstant.ACTION_FORBIDDEN);
		}
		if (floorRequest.getStatus() != RequestStatus.ACCEPTED && floorRequest.getCounterpartId() != null) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_STATE);
		}
	}

	private void assertCanReject(FloorRequest floorRequest) {
		if (!isAdmin() && !isManager(floorRequest.getCounterpartRoomId())
				&& !isManager(floorRequest.getRequesterId())) {
			throw new ResourceException(HttpStatus.FORBIDDEN, ErrorMessageConstant.ACTION_FORBIDDEN);
		}
		if (floorRequest.getStatus() != RequestStatus.ACCEPTED && floorRequest.getStatus() != RequestStatus.PENDING) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_STATE);
		}
	}

	private void assertCanAccept(FloorRequest floorRequest) {
		if (!(isAdmin() || isSelf(floorRequest.getCounterpartId()))) {
			throw new ResourceException(HttpStatus.FORBIDDEN, ErrorMessageConstant.ACTION_FORBIDDEN);
		}
		if (floorRequest.getStatus() != RequestStatus.PENDING) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_STATE);
		}
	}

	private void assertCanDecline(FloorRequest floorRequest) {
		if (!(isAdmin() || isSelf(floorRequest.getCounterpartId()))) {
			throw new ResourceException(HttpStatus.FORBIDDEN, ErrorMessageConstant.ACTION_FORBIDDEN);
		}
		if (floorRequest.getStatus() != RequestStatus.PENDING) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_STATE);
		}
	}

	private void assertCanCancel(FloorRequest floorRequest) {
		if (!(isAdmin() || isSelf(floorRequest.getRequesterId()))) {
			throw new ResourceException(HttpStatus.FORBIDDEN, ErrorMessageConstant.ACTION_FORBIDDEN);
		}
		if (floorRequest.getStatus() != RequestStatus.PENDING) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_STATE);
		}
	}

	private void applyApprove(FloorRequest floorRequest) {
		floorRequest.setStatus(RequestStatus.APPROVED);
		floorRequest.setApproverId(getUserId());
	}

	private void applyReject(FloorRequest floorRequest, String reason) {
		floorRequest.setStatus(RequestStatus.REJECTED);
		floorRequest.setApproverId(getUserId());
		floorRequest.setClosedReason(reason);
	}

	private void applyAccept(FloorRequest floorRequest) {
		floorRequest.setStatus(RequestStatus.ACCEPTED);
		if (isAdmin())
			floorRequest.setApproverId(getUserId());
	}

	private void applyDecline(FloorRequest floorRequest, String reason) {
		floorRequest.setStatus(RequestStatus.DECLINED);
		if (isAdmin())
			floorRequest.setApproverId(getUserId());
		floorRequest.setClosedReason(reason);
	}

	private void applyCancel(FloorRequest floorRequest) {
		floorRequest.setStatus(RequestStatus.CANCELLED);
		if (isAdmin())
			floorRequest.setApproverId(getUserId());
	}

	private void validateUserPermission(Long requesterId, Long roomId) {
		log.info("Validating user permission requestId={}", requesterId);
		if (!isAdmin() && !isSelf(requesterId) && !isManager(roomId)) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.FORBIDDEN);
		}
	}

	private void validateRequesterRoom(Long requesterId, Long requesterRoomId) {
		log.info("Validating requester roomId={} requesterId={} ", requesterRoomId, requesterId);
		Room room = roomRepository.findById(requesterId).orElse(null);

		if (room != null) {
			if (room.getUserId() != requesterId) {
				throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ROOM_OWNER_MISMATCH);
			}
		}

	}

	private void validateNoActiveRequestsForRequester(Long requesterId) {
		if (floorRequestRepository.existsActiveByRequesterId(requesterId)) {
			throw new ResourceException(HttpStatus.BAD_REQUEST,
					ErrorMessageConstant.ACTIVE_REQUEST_EXISTS_FOR_REQUESTER);
		}
	}

	private void validateNoActiveRequestsForCounterpart(Long counterpartId) {
		if (floorRequestRepository.existsActiveByCounterpartId(counterpartId)) {
			throw new ResourceException(HttpStatus.BAD_REQUEST,
					ErrorMessageConstant.ACTIVE_REQUEST_EXISTS_FOR_COUNTERPART);
		}
	}

	private void validateCounterpartRoom(Long counterpartRoomId, Long counterpartId) {
		log.debug("Validating counterpart roomId={} counterpartId={}", counterpartRoomId, counterpartId);

		Room room = roomRepository.findById(counterpartRoomId).orElseThrow(() -> {
			log.error("Room not found: counterpartRoomId={}", counterpartRoomId);
			return new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ROOM_NOT_FOUND);
		});

		if (counterpartId == null) {
			if (room.getStatus() != RoomStatus.AVAILABLE) {
				throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ROOM_NOT_AVAILABLE);
			}
			counterpartId = 0L;
		}

		if (room.getUserId() != null && !counterpartId.equals(room.getUserId())) {
			log.warn("Room owner mismatch: roomId={}, expectedUserId={}, actualUserId={}", counterpartRoomId,
					counterpartId, room.getUserId());
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ROOM_OWNER_MISMATCH);
		}

		log.debug("Counterpart room validated: roomId={}", counterpartRoomId);
	}

	private boolean isAdmin() {
		return SecurityUtil.hasRole(Role.ADMIN.name());
	}

	private boolean isManager(Long roomId) {
		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ROOM_NOT_FOUND));
		Floor floor = floorRepository.findById(room.getFloorId())
				.orElseThrow(() -> new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.FLOOR_NOT_FOUND));
		Long managerId = getUserId();
		return SecurityUtil.hasRole(Role.MANAGER.name()) && floor.getManagerId().equals(managerId);
	}

	private boolean isSelf(Long id) {
		Long actorId = getUserId();
		return actorId != null && actorId.equals(id);
	}

	private Long getUserId() {
		Long actorId = SecurityUtil.getCurrentUserId().orElseThrow(() -> {
			log.error("Actor ID not found in security context");
			return new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ID_NOT_FOUND);
		});
		return actorId;
	}

}
