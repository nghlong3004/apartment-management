package vn.io.nghlong3004.apartment_management.model.dto;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Data;
import vn.io.nghlong3004.apartment_management.model.FloorRequest;
import vn.io.nghlong3004.apartment_management.model.RequestStatus;

@Data
@Builder
public class FloorRequestSummary {
	private Long id;
	private Long requesterId;
	private Long requesterRoomId;
	private Long counterpartId;
	private Long counterpartRoomId;
	private Long approverId;
	private RequestStatus status;
	private String closedReason;
	private Timestamp created;

	public static FloorRequestSummary from(FloorRequest floorRequest) {
		FloorRequestSummary floorRequestSummary = FloorRequestSummary.builder().id(floorRequest.getId())
				.requesterId(floorRequest.getRequesterId()).requesterRoomId(floorRequest.getRequesterRoomId())
				.counterpartId(floorRequest.getCounterpartId()).counterpartRoomId(floorRequest.getCounterpartRoomId())
				.approverId(floorRequest.getApproverId()).status(floorRequest.getStatus())
				.closedReason(floorRequest.getClosedReason()).created(floorRequest.getCreated()).build();

		return floorRequestSummary;
	}

}
