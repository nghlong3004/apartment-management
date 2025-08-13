package vn.io.nghlong3004.apartment_management.service;

public interface FloorService {

	public void createJoinRequest(Long id, Long roomId);

	public void createMoveRequest(Long id, Long roomId);

}
