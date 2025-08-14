package vn.io.nghlong3004.apartment_management.service;

public interface FloorService {

	void createJoinRequest(Long id, Long roomId);

	void createMoveRequest(Long id, Long roomId);

}
