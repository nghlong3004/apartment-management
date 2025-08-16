package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "First name is required.") @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters.") String firstName,
		@NotBlank(message = "Last name is required.") @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters.") String lastName,
		@NotBlank(message = "Email is required.") @Email(message = "Email format is invalid.") String email,
		@NotBlank(message = "Password is required.") @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.") String password,
		@Size(min = 10, max = 10, message = "Phone number only 10 characters (Only support Vietnamese phone numbers)") String phoneNumber) {

}
