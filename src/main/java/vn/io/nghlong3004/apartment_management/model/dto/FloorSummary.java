package vn.io.nghlong3004.apartment_management.model.dto;

import lombok.Builder;
import lombok.Data;
import vn.io.nghlong3004.apartment_management.model.Floor;

@Data
@Builder
public class FloorSummary {

	private Long id;
	private String name;
	private Long managerId;
	private Integer roomCount;

	public static FloorSummary from(Floor floor) {
		return FloorSummary.builder().id(floor.getId()).name(floor.getName()).managerId(floor.getManagerId())
				.roomCount(floor.getRoomCount()).build();
	}

}
