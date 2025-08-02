package vn.io.nghlong3004.apartment_management.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.io.nghlong3004.apartment_management.exception.AppException;
import vn.io.nghlong3004.apartment_management.model.Role;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserStatus;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;
import vn.io.nghlong3004.apartment_management.repository.UserRepository;
import vn.io.nghlong3004.apartment_management.service.UserService;
import vn.io.nghlong3004.apartment_management.util.MessageUtil;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void register(RegisterRequest registerRequest) {

		int count = userRepository.existsByEmail(registerRequest.getEmail());
		if (count > 0) {
			throw new AppException(HttpStatus.BAD_REQUEST, MessageUtil.EXISTS_EMAIL);
		}

		User user = User.builder().firstName(registerRequest.getFirstName()).lastName(registerRequest.getLastName())
				.email(registerRequest.getEmail()).phoneNumber(registerRequest.getPhoneNumber())
				.passwordHash(passwordEncoder.encode(registerRequest.getPassword())).role(Role.USER)
				.status(UserStatus.ACTIVE).floor(null).build();

		userRepository.save(user);
	}

}
