package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

	@NotBlank(message = "First name is required.")
	@Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters.")
	private String firstName;

	@NotBlank(message = "Last name is required.")
	@Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters.")
	private String lastName;

	@NotBlank(message = "Email is required.")
	@Email(message = "Email format is invalid.")
	private String email;

	@NotBlank(message = "Password is required.")
	@Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
	private String password;

	@Size(min = 10, max = 10, message = "Phone number only 10 characters (Only support Vietnamese phone numbers)")
	private String phoneNumber;
}
