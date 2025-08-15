package vn.io.nghlong3004.apartment_management.model.dto;

import vn.io.nghlong3004.apartment_management.model.RoomStatus;

public record RoomResponse(Long id, Long floorId, String userId, String name, RoomStatus status) {

}
