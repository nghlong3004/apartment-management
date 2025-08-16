package vn.io.nghlong3004.apartment_management.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import vn.io.nghlong3004.apartment_management.constant.ErrorMessage;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.RoomRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomResponse;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

	@Mock
	private RoomRepository roomRepository;
	@Mock
	private FloorRepository floorRepository;

	@InjectMocks
	private RoomServiceImpl service;

	@Captor
	private ArgumentCaptor<Room> roomCaptor;

	@Captor
	private ArgumentCaptor<String> orderByCaptor;

	private Room sampleRoom(Long id, Long floorId, String name, RoomStatus status) {
		return Room.builder().id(id).floorId(floorId).name(name).status(status).build();
	}

	private RoomRequest sampleReq(String name, RoomStatus status) {
		RoomRequest req = new RoomRequest();
		req.setName(name);
		req.setStatus(status);
		return req;
	}

	@Nested
	class GetRoom {
		@Test
		@DisplayName("getRoom -> returns room when found")
		void getRoom_found() {
			Room r = sampleRoom(10L, 1L, "A101", RoomStatus.AVAILABLE);
			when(roomRepository.findRoomByFloorIdAndRoomId(1L, 10L)).thenReturn(Optional.of(r));

			Room got = service.getRoom(1L, 10L);

			assertThat(got).isEqualTo(r);
		}

		@Test
		@DisplayName("getRoom -> throws ROOM_NOT_FOUND when absent")
		void getRoom_notFound() {
			when(roomRepository.findRoomByFloorIdAndRoomId(1L, 10L)).thenReturn(Optional.empty());

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> service.getRoom(1L, 10L));
			assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(ex.getMessage()).isEqualTo(ErrorMessage.ROOM_NOT_FOUND);
		}
	}

	@Test
	@DisplayName("reserveRoom -> sets status = RESERVED and userId, then calls update")
	void reserveRoom_updates() {
		Room r = sampleRoom(20L, 2L, "B202", RoomStatus.AVAILABLE);

		service.reserveRoom(r, 99L);

		verify(roomRepository).updateRoom(roomCaptor.capture());
		Room updated = roomCaptor.getValue();
		assertThat(updated.getId()).isEqualTo(20L);
		assertThat(updated.getStatus()).isEqualTo(RoomStatus.RESERVED);
		assertThat(updated.getUserId()).isEqualTo(99L);
	}

	@Test
	@DisplayName("getRoomResponse -> maps to DTO")
	void getRoomResponse_maps() {
		Room r = sampleRoom(11L, 3L, "C301", RoomStatus.SOLD);
		when(roomRepository.findRoomByFloorIdAndRoomId(3L, 11L)).thenReturn(Optional.of(r));

		RoomResponse dto = service.getRoomResponse(3L, 11L);

		assertThat(dto.getId()).isEqualTo(11L);
		assertThat(dto.getFloorId()).isEqualTo(3L);
		assertThat(dto.getName()).isEqualTo("C301");
		assertThat(dto.getStatus()).isEqualTo(RoomStatus.SOLD);
	}

	@Test
	@DisplayName("getAllRooms -> returns empty list when repository returns null/empty")
	void getAllRooms_empty() {
		when(roomRepository.findAllRoomsByFloorId(4L)).thenReturn(null);
		assertThat(service.getAllRooms(4L)).isEmpty();

		when(roomRepository.findAllRoomsByFloorId(4L)).thenReturn(List.of());
		assertThat(service.getAllRooms(4L)).isEmpty();
	}

	@Nested
	class GetRoomsByFloor {
		@Test
		@DisplayName("throws FLOOR_NOT_FOUND if floor does not exist")
		void floorNotFound() {
			when(floorRepository.floorExists(9L)).thenReturn(Optional.empty());

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> service.getRoomsByFloor(9L, 0, 20, "id,asc"));
			assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(ex.getMessage()).isEqualTo(ErrorMessage.FLOOR_NOT_FOUND);
		}

		@Test
		@DisplayName("happy path -> builds PagedResponse and normalizes sort")
		void happyPath() {
			when(floorRepository.floorExists(5L)).thenReturn(Optional.of(true));
			when(roomRepository.countByFloorId(5L)).thenReturn(3L);

			Room r1 = sampleRoom(1L, 5L, "A1", RoomStatus.AVAILABLE);
			Room r2 = sampleRoom(2L, 5L, "A2", RoomStatus.RESERVED);
			when(roomRepository.findPageByFloorId(eq(5L), anyString(), eq(2), eq(0))).thenAnswer(inv -> {
				return List.of(r1, r2);
			});

			PagedResponse<RoomResponse> page = service.getRoomsByFloor(5L, 0, 2, "name,desc");

			verify(roomRepository).findPageByFloorId(eq(5L), orderByCaptor.capture(), eq(2), eq(0));
			assertThat(orderByCaptor.getValue()).isEqualTo("name DESC");
			assertThat(page.getTotalElements()).isEqualTo(3L);
			assertThat(page.getTotalPages()).isEqualTo((int) Math.ceil(3 / 2.0));
			assertThat(page.getContent()).hasSize(2);
			assertThat(page.getContent().get(0).getName()).isEqualTo("A1");
		}

		@Test
		@DisplayName("unrecognized sort -> defaults to 'id ASC'")
		void sortDefault() {
			when(floorRepository.floorExists(6L)).thenReturn(Optional.of(true));
			when(roomRepository.countByFloorId(6L)).thenReturn(0L);
			when(roomRepository.findPageByFloorId(eq(6L), anyString(), eq(20), eq(0))).thenReturn(List.of());

			service.getRoomsByFloor(6L, 0, 20, "hacky_column,drop");

			verify(roomRepository).findPageByFloorId(eq(6L), orderByCaptor.capture(), eq(20), eq(0));
			assertThat(orderByCaptor.getValue()).isEqualTo("id ASC");
		}
	}

	@Nested
	class CreateRoom {
		@Test
		@DisplayName("throws FLOOR_NOT_FOUND when floor missing")
		void floorMissing() {
			when(floorRepository.floorExists(1L)).thenReturn(Optional.empty());

			RoomRequest req = sampleReq("R1", null);

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> service.createRoom(1L, req));
			assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(ex.getMessage()).isEqualTo(ErrorMessage.FLOOR_NOT_FOUND);

			verifyNoInteractions(roomRepository);
		}

		@Test
		@DisplayName("throws BAD_REQUEST when room name exists in floor")
		void duplicateName() {
			when(floorRepository.floorExists(2L)).thenReturn(Optional.of(true));
			when(roomRepository.existsByFloorIdAndName(2L, "R1")).thenReturn(Optional.of(true));

			RoomRequest req = sampleReq("R1", null);

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> service.createRoom(2L, req));
			assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(ex.getMessage()).contains("Room name already exists");

			verify(roomRepository, never()).insert(any());
			verify(floorRepository, never()).incrementRoomCount(anyLong());
		}

		@Test
		@DisplayName("happy path -> insert room and increment floor count")
		void happy() {
			when(floorRepository.floorExists(3L)).thenReturn(Optional.of(true));
			when(roomRepository.existsByFloorIdAndName(3L, "R1")).thenReturn(Optional.of(false));

			RoomRequest req = sampleReq("R1", null);

			service.createRoom(3L, req);

			verify(roomRepository).insert(roomCaptor.capture());
			Room saved = roomCaptor.getValue();
			assertThat(saved.getFloorId()).isEqualTo(3L);
			assertThat(saved.getName()).isEqualTo("R1");
			assertThat(saved.getStatus()).isEqualTo(RoomStatus.AVAILABLE);

			verify(floorRepository).incrementRoomCount(3L);
		}
	}

	@Nested
	class UpdateRoom {
		@Test
		@DisplayName("throws ROOM_NOT_FOUND when target not exists")
		void notFound() {
			when(roomRepository.findRoomByFloorIdAndRoomId(4L, 44L)).thenReturn(Optional.empty());

			RoomRequest req = sampleReq("X", RoomStatus.RESERVED);

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> service.updateRoom(4L, 44L, req));
			assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(ex.getMessage()).isEqualTo(ErrorMessage.ROOM_NOT_FOUND);
		}

		@Test
		@DisplayName("throws CONFLICT when name duplicated (excluding id)")
		void duplicateName() {
			when(roomRepository.findRoomByFloorIdAndRoomId(4L, 44L))
					.thenReturn(Optional.of(sampleRoom(44L, 4L, "OLD", RoomStatus.AVAILABLE)));
			when(roomRepository.existsByFloorIdAndNameExcludingId(4L, "X", 44L)).thenReturn(Optional.of(true));

			RoomRequest req = sampleReq("X", RoomStatus.SOLD);

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> service.updateRoom(4L, 44L, req));
			assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
			assertThat(ex.getMessage()).contains("Room name already exists");
		}

		@Test
		@DisplayName("happy path -> updateRoom called with correct data")
		void happy() {
			when(roomRepository.findRoomByFloorIdAndRoomId(4L, 44L))
					.thenReturn(Optional.of(sampleRoom(44L, 4L, "OLD", RoomStatus.AVAILABLE)));
			when(roomRepository.existsByFloorIdAndNameExcludingId(4L, "NEW", 44L)).thenReturn(Optional.of(false));

			RoomRequest req = sampleReq("NEW", RoomStatus.RESERVED);

			service.updateRoom(4L, 44L, req);

			verify(roomRepository).updateRoom(roomCaptor.capture());
			Room updated = roomCaptor.getValue();
			assertThat(updated.getId()).isEqualTo(44L);
			assertThat(updated.getFloorId()).isEqualTo(4L);
			assertThat(updated.getName()).isEqualTo("NEW");
			assertThat(updated.getStatus()).isEqualTo(RoomStatus.RESERVED);
		}
	}

	@Test
	@DisplayName("deleteRoom -> calls delete and decrementRoomCount")
	void deleteRoom_callsRepo() {
		service.deleteRoom(8L, 88L);

		verify(roomRepository).deleteByIdAndFloorId(88L, 8L);
		verify(floorRepository).decrementRoomCount(8L);
	}

	@Nested
	class GetRoomByName {
		@Test
		@DisplayName("throws FLOOR_NOT_FOUND when floor missing")
		void floorMissing() {
			when(floorRepository.floorExists(1L)).thenReturn(Optional.empty());

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> service.getRoomByName(1L, "R1"));
			assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(ex.getMessage()).isEqualTo(ErrorMessage.FLOOR_NOT_FOUND);
		}

		@Test
		@DisplayName("throws ROOM_NOT_FOUND when no room with that name")
		void roomMissing() {
			when(floorRepository.floorExists(2L)).thenReturn(Optional.of(true));
			when(roomRepository.findByFloorIdAndName(2L, "R1")).thenReturn(Optional.empty());

			ResourceException ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceException.class,
					() -> service.getRoomByName(2L, "R1"));
			assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(ex.getMessage()).isEqualTo(ErrorMessage.ROOM_NOT_FOUND);
		}

		@Test
		@DisplayName("happy path -> returns RoomResponse")
		void happy() {
			when(floorRepository.floorExists(3L)).thenReturn(Optional.of(true));
			Room r = sampleRoom(100L, 3L, "R100", RoomStatus.SOLD);
			when(roomRepository.findByFloorIdAndName(3L, "R100")).thenReturn(Optional.of(r));

			RoomResponse dto = service.getRoomByName(3L, "R100");

			assertThat(dto.getId()).isEqualTo(100L);
			assertThat(dto.getFloorId()).isEqualTo(3L);
			assertThat(dto.getName()).isEqualTo("R100");
			assertThat(dto.getStatus()).isEqualTo(RoomStatus.SOLD);
		}
	}
}
