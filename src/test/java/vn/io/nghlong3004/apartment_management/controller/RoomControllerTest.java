package vn.io.nghlong3004.apartment_management.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.RoomRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomResponse;
import vn.io.nghlong3004.apartment_management.service.RoomService;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

	@Mock
	private RoomService roomService;

	@InjectMocks
	private RoomController controller;

	@Captor
	private ArgumentCaptor<Long> longCaptor1;
	@Captor
	private ArgumentCaptor<Long> longCaptor2;
	@Captor
	private ArgumentCaptor<Integer> intCaptor1;
	@Captor
	private ArgumentCaptor<Integer> intCaptor2;
	@Captor
	private ArgumentCaptor<String> stringCaptor;
	@Captor
	private ArgumentCaptor<RoomRequest> roomRequestCaptor;

	private RoomRequest sampleRequest() {
		RoomRequest r = new RoomRequest("R101", 99L, RoomStatus.RESERVED);
		return r;
	}

	private PagedResponse<RoomResponse> sampleResponse(Long id, Long floorId, String name, Long userId,
			RoomStatus status) {
		RoomResponse dto = new RoomResponse(id, floorId, userId, name, status);

		PagedResponse<RoomResponse> resp = PagedResponse.<RoomResponse>builder().content(List.of(dto)).page(0).size(1)
				.totalElements(1L).totalPages(1).build();
		return resp;
	}

	private PagedResponse<RoomResponse> samplePage() {
		return PagedResponse.<RoomResponse>builder()
				.content(List.of(new RoomResponse(1L, 10L, 2L, "R1", RoomStatus.AVAILABLE),
						new RoomResponse(2L, 10L, 3L, "R2", RoomStatus.SOLD)))
				.page(0).size(2).totalElements(5).totalPages(3).build();
	}

	@Test
	@DisplayName("POST /{floorId}/room -> delegates to service.createRoom with same args")
	void createRoom_delegates() {
		Long floorId = 10L;
		RoomRequest req = sampleRequest();

		controller.createRoom(floorId, req);

		verify(roomService).createRoom(longCaptor1.capture(), roomRequestCaptor.capture());
		assertThat(longCaptor1.getValue()).isEqualTo(floorId);
		assertThat(roomRequestCaptor.getValue()).isEqualTo(req);
	}

	@Test
	@DisplayName("GET /{floorId}/room (no name) -> returns paged list and delegates with paging/sort")
	void listRooms_paged() {
		Long floorId = 11L;
		int page = 2;
		int size = 50;
		String sort = "name,desc";
		PagedResponse<RoomResponse> expected = samplePage();

		when(roomService.getRooms(floorId, null, page, size, sort)).thenReturn(expected);

		PagedResponse<RoomResponse> got = controller.rooms(floorId, null, page, size, sort);

		verify(roomService).getRooms(longCaptor1.capture(), null, intCaptor1.capture(), intCaptor2.capture(),
				stringCaptor.capture());
		assertThat(longCaptor1.getValue()).isEqualTo(floorId);
		assertThat(intCaptor1.getValue()).isEqualTo(page);
		assertThat(intCaptor2.getValue()).isEqualTo(size);
		assertThat(stringCaptor.getValue()).isEqualTo(sort);

		assertThat(got).isEqualTo(expected);
		assertThat(got.getContent()).hasSize(2);
		assertThat(got.getPage()).isEqualTo(0);
		assertThat(got.getTotalElements()).isEqualTo(5);
	}

	@Test
	@DisplayName("GET /{floorId}/room/{roomId} -> returns RoomResponse and delegates correctly")
	void getRoom_byId() {
		Long floorId = 12L;
		Long roomId = 1201L;
		PagedResponse<RoomResponse> listExpected = sampleResponse(roomId, floorId, "A101", 7L, RoomStatus.AVAILABLE);
		RoomResponse expected = listExpected.getContent().get(0);
		when(roomService.getRoomResponse(floorId, roomId)).thenReturn(expected);

		RoomResponse got = controller.getRoom(floorId, roomId);

		verify(roomService).getRoomResponse(longCaptor1.capture(), longCaptor2.capture());
		assertThat(longCaptor1.getValue()).isEqualTo(floorId);
		assertThat(longCaptor2.getValue()).isEqualTo(roomId);

		assertThat(got).isEqualTo(expected);
	}

	@Test
	@DisplayName("GET /{floorId}/room?name=... -> returns RoomResponse by name")
	void getRoom_byName() {
		Long floorId = 13L;
		String roomName = "B202";

		PagedResponse<RoomResponse> expected = sampleResponse(222L, floorId, roomName, 8L, RoomStatus.RESERVED);

		when(roomService.getRooms(floorId, roomName, 0, 0, null)).thenReturn(expected);

		PagedResponse<RoomResponse> got = controller.rooms(floorId, roomName, 0, 0, null);

		verify(roomService).getRooms(longCaptor1.capture(), stringCaptor.capture(), 0, 0, null);
		assertThat(longCaptor1.getValue()).isEqualTo(floorId);
		assertThat(stringCaptor.getValue()).isEqualTo(roomName);

		assertThat(got).isEqualTo(expected);
	}

	@Test
	@DisplayName("PUT /{floorId}/room/{roomId} -> delegates to service.updateRoom with request body")
	void updateRoom_delegates() {
		Long floorId = 14L;
		Long roomId = 1401L;
		RoomRequest req = sampleRequest();

		controller.updateRoom(floorId, roomId, req);

		verify(roomService).updateRoom(longCaptor1.capture(), longCaptor2.capture(), roomRequestCaptor.capture());
		assertThat(longCaptor1.getValue()).isEqualTo(floorId);
		assertThat(longCaptor2.getValue()).isEqualTo(roomId);
		assertThat(roomRequestCaptor.getValue()).isEqualTo(req);
	}

	@Test
	@DisplayName("DELETE /{floorId}/room/{roomId} -> delegates to service.deleteRoom")
	void deleteRoom_delegates() {
		Long floorId = 15L;
		Long roomId = 1501L;

		controller.deleteRoom(floorId, roomId);

		verify(roomService).deleteRoom(longCaptor1.capture(), longCaptor2.capture());
		assertThat(longCaptor1.getValue()).isEqualTo(floorId);
		assertThat(longCaptor2.getValue()).isEqualTo(roomId);
	}
}
