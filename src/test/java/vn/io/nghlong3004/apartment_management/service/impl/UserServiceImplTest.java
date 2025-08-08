package vn.io.nghlong3004.apartment_management.service.impl;

import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generateEmail;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generateFirstName;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generateLastName;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generatePassword;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generatePhoneNumber;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.io.nghlong3004.apartment_management.constants.MessageConstants;
import vn.io.nghlong3004.apartment_management.exception.AccountResourcesException;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.exception.TokenRefreshException;
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

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Value("${jwt.refresh-token-expiration-ms}")
	private String REFRESH_TOKEN_EXPIRATION_MS;

	@Mock
	private UserRepository mockUserRepository;
	@Mock
	private PasswordEncoder mockPasswordEncoder;
	@Mock
	private JWTTokenProvider mockJwtTokenProvider;
	@Mock
	private RefreshTokenService mockRefreshTokenService;

	@InjectMocks
	private UserServiceImpl userServiceImpl;

	@Captor
	private ArgumentCaptor<User> userArgumentCaptor;

	private final int maxTestCase = 15;

	private RegisterRequest createSampleRegisterRequest() {

		return RegisterRequest.builder().firstName(generateFirstName()).lastName(generateLastName())
				.email(generateEmail()).password(generatePassword()).phoneNumber(generatePhoneNumber()).build();
	}

	private LoginRequest createSampleLoginRequest() {
		return LoginRequest.builder().email(generateEmail()).password(generatePassword()).build();
	}

	private User createSampleUser() {
		return User.builder().email(generateEmail()).password(generatePassword()).role(Role.USER)
				.status(UserStatus.ACTIVE).build();
	}

	private RefreshToken createSampleRefreshToken(User user) {
		return RefreshToken.builder().id(1L).userId(user.getId()).token(UUID.randomUUID().toString())
				.expiryDate(Instant.now().plusMillis(Long.parseLong(REFRESH_TOKEN_EXPIRATION_MS))).build();
	}

	@Test
	@DisplayName("Method: Register -> Save user successfully when email does not exist")
	void register_WhenEmailDoesNotExist_ShouldSaveUserSuccessfully() {
		RegisterRequest registerRequest = createSampleRegisterRequest();
		String password = UUID.randomUUID().toString();
		Mockito.when(mockUserRepository.existsByEmail(registerRequest.getEmail())).thenReturn(Optional.of(false));
		Mockito.when(mockPasswordEncoder.encode(registerRequest.getPassword())).thenReturn(password);

		userServiceImpl.register(registerRequest);

		Mockito.verify(mockUserRepository).save(userArgumentCaptor.capture());

		User savedUser = userArgumentCaptor.getValue();

		Assertions.assertNotNull(savedUser);
		Assertions.assertEquals(registerRequest.getFirstName(), savedUser.getFirstName());
		Assertions.assertEquals(registerRequest.getLastName(), savedUser.getLastName());
		Assertions.assertEquals(registerRequest.getEmail(), savedUser.getEmail());
		Assertions.assertEquals(password, savedUser.getPassword());
		Assertions.assertEquals(Role.USER, savedUser.getRole());
		Assertions.assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
	}

	@Test
	@DisplayName("Method: register -> Throw AppException when email already exists")
	void register_WhenEmailAlreadyExists_ShouldThrowAppException() {
		for (int i = 0; i < maxTestCase; ++i) {
			RegisterRequest request = createSampleRegisterRequest();

			Mockito.when(mockUserRepository.existsByEmail(request.getEmail())).thenReturn(Optional.of(true));

			ResourceException exception = Assertions.assertThrows(ResourceException.class, () -> {
				userServiceImpl.register(request);
			});

			Assertions.assertEquals(exception.getHttpStatus(), HttpStatus.BAD_REQUEST);
			Assertions.assertEquals(exception.getMessage(), MessageConstants.EXISTS_EMAIL);
		}
	}

	@Test
	@DisplayName("Method: login -> Throw AccountResourcesException when user does not exist")
	void login_WhenUserNotFound_ShouldThrowAccountResourcesException() {
		for (int i = 0; i < maxTestCase; ++i) {
			LoginRequest loginRequest = createSampleLoginRequest();
			Mockito.when(mockUserRepository.findPasswordByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

			Assertions.assertThrows(AccountResourcesException.class, () -> {
				userServiceImpl.login(loginRequest);
			});
		}
	}

	@Test
	@DisplayName("Method: login -> Throws AccountResourcesException when password is incorrect")
	void login_WhenPasswordIsIncorrect_ShouldThrowAccountResourcesException() {
		for (int i = 0; i < maxTestCase; ++i) {
			LoginRequest loginRequest = createSampleLoginRequest();
			User user = createSampleUser();
			Mockito.when(mockUserRepository.findPasswordByEmail(loginRequest.getEmail()))
					.thenReturn(Optional.of(user.getPassword()));
			Mockito.when(mockPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

			Assertions.assertThrows(AccountResourcesException.class, () -> {
				userServiceImpl.login(loginRequest);
			});
		}
	}

	@Test
	@DisplayName("Method: refresh -> Token refresh successful when refresh token is valid")
	void refresh_WhenRefreshTokenIsValid_ShouldReturnNewToken() {
		for (int i = 0; i < maxTestCase; ++i) {
			User user = createSampleUser();
			RefreshToken refreshToken = createSampleRefreshToken(user);
			String newAccessToken = UUID.randomUUID().toString();

			Mockito.when(mockRefreshTokenService.findByToken(refreshToken.getToken()))
					.thenReturn(Optional.of(refreshToken));
			Mockito.doNothing().when(mockRefreshTokenService).verifyExpiration(refreshToken);
			Mockito.when(mockUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
			Mockito.when(mockJwtTokenProvider.generateToken(user.getEmail(), user.getRole().name()))
					.thenReturn(newAccessToken);
			Token resultToken = userServiceImpl.refresh(refreshToken.getToken());

			Assertions.assertNotNull(resultToken);
			Assertions.assertEquals(newAccessToken, resultToken.getAccessToken());
			Assertions.assertEquals(refreshToken.getToken(), resultToken.getRefreshToken());
			Mockito.verify(mockRefreshTokenService).verifyExpiration(refreshToken);
		}
	}

	@Test
	@DisplayName("Method: refresh -> Throws TokenRefreshException when refresh token does not exist")
	void refresh_WhenRefreshTokenNotFound_ShouldThrowTokenRefreshException() {
		for (int i = 0; i < maxTestCase; ++i) {
			String invalidToken = UUID.randomUUID().toString();
			Mockito.when(mockRefreshTokenService.findByToken(invalidToken)).thenReturn(Optional.empty());

			Assertions.assertThrows(TokenRefreshException.class, () -> {
				userServiceImpl.refresh(invalidToken);
			});
		}
	}

	@Test
	@DisplayName("Method: refresh -> Throws AccountResourcesException when the user's token does not exist")
	void refresh_WhenUserOfTokenNotFound_ShouldThrowAccountResourcesException() {
		for (int i = 0; i < maxTestCase; ++i) {
			User user = createSampleUser();
			RefreshToken refreshToken = createSampleRefreshToken(user);

			Mockito.when(mockRefreshTokenService.findByToken(refreshToken.getToken()))
					.thenReturn(Optional.of(refreshToken));
			Mockito.doNothing().when(mockRefreshTokenService).verifyExpiration(refreshToken);
			Mockito.when(mockUserRepository.findById(user.getId())).thenReturn(Optional.empty());

			Assertions.assertThrows(AccountResourcesException.class, () -> {
				userServiceImpl.refresh(refreshToken.getToken());
			});
		}
	}

	@Test
	@DisplayName("Method: getResponseCookieRefreshToken -> ResponseCookie")
	void getResponseCookieRefreshToken_WhenCalled_ShouldReturnCorrectCookie() {
		for (int i = 0; i < maxTestCase; ++i) {
			String refreshTokenValue = UUID.randomUUID().toString();
			long maxAgeInSeconds = Long.parseLong(REFRESH_TOKEN_EXPIRATION_MS) / 1000;

			ResponseCookie cookie = userServiceImpl.getResponseCookieRefreshToken(refreshTokenValue);

			Assertions.assertNotNull(cookie);
			Assertions.assertEquals("refresh_token", cookie.getName());
			Assertions.assertEquals(refreshTokenValue, cookie.getValue());
			Assertions.assertTrue(cookie.isHttpOnly());
			Assertions.assertTrue(cookie.isSecure());
			Assertions.assertEquals("/", cookie.getPath());
			Assertions.assertEquals(maxAgeInSeconds, cookie.getMaxAge().getSeconds());
			Assertions.assertEquals("Strict", cookie.getSameSite());
		}
	}

}