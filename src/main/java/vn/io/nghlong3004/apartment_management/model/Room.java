package vn.io.nghlong3004.apartment_management.model;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Room {

	private Long id;
	private Long floorId;
	private Long userId;
	private String name;
	private RoomStatus status;
	private Timestamp created;
	private Timestamp updated;

}
