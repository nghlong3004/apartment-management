package vn.io.nghlong3004.apartment_management.controller;

import static org.mockito.Mockito.verify;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

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
}
