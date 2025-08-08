package vn.io.nghlong3004.apartment_management.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
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
public class UserServiceImpl implements UserService {

	@Value("${jwt.refresh-token-expiration-ms}")
	private String REFRESH_TOKEN_EXPIRATION_MS;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JWTTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;

	@Override
	public void register(RegisterRequest registerRequest) {

		validateEmail(registerRequest);

		User user = User.builder().firstName(registerRequest.getFirstName()).lastName(registerRequest.getLastName())
				.email(registerRequest.getEmail()).phoneNumber(registerRequest.getPhoneNumber())
				.password(passwordEncoder.encode(registerRequest.getPassword())).role(Role.USER)
				.status(UserStatus.ACTIVE).floor(null).build();

		userRepository.save(user);
	}

	@Override
	public Token login(LoginRequest loginRequest) {

		User user = userRepository.findByEmail(loginRequest.getEmail())
				.orElseThrow(() -> new ResourceException(ErrorState.LOGIN_FALSE));

		validateAccount(loginRequest.getPassword(), user.getPassword());

		String accessToken = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());

		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

		return Token.builder().accessToken(accessToken).refreshToken(refreshToken.getToken()).build();
	}

	@Override
	public Token refresh(String requestRefreshToken) {

		RefreshToken refreshToken = getRefreshToken(requestRefreshToken);

		User user = userRepository.findById(refreshToken.getUserId())
				.orElseThrow(() -> new ResourceException(ErrorState.ERROR_REFRESH_TOKEN));

		String accessToken = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());

		return Token.builder().accessToken(accessToken).refreshToken(refreshToken.getToken()).build();
	}

	@Override
	public ResponseCookie getResponseCookieRefreshToken(String refreshToken) {
		ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken).httpOnly(true).secure(true)
				.path("/").maxAge(Long.parseLong(REFRESH_TOKEN_EXPIRATION_MS) / 1000).sameSite("Strict").build();
		return responseCookie;
	}

	private RefreshToken getRefreshToken(String requestRefreshToken) {
		RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
				.orElseThrow(() -> new ResourceException(ErrorState.ERROR_REFRESH_TOKEN));

		refreshTokenService.verifyExpiration(refreshToken);

		return refreshToken;
	}

	private void validateAccount(String rawPassword, String encodedPassword) {
		if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
			throw new ResourceException(ErrorState.LOGIN_FALSE);
		}
	}

	private void validateEmail(RegisterRequest registerRequest) {
		if (userRepository.existsByEmail(registerRequest.getEmail()).orElse(false)) {
			throw new ResourceException(ErrorState.EXISTS_EMAIL);
		}
	}

}
