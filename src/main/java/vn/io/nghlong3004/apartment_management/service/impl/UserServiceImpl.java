package vn.io.nghlong3004.apartment_management.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.exception.ErrorState;
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
import vn.io.nghlong3004.apartment_management.util.SecurityUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

	@Value("${jwt.refresh-token-expiration-ms}")
	private long refreshTokenExpirationMs;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JWTService jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;

	@Override
	public void register(RegisterRequest registerRequest) {
		log.info("Start the registration process for email: {}", registerRequest.getEmail());

		validateEmail(registerRequest);

		User user = User.builder().firstName(registerRequest.getFirstName()).lastName(registerRequest.getLastName())
				.email(registerRequest.getEmail()).phoneNumber(registerRequest.getPhoneNumber())
				.password(passwordEncoder.encode(registerRequest.getPassword())).role(Role.USER)
				.status(UserStatus.ACTIVE).floor(null).build();

		userRepository.save(user);
		log.info("User registration successful with email: {}.", user.getEmail());
	}

	@Override
	public Token login(LoginRequest loginRequest) {
		log.info("Start the login process for email: {}", loginRequest.getEmail());

		User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> {
			log.warn("Login failed. No user found with email: {}", loginRequest.getEmail());
			return new ResourceException(ErrorState.LOGIN_FALSE);
		});

		log.info("Found user with ID: {}", user.getId());

		validateAccount(loginRequest.getPassword(), user);

		String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
		log.debug("Successfully created access token for user ID: {}", user.getId());

		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
		log.debug("Successfully generated refresh token for user ID: {}", user.getId());

		log.info("User {} logged in successfully.", user.getEmail());
		return Token.builder().accessToken(accessToken).refreshToken(refreshToken.getToken()).build();
	}

	@Override
	public Token refresh(String requestRefreshToken) {
		log.info("Start the token refresh process.");

		RefreshToken refreshToken = getRefreshToken(requestRefreshToken);

		User user = userRepository.findById(refreshToken.getUserId()).orElseThrow(() -> {
			log.error("Data Inconsistency Error: No user with ID: {} associated with refresh token found.",
					refreshToken.getUserId());
			return new ResourceException(ErrorState.ERROR_REFRESH_TOKEN);
		});

		String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
		log.info("Refresh access token successfully for user ID: {}", user.getId());

		return Token.builder().accessToken(accessToken).refreshToken(refreshToken.getToken()).build();
	}

	@Override
	public ResponseCookie getResponseCookieRefreshToken(String refreshToken) {
		log.debug("Create ResponseCookie for refresh token.");
		ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken).httpOnly(true).secure(true)
				.path("/").maxAge(refreshTokenExpirationMs / 1000).sameSite("Strict").build();
		return responseCookie;
	}

	@Override
	public void updateUser(Long id, UserDto userDto) {
		Long actorId = SecurityUtil.getCurrentUserId()
				.orElseThrow(() -> new ResourceException(ErrorState.UNWANTED_EXCEPTION));

		boolean isAdmin = SecurityUtil.hasRole("ADMIN");
		if (!isAdmin && !actorId.equals(id)) {
			throw new ResourceException(ErrorState.FORBIDDEN);
		}

		log.info("UpdateUser requested: actorId={}, targetId={}", actorId, id);

		User user = userRepository.findById(id).orElseThrow(() -> new ResourceException(ErrorState.NOT_FOUND));

		if (userDto.getEmail() != null) {
			String newEmail = userDto.getEmail().trim().toLowerCase();
			if (!newEmail.equalsIgnoreCase(user.getEmail())) {

				userRepository.existsByEmail(newEmail)
						.orElseThrow(() -> new ResourceException(ErrorState.EXISTS_EMAIL));
				user.setEmail(newEmail);
				log.debug("Email change: {} -> {}", user.getEmail(), newEmail);
			}
		}
		if (userDto.getFirstName() != null)
			user.setFirstName(userDto.getFirstName());
		if (userDto.getLastName() != null)
			user.setLastName(userDto.getLastName());
		if (userDto.getEmail() != null)
			user.setEmail(userDto.getEmail());
		if (userDto.getPhoneNumber() != null)
			user.setPhoneNumber(userDto.getPhoneNumber());

		user.setUpdated(Timestamp.valueOf(LocalDateTime.now()));

		userRepository.update(user);
	}

	@Override
	public UserDto getUser(Long id) {
		log.info("Start get User by id: {}.", id);

		User user = userRepository.findById(id).orElseThrow(() -> new ResourceException(ErrorState.NOT_FOUND));

		log.info("User with corresponding id exists: {}.", id);

		return UserDto.builder().email(user.getEmail()).firstName(user.getFirstName()).lastName(user.getLastName())
				.phoneNumber(user.getPhoneNumber()).build();
	}

	private RefreshToken getRefreshToken(String requestRefreshToken) {
		log.info("Looking for refresh token in database.");
		RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken).orElseThrow(() -> {
			log.warn("Token refresh request failed: Refresh token does not exist in database.");
			throw new ResourceException(ErrorState.ERROR_REFRESH_TOKEN);
		});

		log.debug("Refresh token found. Verifying expiration time..");
		refreshTokenService.verifyExpiration(refreshToken);

		return refreshToken;
	}

	private void validateAccount(String rawPassword, User user) {
		if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
			log.warn("Authentication failed for user {}: Incorrect password.", user.getEmail());
			throw new ResourceException(ErrorState.LOGIN_FALSE);
		}
		if (user.getStatus() != UserStatus.ACTIVE) {
			log.warn("Authentication failed for user {}: Account is inactive.", user.getEmail());
			throw new ResourceException(ErrorState.ACCOUNT_INACTIVE);
		}
	}

	private void validateEmail(RegisterRequest registerRequest) {
		if (userRepository.existsByEmail(registerRequest.getEmail()).orElse(false)) {
			log.warn("Registration failed: Email {} already exists.", registerRequest.getEmail());
			throw new ResourceException(ErrorState.EXISTS_EMAIL);
		}
	}
}