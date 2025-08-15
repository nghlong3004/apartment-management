package vn.io.nghlong3004.apartment_management.model.dto;

import java.util.List;

public record FloorResponse(Long id, String managerId, String name, Integer roomCount, List<RoomResponse> room) {

}
