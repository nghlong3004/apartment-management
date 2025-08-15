package vn.io.nghlong3004.apartment_management.service;

import vn.io.nghlong3004.apartment_management.model.dto.FloorCreateRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;
import vn.io.nghlong3004.apartment_management.model.dto.FloorUpdateRequest;

public interface FloorService {

	void createJoinRequest(Long floorId, Long roomId);

	void createMoveRequest(Long floorId, Long roomId);

	FloorResponse getFloorWithRooms(Long floorId);

	void updateFloor(Long floorId, FloorUpdateRequest floorUpdateRequest);

	void addFloor(FloorCreateRequest floorCreateRequest);

	void deleteFloor(Long floorId);

}
