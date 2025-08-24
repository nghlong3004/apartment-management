package vn.io.nghlong3004.apartment_management.service;

import jakarta.validation.constraints.Min;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.RoomRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomResponse;

public interface RoomService {

	void createRoom(Long floorId);

	RoomResponse getRoomResponse(Long floorId, Long roomId);

	void updateRoom(Long floorId, Long roomId, RoomRequest req);

	void deleteRoom(Long floorId, Long roomId);

	PagedResponse<RoomResponse> getRooms(@Min(1) Long floorId, String name, int page, int size, String sort);

	Room getRoom(Long floorId, Long roomId);

}
