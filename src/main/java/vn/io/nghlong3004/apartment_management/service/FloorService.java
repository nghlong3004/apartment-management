package vn.io.nghlong3004.apartment_management.service;

import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;
import vn.io.nghlong3004.apartment_management.model.dto.FloorSummary;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;

public interface FloorService {

	FloorResponse getFloorWithRooms(Long floorId);

	void deleteFloor(Long floorId);

	void createFloor();

	PagedResponse<FloorSummary> getFloors(String name, int page, int size, String sort);

}
