package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import vn.io.nghlong3004.apartment_management.model.User;

@Getter
@Builder
public class UserDto {
	@NotBlank(message = "Email is required.")
	@Email(message = "Email format is invalid.")
	private String email;
	@NotBlank(message = "First name is required.")
	@Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters.")
	private String firstName;
	@NotBlank(message = "Last name is required.")
	@Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters.")
	private String lastName;
	@Size(min = 10, max = 10, message = "Phone number only 10 characters (Only support Vietnamese phone numbers)")
	private String phoneNumber;

	public static UserDto from(User user) {
		return UserDto.builder().email(user.getEmail()).firstName(user.getFirstName()).lastName(user.getLastName())
				.phoneNumber(user.getPhoneNumber()).build();
	}
}
