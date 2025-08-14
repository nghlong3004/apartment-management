package vn.io.nghlong3004.apartment_management.model;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Floor {
	private Long id;
	private Long managerId;
	private String name;
	private Integer roomCount;
	private Timestamp created;
	private Timestamp updated;
}
