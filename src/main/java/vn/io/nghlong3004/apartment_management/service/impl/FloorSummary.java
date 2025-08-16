package vn.io.nghlong3004.apartment_management.service.impl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FloorSummary {

	private Long id;
	private String name;
	private Long managerId;
	private Integer roomCount;

}
