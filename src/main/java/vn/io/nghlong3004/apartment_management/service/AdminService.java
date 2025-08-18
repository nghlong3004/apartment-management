package vn.io.nghlong3004.apartment_management.service;

import vn.io.nghlong3004.apartment_management.model.dto.FloorManagerRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomOwnerRequest;

public interface AdminService {

	void setManager(Long floorId, FloorManagerRequest request);

	void deleteManager(Long floorId, FloorManagerRequest request);

	void setOwner(Long roomId, RoomOwnerRequest request);

	void deleteOwner(Long roomId, RoomOwnerRequest request);

}
