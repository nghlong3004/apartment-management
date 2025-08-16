package vn.io.nghlong3004.apartment_management.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.RequestStatus;
import vn.io.nghlong3004.apartment_management.model.RequestType;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.service.RoomService;
import vn.io.nghlong3004.apartment_management.service.validator.FloorServiceValidator;
import vn.io.nghlong3004.apartment_management.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class FloorServiceImplTest {

	private final FloorRepository floorRepository = Mockito.mock(FloorRepository.class);
	private final FloorServiceValidator floorValidator = Mockito.mock(FloorServiceValidator.class);
	private final RoomService roomService = Mockito.mock(RoomService.class);

	private final FloorServiceImpl floorService = new FloorServiceImpl(floorRepository, floorValidator, roomService);

	private Room sampleRoom(Long id, Long floorId, Long userId, String name, RoomStatus status) {
		return Room.builder().id(id).floorId(floorId).userId(userId).name(name).status(status).build();
	}

	@Test
	@DisplayName("Method: createJoinRequest -> throws ID_NOT_FOUND when no current user")
	void createJoinRequest_NoCurrentUser_ShouldThrow() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.empty());

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> floorService.createJoinRequest(10L, 77L));
			Assertions.assertThat(ex.getMessage()).isEqualTo(ErrorMessageConstant.ID_NOT_FOUND);

			verify(floorRepository, never()).createRequest(any(), any(), any(), any(), any());
			verify(roomService, never()).reserveRoom(any(Room.class), any());
		}
	}

	@Test
	@DisplayName("Method: createJoinRequest -> success creates request and reserves room")
	void createJoinRequest_Success_ShouldPersistAndReserve() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			Long userId = Math.abs(UUID.randomUUID().getMostSignificantBits());
			Long floorId = 5L;
			Long roomId = 9L;

			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(userId));

			Room room = sampleRoom(roomId, floorId, null, "R-9", RoomStatus.AVAILABLE);
			when(roomService.getRoom(floorId, roomId)).thenReturn(room);
			Mockito.doNothing().when(floorValidator).ensureNoPendingRequestForSelf(userId, RequestType.JOIN);
			Mockito.doNothing().when(floorValidator).ensureRoomAvailable(RoomStatus.AVAILABLE);

			floorService.createJoinRequest(floorId, roomId);

			verify(floorValidator).ensureNoPendingRequestForSelf(userId, RequestType.JOIN);
			verify(roomService).getRoom(floorId, roomId);
			verify(floorValidator).ensureRoomAvailable(RoomStatus.AVAILABLE);
			verify(floorRepository).createRequest(userId, floorId, roomId, RequestType.JOIN, RequestStatus.PENDING);
			verify(roomService).reserveRoom(room, userId);
		}
	}

	@Test
	@DisplayName("Method: createJoinRequest -> throws PENDING_REQUEST_EXISTS when self has pending JOIN")
	void createJoinRequest_PendingExists_ShouldThrow() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			Long userId = 1L;
			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(userId));

			Mockito.doThrow(new ResourceException(org.springframework.http.HttpStatus.BAD_REQUEST,
					ErrorMessageConstant.PENDING_REQUEST_EXISTS)).when(floorValidator)
					.ensureNoPendingRequestForSelf(userId, RequestType.JOIN);

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> floorService.createJoinRequest(2L, 3L));
			Assertions.assertThat(ex.getMessage()).isEqualTo(ErrorMessageConstant.PENDING_REQUEST_EXISTS);

			verify(roomService, never()).getRoom(any(), any());
			verify(floorRepository, never()).createRequest(any(), any(), any(), any(), any());
			verify(roomService, never()).reserveRoom(any(Room.class), any());
		}
	}

	@Test
	@DisplayName("Method: createJoinRequest -> throws ROOM_ALREADY_RESERVED when room not AVAILABLE")
	void createJoinRequest_RoomNotAvailable_ShouldThrow() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			Long userId = 1L;
			Long floorId = 7L;
			Long roomId = 8L;

			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(userId));
			Mockito.doNothing().when(floorValidator).ensureNoPendingRequestForSelf(userId, RequestType.JOIN);

			Room room = sampleRoom(roomId, floorId, null, "R-8", RoomStatus.RESERVED);
			when(roomService.getRoom(floorId, roomId)).thenReturn(room);
			Mockito.doThrow(new ResourceException(org.springframework.http.HttpStatus.BAD_REQUEST,
					ErrorMessageConstant.ROOM_ALREADY_RESERVED)).when(floorValidator).ensureRoomAvailable(RoomStatus.RESERVED);

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> floorService.createJoinRequest(floorId, roomId));
			Assertions.assertThat(ex.getMessage()).isEqualTo(ErrorMessageConstant.ROOM_ALREADY_RESERVED);

			verify(floorRepository, never()).createRequest(any(), any(), any(), any(), any());
			verify(roomService, never()).reserveRoom(any(Room.class), any());
		}
	}

	@Test
	@DisplayName("Method: createMoveRequest -> throws ID_NOT_FOUND when no current user")
	void createMoveRequest_NoCurrentUser_ShouldThrow() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.empty());

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> floorService.createMoveRequest(10L, 77L));
			Assertions.assertThat(ex.getMessage()).isEqualTo(ErrorMessageConstant.ID_NOT_FOUND);

			verify(floorRepository, never()).createRequest(any(), any(), any(), any(), any());
		}
	}

	@Test
	@DisplayName("Method: createMoveRequest -> throws MOVE_TO_OWN_ROOM_NOT_ALLOWED when moving into own room")
	void createMoveRequest_MoveIntoOwnRoom_ShouldThrow() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			Long userId = 11L;
			Long floorId = 2L;
			Long roomId = 33L;

			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(userId));
			Mockito.doNothing().when(floorValidator).ensureNoPendingRequestForSelf(userId, RequestType.MOVE);

			Room room = sampleRoom(roomId, floorId, userId, "SELF", RoomStatus.AVAILABLE);
			when(roomService.getRoom(floorId, roomId)).thenReturn(room);

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> floorService.createMoveRequest(floorId, roomId));
			Assertions.assertThat(ex.getMessage()).isEqualTo(ErrorMessageConstant.MOVE_TO_OWN_ROOM_NOT_ALLOWED);

			verify(floorRepository, never()).createRequest(any(), any(), any(), any(), any());
		}
	}

	@Test
	@DisplayName("Method: createMoveRequest -> throws ROOM_MOVE_NOT_ALLOWED when room status is RESERVED")
	void createMoveRequest_RoomReserved_ShouldThrow() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			Long userId = 22L;
			Long floorId = 3L;
			Long roomId = 44L;

			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(userId));
			Mockito.doNothing().when(floorValidator).ensureNoPendingRequestForSelf(userId, RequestType.MOVE);

			Room room = sampleRoom(roomId, floorId, 999L, "TARGET", RoomStatus.RESERVED);
			when(roomService.getRoom(floorId, roomId)).thenReturn(room);

			Mockito.doThrow(new ResourceException(org.springframework.http.HttpStatus.BAD_REQUEST,
					ErrorMessageConstant.ROOM_MOVE_NOT_ALLOWED)).when(floorValidator).ensureRoomMovable(RoomStatus.RESERVED);

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> floorService.createMoveRequest(floorId, roomId));
			Assertions.assertThat(ex.getMessage()).isEqualTo(ErrorMessageConstant.ROOM_MOVE_NOT_ALLOWED);

			verify(floorRepository, never()).createRequest(any(), any(), any(), any(), any());
		}
	}

	@Test
	@DisplayName("Method: createMoveRequest -> throws PERSON_PENDING_REQUEST when target user already has pending MOVE")
	void createMoveRequest_TargetUserPending_ShouldThrow() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			Long userId = 33L;
			Long currentOccupant = 77L;
			Long floorId = 4L;
			Long roomId = 55L;

			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(userId));
			Mockito.doNothing().when(floorValidator).ensureNoPendingRequestForSelf(userId, RequestType.MOVE);

			Room room = sampleRoom(roomId, floorId, currentOccupant, "R-55", RoomStatus.AVAILABLE);
			when(roomService.getRoom(floorId, roomId)).thenReturn(room);
			Mockito.doNothing().when(floorValidator).ensureRoomMovable(RoomStatus.AVAILABLE);

			Mockito.doThrow(new ResourceException(org.springframework.http.HttpStatus.BAD_REQUEST,
					ErrorMessageConstant.PERSON_PENDING_REQUEST)).when(floorValidator)
					.ensureNoPendingRequestForOther(currentOccupant, RequestType.MOVE);

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> floorService.createMoveRequest(floorId, roomId));
			Assertions.assertThat(ex.getMessage()).isEqualTo(ErrorMessageConstant.PERSON_PENDING_REQUEST);

			verify(floorRepository, never()).createRequest(any(), any(), any(), any(), any());
		}
	}

	@Test
	@DisplayName("Method: createMoveRequest -> success creates MOVE request PENDING")
	void createMoveRequest_Success_ShouldPersist() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			Long userId = 44L;
			Long currentOccupant = 99L;
			Long floorId = 6L;
			Long roomId = 66L;

			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(userId));
			Mockito.doNothing().when(floorValidator).ensureNoPendingRequestForSelf(userId, RequestType.MOVE);

			Room room = sampleRoom(roomId, floorId, currentOccupant, "R-66", RoomStatus.AVAILABLE);
			when(roomService.getRoom(floorId, roomId)).thenReturn(room);

			Mockito.doNothing().when(floorValidator).ensureRoomMovable(RoomStatus.AVAILABLE);
			Mockito.doNothing().when(floorValidator).ensureNoPendingRequestForOther(currentOccupant, RequestType.MOVE);

			floorService.createMoveRequest(floorId, roomId);

			verify(floorRepository).createRequest(eq(userId), eq(floorId), eq(roomId), eq(RequestType.MOVE),
					eq(RequestStatus.PENDING));

			verify(roomService, never()).reserveRoom(any(Room.class), any());
		}
	}
}
