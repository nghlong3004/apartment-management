package vn.io.nghlong3004.apartment_management.model;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshToken {

	private Long id;
	private Long userId;
	private String token;
	private Instant expiryDate;

}
