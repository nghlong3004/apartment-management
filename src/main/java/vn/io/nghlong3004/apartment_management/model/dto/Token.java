package vn.io.nghlong3004.apartment_management.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Token {

	private String accessToken;
	private String refreshToken;

}
