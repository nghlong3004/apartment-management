package vn.io.nghlong3004.apartment_management.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.io.nghlong3004.apartment_management.exception.AppException;
import vn.io.nghlong3004.apartment_management.model.Role;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserStatus;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;
import vn.io.nghlong3004.apartment_management.repository.UserRepository;
import vn.io.nghlong3004.apartment_management.service.UserService;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void register(RegisterRequest registerRequest) {

		String email = userRepository.existsByEmail(registerRequest.getEmail());
		if (email != null) {
			throw new AppException(HttpStatus.BAD_REQUEST, "Email may already be in use.");
		}

		User user = new User();
		user.setFirstName(registerRequest.getFirstName());
		user.setLastName(registerRequest.getLastName());
		user.setEmail(registerRequest.getEmail());
		user.setPhoneNumber(registerRequest.getPhoneNumber());
		user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
		user.setRole(Role.USER);
		user.setStatus(UserStatus.ACTIVE);
		user.setFloor(null);
		userRepository.save(user);
	}

}
