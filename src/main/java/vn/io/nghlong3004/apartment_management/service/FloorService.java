package vn.io.nghlong3004.apartment_management.service;

import vn.io.nghlong3004.apartment_management.model.dto.FloorRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;
import vn.io.nghlong3004.apartment_management.model.dto.FloorSummary;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;

public interface FloorService {

	void createJoinRequest(Long floorId, Long roomId);

	void createMoveRequest(Long floorId, Long roomId);

	FloorResponse getFloorWithRooms(Long floorId);

	void updateFloor(Long floorId, FloorRequest floorUpdateRequest);

	void deleteFloor(Long floorId);

	void addFloor(FloorRequest floorRequest);

	PagedResponse<FloorSummary> getFloors(String name, int page, int size, String sort);

}
