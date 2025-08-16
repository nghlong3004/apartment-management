package vn.io.nghlong3004.apartment_management.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import vn.io.nghlong3004.apartment_management.model.dto.FloorRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;
import vn.io.nghlong3004.apartment_management.model.dto.JoinRoomRequest;
import vn.io.nghlong3004.apartment_management.service.FloorService;

@ExtendWith(MockitoExtension.class)
class FloorControllerTest {

	@Mock
	private FloorService floorService;

	@InjectMocks
	private FloorController floorController;

	@Captor
	private ArgumentCaptor<Long> floorIdCaptor;

	@Captor
	private ArgumentCaptor<Long> roomIdCaptor;

	@Captor
	private ArgumentCaptor<Long> longCaptor;

	private JoinRoomRequest createSampleJoinRoomRequest() {
		long roomId = Math.abs(new Random().nextLong(1_000_000)) + 1;
		return new JoinRoomRequest(roomId);
	}

	@Test
	@DisplayName("POST /api/v1/floor/{floorId} -> should delegate to FloorService.createJoinRequest")
	void joinRoom_ShouldDelegateToService() {
		long floorId = Math.abs(new Random().nextLong(1_000_000)) + 1;
		JoinRoomRequest req = createSampleJoinRoomRequest();

		floorController.joinRoom(floorId, req);

		verify(floorService).createJoinRequest(floorIdCaptor.capture(), roomIdCaptor.capture());
		Assertions.assertEquals(floorId, floorIdCaptor.getValue());
		Assertions.assertEquals(req.roomId(), roomIdCaptor.getValue());
	}

	@Test
	@DisplayName("PUT /api/v1/floor/{floorId} -> should delegate to FloorService.createMoveRequest")
	void moveRoom_ShouldDelegateToService() {
		long floorId = Math.abs(new Random().nextLong(1_000_000)) + 1;
		JoinRoomRequest req = createSampleJoinRoomRequest();

		floorController.moveRoom(floorId, req);

		verify(floorService).createMoveRequest(floorIdCaptor.capture(), roomIdCaptor.capture());
		Assertions.assertEquals(floorId, floorIdCaptor.getValue());
		Assertions.assertEquals(req.roomId(), roomIdCaptor.getValue());
	}

	@Test
	@DisplayName("POST /api/v1/floor/{floorId} -> annotated with @ResponseStatus(CREATED)")
	void joinRoom_ShouldHaveCreatedResponseStatusAnnotation() throws NoSuchMethodException {
		Method m = FloorController.class.getMethod("joinRoom", Long.class, JoinRoomRequest.class);
		ResponseStatus rs = m.getAnnotation(ResponseStatus.class);
		Assertions.assertNotNull(rs, "Missing @ResponseStatus on joinRoom");
		Assertions.assertEquals(HttpStatus.CREATED, rs.code());
	}

	@Test
	@DisplayName("PUT /api/v1/floor/{floorId} -> annotated with @ResponseStatus(CREATED)")
	void moveRoom_ShouldHaveCreatedResponseStatusAnnotation() throws NoSuchMethodException {
		Method m = FloorController.class.getMethod("moveRoom", Long.class, JoinRoomRequest.class);
		ResponseStatus rs = m.getAnnotation(ResponseStatus.class);
		Assertions.assertNotNull(rs, "Missing @ResponseStatus on moveRoom");
		Assertions.assertEquals(HttpStatus.CREATED, rs.code());
	}

	@Test
	@DisplayName("POST /{floorId} -> delegates to floorService.createJoinRequest with floorId & roomId")
	void joinRoom_delegates() {
		Long floorId = 10L;
		Long roomId = 200L;
		JoinRoomRequest req = new JoinRoomRequest(roomId);

		floorController.joinRoom(floorId, req);

		verify(floorService).createJoinRequest(longCaptor.capture(), longCaptor.capture());
		var args = longCaptor.getAllValues();
		assertThat(args.get(0)).isEqualTo(floorId);
		assertThat(args.get(1)).isEqualTo(roomId);
	}

	@Test
	@DisplayName("PUT /{floorId}/room/move -> delegates to floorService.createMoveRequest")
	void moveRoom_delegates() {
		Long floorId = 11L;
		Long roomId = 201L;
		JoinRoomRequest req = new JoinRoomRequest(roomId);

		floorController.moveRoom(floorId, req);

		verify(floorService).createMoveRequest(longCaptor.capture(), longCaptor.capture());
		var args = longCaptor.getAllValues();
		assertThat(args.get(0)).isEqualTo(floorId);
		assertThat(args.get(1)).isEqualTo(roomId);
	}

	@Test
	@DisplayName("GET /{floorId} -> returns FloorResponse from service")
	void getFloor_returnsResponse() {
		Long floorId = 12L;
		FloorResponse expected = Mockito.mock(FloorResponse.class);

		when(floorService.getFloorWithRooms(floorId)).thenReturn(expected);

		FloorResponse got = floorController.getFloor(floorId);

		verify(floorService).getFloorWithRooms(longCaptor.capture());
		assertThat(longCaptor.getValue()).isEqualTo(floorId);
		assertThat(got).isSameAs(expected);
	}

	@Test
	@DisplayName("PUT /{floorId} -> delegates to floorService.updateFloor")
	void updateFloor_delegates() {
		Long floorId = 13L;
		FloorRequest request = Mockito.mock(FloorRequest.class);

		floorController.updateFloor(floorId, request);

		verify(floorService).updateFloor(floorId, request);
	}

	@Test
	@DisplayName("POST / -> delegates to floorService.addFloor")
	void addFloor_delegates() {
		FloorRequest request = Mockito.mock(FloorRequest.class);

		floorController.addFloor(request);

		verify(floorService).addFloor(request);
	}

	@Test
	@DisplayName("DELETE /{floorId} -> delegates to floorService.deleteFloor")
	void deleteFloor_delegates() {
		Long floorId = 14L;

		floorController.deleteFloor(floorId);

		verify(floorService).deleteFloor(longCaptor.capture());
		assertThat(longCaptor.getValue()).isEqualTo(floorId);
	}
}
