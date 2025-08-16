package vn.io.nghlong3004.apartment_management.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.RefreshToken;
import vn.io.nghlong3004.apartment_management.model.Role;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserStatus;
import vn.io.nghlong3004.apartment_management.model.dto.LoginRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;
import vn.io.nghlong3004.apartment_management.model.dto.Token;
import vn.io.nghlong3004.apartment_management.model.dto.UserDto;
import vn.io.nghlong3004.apartment_management.repository.UserRepository;
import vn.io.nghlong3004.apartment_management.service.JWTService;
import vn.io.nghlong3004.apartment_management.service.RefreshTokenService;
import vn.io.nghlong3004.apartment_management.service.UserService;
import vn.io.nghlong3004.apartment_management.service.validator.UserServiceValidator;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

	@Value("${jwt.refresh-token-expiration-ms}")
	private long refreshTokenExpirationMs;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JWTService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final UserServiceValidator userServiceValidator;

	@Override
	@Transactional
	public void register(RegisterRequest registerRequest) {
		log.info("Register start for email={}", registerRequest.getEmail());

		registerRequest.setEmail(normalizeEmail(registerRequest.getEmail()));

		userServiceValidator.ensureEmailNotExists(registerRequest.getEmail());

		User user = User.builder().firstName(registerRequest.getFirstName()).lastName(registerRequest.getLastName())
				.email(registerRequest.getEmail()).phoneNumber(registerRequest.getPhoneNumber())
				.password(passwordEncoder.encode(registerRequest.getPassword())).role(Role.USER)
				.status(UserStatus.ACTIVE).floor(null).build();

		userRepository.save(user);
		log.info("Register success for email={}", user.getEmail());
	}

	@Override
	@Transactional
	public Token login(LoginRequest loginRequest) {
		log.info("Login start for email={}", loginRequest.getEmail());

		User user = userRepository.findByEmail(normalizeEmail(loginRequest.getEmail())).orElseThrow(() -> {
			log.warn("Login failed: email not found {}", loginRequest.getEmail());
			return new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_CREDENTIALS);
		});

		userServiceValidator.validateCredentials(loginRequest.getPassword(), user);

		String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

		log.info("Login success userId={}", user.getId());
		return Token.builder().accessToken(accessToken).refreshToken(refreshToken.getToken()).build();
	}

	@Override
	@Transactional
	public Token refresh(String requestRefreshToken) {
		log.info("Refresh token start");

		RefreshToken refreshToken = userServiceValidator.findAndVerifyRefreshToken(requestRefreshToken);

		User user = userRepository.findById(refreshToken.getUserId()).orElseThrow(() -> {
			log.error("Refresh token points to non-existing userId={}", refreshToken.getUserId());
			return new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_REFRESH_TOKEN);
		});

		String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
		log.info("Refresh token success userId={}", user.getId());

		return Token.builder().accessToken(newAccessToken).refreshToken(refreshToken.getToken()).build();
	}

	@Override
	public ResponseCookie getResponseCookieRefreshToken(String refreshToken) {
		log.debug("Build refresh token cookie");
		return ResponseCookie.from("refresh_token", refreshToken).httpOnly(true).secure(true).path("/")
				.maxAge(refreshTokenExpirationMs / 1000).sameSite("Strict").build();
	}

	@Override
	@Transactional
	public void updateUser(Long id, UserDto userDto) {
		userServiceValidator.ensureCanUpdateUser(id);
		log.info("Update user start id={}", id);
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.ID_NOT_FOUND));

		userRepository.update(mapUserDtoToUser(id, userDto, user));
		log.info("Update user success id={}", id);
	}

	@Override
	@Transactional
	public UserDto getUser(Long id) {
		log.info("Get user start id={}", id);

		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ID_NOT_FOUND));

		log.info("Get user success id={}", id);
		return UserDto.from(user);
	}

	private String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

	private User mapUserDtoToUser(Long id, UserDto userDto, User currentUser) {
		User user = currentUser;
		if (userDto.getEmail() != null) {
			String newEmail = normalizeEmail(userDto.getEmail());
			if (!newEmail.equalsIgnoreCase(user.getEmail())) {
				if (userRepository.existsByEmail(newEmail).orElse(false)) {
					log.warn("Update user email conflict: id={}, newEmail={}", id, newEmail);
					throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.EMAIL_ALREADY_EXISTS);
				}
				String oldEmail = user.getEmail();
				user.setEmail(newEmail);
				log.info("Email change: {} -> {}", oldEmail, newEmail);
			}
		}
		if (userDto.getFirstName() != null)
			user.setFirstName(userDto.getFirstName());
		if (userDto.getLastName() != null)
			user.setLastName(userDto.getLastName());
		if (userDto.getPhoneNumber() != null)
			user.setPhoneNumber(userDto.getPhoneNumber());

		return user;
	}

}
