package vn.io.nghlong3004.apartment_management.model;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FloorRequest {
	private Long id;
	private Long userId;
	private Long approverId;
	private Floor floor;
	private Room room;
	private RequestType type;
	private RequestStatus status;
	private Timestamp created;
	private Timestamp updated;
}
