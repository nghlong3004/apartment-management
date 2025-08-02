package vn.io.nghlong3004.apartment_management.model;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User {
	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private String passwordHash;
	private String phoneNumber;
	private Role role;
	private UserStatus status;
	private Floor floor;
	private LocalDateTime created;
	private LocalDateTime updated;
}
