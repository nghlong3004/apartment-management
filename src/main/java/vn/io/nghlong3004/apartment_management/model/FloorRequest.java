package vn.io.nghlong3004.apartment_management.model;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Data;
import vn.io.nghlong3004.apartment_management.model.dto.CreateRequest;

@Data
@Builder
public class FloorRequest {
	private Long id;
	private Long requesterId;
	private Long counterpartId;
	private Long approverId;

	private Long requesterRoomId;
	private Long counterpartRoomId;

	private RequestStatus status;

	private String closedReason;

	private Timestamp created;
	private Timestamp updated;

	public static FloorRequest of(CreateRequest request) {
		FloorRequest floorRequest = FloorRequest.builder().requesterId(request.requesterId())
				.counterpartId(request.counterpartId()).requesterRoomId(request.requesterRoomId())
				.counterpartRoomId(request.counterpartRoomId()).status(RequestStatus.PENDING).build();

		return floorRequest;
	}
}
