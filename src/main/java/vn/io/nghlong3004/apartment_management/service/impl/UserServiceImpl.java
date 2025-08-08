package vn.io.nghlong3004.apartment_management.service.impl;

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
import vn.io.nghlong3004.apartment_management.repository.UserRepository;
import vn.io.nghlong3004.apartment_management.security.JWTTokenProvider;
import vn.io.nghlong3004.apartment_management.service.RefreshTokenService;
import vn.io.nghlong3004.apartment_management.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

	@Value("${jwt.refresh-token-expiration-ms}")
	private String REFRESH_TOKEN_EXPIRATION_MS;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JWTTokenProvider jwtTokenProvider;
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
		log.info("User registration successful with email: {}. \n New User ID: {}", user.getEmail(), user.getId());
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

		String accessToken = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
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

		String accessToken = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
		log.info("Refresh access token successfully for user ID: {}", user.getId());

		return Token.builder().accessToken(accessToken).refreshToken(refreshToken.getToken()).build();
	}

	@Override
	public ResponseCookie getResponseCookieRefreshToken(String refreshToken) {
		log.debug("Create ResponseCookie for refresh token.");
		ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken).httpOnly(true).secure(true)
				.path("/").maxAge(Long.parseLong(REFRESH_TOKEN_EXPIRATION_MS) / 1000).sameSite("Strict").build();
		return responseCookie;
	}

	private RefreshToken getRefreshToken(String requestRefreshToken) {
		log.debug("Looking for refresh token in database.");
		RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken).orElseThrow(() -> {
			log.warn("Token refresh request failed: Refresh token does not exist in database.");
			return new ResourceException(ErrorState.ERROR_REFRESH_TOKEN);
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
			log.warn("Đăng ký thất bại: Email {} đã tồn tại.", registerRequest.getEmail());
			throw new ResourceException(ErrorState.EXISTS_EMAIL);
		}
	}
}