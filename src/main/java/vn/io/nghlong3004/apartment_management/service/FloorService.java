package vn.io.nghlong3004.apartment_management.service;

import vn.io.nghlong3004.apartment_management.model.dto.FloorRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;

public interface FloorService {

	void createJoinRequest(Long floorId, Long roomId);

	void createMoveRequest(Long floorId, Long roomId);

	FloorResponse getFloorWithRooms(Long floorId);

	void updateFloor(Long floorId, FloorRequest floorUpdateRequest);

	void deleteFloor(Long floorId);

	void addFloor(FloorRequest floorRequest);

	FloorResponse getFloorByName(String name);

}
