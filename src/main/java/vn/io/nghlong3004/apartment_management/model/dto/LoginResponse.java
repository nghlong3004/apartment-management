package vn.io.nghlong3004.apartment_management.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class LoginResponse {

	@NonNull
	private String accessToken;

}
