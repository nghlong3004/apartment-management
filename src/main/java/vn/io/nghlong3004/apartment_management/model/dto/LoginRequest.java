package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {

	@NotBlank(message = "Email is required.")
	@Email(message = "Email format is invalid.")
	private String email;

	@NotBlank(message = "Password is required.")
	@Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
	private String password;

}
