package vn.io.nghlong3004.apartment_management.model;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RefreshToken {

	private Long id;
	private Long userId;
	private String token;
	private Instant expiryDate;

}
