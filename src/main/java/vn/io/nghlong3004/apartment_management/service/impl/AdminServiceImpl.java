package vn.io.nghlong3004.apartment_management.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.Floor;
import vn.io.nghlong3004.apartment_management.model.Role;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserStatus;
import vn.io.nghlong3004.apartment_management.model.dto.FloorManagerRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomOwnerRequest;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.repository.RoomRepository;
import vn.io.nghlong3004.apartment_management.repository.UserRepository;
import vn.io.nghlong3004.apartment_management.service.AdminService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

	private final UserRepository userRepository;

	private final FloorRepository floorRepository;

	private final RoomRepository roomRepository;

	@Override
	@Transactional
	public void setManager(Long floorId, FloorManagerRequest request) {
		log.info("Start set manager by user id: {}", request.userId());
		User user = getActiveUserById(request.userId());

		if (user.getRole() == Role.MANAGER) {
			log.warn("User {} is already a manager (floorId={})", user.getId(), floorId);
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ALREADY_A_MANAGER);
		}
		Floor floor = floorRepository.findById(floorId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.FLOOR_NOT_FOUND));
		if (floor.getManagerId() != null) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.MANAGER_FLOOR);
		}

		user.setRole(Role.MANAGER);

		userRepository.update(user);

		floorRepository.updateManager(floorId, user.getId());

		log.info("Success update manager by user id: {}", request.userId());
	}

	@Override
	@Transactional
	public void deleteManager(Long floorId, @Valid FloorManagerRequest request) {
		log.info("Start delete manager id: {}", request.userId());
		User user = getActiveUserById(request.userId());

		if (user.getRole() != Role.MANAGER) {
			log.warn("User {} is not a manager (floorId={})", user.getId(), floorId);
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.NOT_MANAGER);
		}

		Floor floor = floorRepository.findById(floorId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.FLOOR_NOT_FOUND));
		if (floor.getManagerId() != user.getId()) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.NOT_MANAGER_FLOOR);
		}

		user.setRole(Role.USER);

		userRepository.update(user);

		floorRepository.updateManager(floorId, null);

		log.info("Success delete manager id: {}", request.userId());
	}

	@Override
	@Transactional
	public void setOwner(Long roomId, RoomOwnerRequest request) {
		log.info("Start set owner room with roomId{} for userId: {}", roomId, request.userId());
		User user = getActiveUserById(request.userId());

		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ROOM_NOT_FOUND));
		if (room.getUserId() != null || room.getStatus() != RoomStatus.AVAILABLE) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.OCCUPIED_ROOM);
		}

		updateOldRoom(user.getId());

		room.setUserId(user.getId());
		room.setStatus(RoomStatus.SOLD);
		roomRepository.updateRoom(room);

		log.info("Success set owner room with roomId{} for userId: {}", roomId, request.userId());
	}

	@Override
	@Transactional
	public void deleteOwner(Long roomId, RoomOwnerRequest request) {
		log.info("Start delete owner room with roomId{} for userId: {}", roomId, request.userId());

		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ROOM_NOT_FOUND));
		if (room.getUserId() != request.userId()) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.NOT_OWNER_ROOM);
		}

		room.setUserId(null);
		room.setStatus(RoomStatus.AVAILABLE);

		roomRepository.updateRoom(room);

		log.info("Success delete owner room with roomId{} for userId: {}", roomId, request.userId());

	}

	private User getActiveUserById(Long userId) {
		log.info("Start get active user with ID{} ", userId);
		User user = userRepository.findById(userId).orElseThrow(() -> {
			log.warn("User not found with id={}", userId);
			return new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ID_NOT_FOUND);
		});

		if (user.getStatus() == UserStatus.INACTIVE) {
			log.warn("User banned with id={}", user.getId());
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.USER_BANNED);
		}
		log.info("Success get active user with ID{} ", userId);
		return user;
	}

	private void updateOldRoom(Long id) {
		Room room = roomRepository.findByUserId(id);
		if (room != null) {
			log.info("Start update old room with ID{} for userId: {}", room.getId(), id);
			room.setUserId(null);
			room.setStatus(RoomStatus.AVAILABLE);
			roomRepository.updateRoom(room);
			log.info("Success update old room with ID{} for userId: {}", room.getId(), id);
		}
	}

}
