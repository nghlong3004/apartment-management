package vn.io.nghlong3004.apartment_management.model;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private String phoneNumber;
	private Role role;
	private UserStatus status;
	private Floor floor;
	private Timestamp created;
	private Timestamp updated;
}
