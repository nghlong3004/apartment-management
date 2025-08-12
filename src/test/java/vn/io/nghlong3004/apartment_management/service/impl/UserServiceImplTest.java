package vn.io.nghlong3004.apartment_management.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generateEmail;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generateFirstName;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generateLastName;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generatePassword;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generatePhoneNumber;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
import vn.io.nghlong3004.apartment_management.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Value("${jwt.refresh-token-expiration-ms}")
	private long refreshTokenExpirationMs;

	@Mock
	private UserRepository mockUserRepository;
	@Mock
	private PasswordEncoder mockPasswordEncoder;
	@Mock
	private JWTService mockJwtTokenProvider;
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
				.expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs)).build();
	}

	@BeforeEach
	private void setup() throws Exception {
		Field f = UserServiceImpl.class.getDeclaredField("refreshTokenExpirationMs");
		f.setAccessible(true);
		f.set(userServiceImpl, 7L * 24 * 60 * 60 * 1000);
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

			Assertions.assertEquals(exception.getErrorState(), ErrorState.EXISTS_EMAIL);
		}
	}

	@Test
	@DisplayName("login -> throws LOGIN_FALSE when user not found")
	void login_UserNotFound_ShouldThrow() {
		for (int i = 0; i < maxTestCase; ++i) {
			LoginRequest loginRequest = createSampleLoginRequest();
			Mockito.when(mockUserRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

			ResourceException resourceException = Assertions.assertThrows(ResourceException.class,
					() -> userServiceImpl.login(loginRequest));
			Assertions.assertEquals(ErrorState.LOGIN_FALSE, resourceException.getErrorState());
		}
	}

	@Test
	@DisplayName("login -> throws LOGIN_FALSE when password invalid")
	void login_WrongPassword_ShouldThrow() {
		for (int i = 0; i < maxTestCase; ++i) {
			LoginRequest loginRequest = createSampleLoginRequest();
			User user = User.builder().id(42L).email(loginRequest.getEmail()).password("encoded") // stored hash
					.role(Role.USER).status(UserStatus.ACTIVE).build();

			Mockito.when(mockUserRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
			Mockito.when(mockPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

			ResourceException resourceException = Assertions.assertThrows(ResourceException.class,
					() -> userServiceImpl.login(loginRequest));
			Assertions.assertEquals(ErrorState.LOGIN_FALSE, resourceException.getErrorState());
		}
	}

	@Test
	@DisplayName("login -> throws ACCOUNT_INACTIVE when user status != ACTIVE")
	void login_InactiveAccount_ShouldThrow() {
		for (int i = 0; i < maxTestCase; ++i) {
			LoginRequest loginRequest = createSampleLoginRequest();
			User user = User.builder().id(7L).email(loginRequest.getEmail()).password("encoded").role(Role.USER)
					.status(UserStatus.INACTIVE).build();

			Mockito.when(mockUserRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
			Mockito.when(mockPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);

			ResourceException resourceException = Assertions.assertThrows(ResourceException.class,
					() -> userServiceImpl.login(loginRequest));
			Assertions.assertEquals(ErrorState.ACCOUNT_INACTIVE, resourceException.getErrorState());
		}
	}

	@Test
	@DisplayName("Method: login -> success returns access & refresh tokens")
	void login_WhenValidCredentials_ShouldReturnToken() {
		for (int i = 0; i < maxTestCase; ++i) {
			User user = User.builder().id(123L).email(createSampleLoginRequest().getEmail()).password("ENC_PWD")
					.role(Role.USER).status(UserStatus.ACTIVE).build();

			String rawPwd = "raw";
			LoginRequest req = LoginRequest.builder().email(user.getEmail()).password(rawPwd).build();

			String access = UUID.randomUUID().toString();
			RefreshToken rt = RefreshToken.builder().id(1L).userId(user.getId()).token(UUID.randomUUID().toString())
					.expiryDate(Instant.now().plusSeconds(3600)).build();

			Mockito.when(mockUserRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
			Mockito.when(mockPasswordEncoder.matches(rawPwd, user.getPassword())).thenReturn(true);
			Mockito.when(mockJwtTokenProvider.generateAccessToken(user.getId(), user.getRole())).thenReturn(access);
			Mockito.when(mockRefreshTokenService.createRefreshToken(user.getId())).thenReturn(rt);

			Token token = userServiceImpl.login(req);

			Assertions.assertNotNull(token);
			Assertions.assertEquals(access, token.getAccessToken());
			Assertions.assertEquals(rt.getToken(), token.getRefreshToken());
		}
	}

	@Test
	@DisplayName("Method: getUser -> return UserDto when found")
	void getUser_WhenFound_ShouldReturnDto() {
		User user = User.builder().id(5L).email("a@b.com").firstName("A").lastName("B").phoneNumber("0909").build();
		when(mockUserRepository.findById(5L)).thenReturn(Optional.of(user));

		UserDto userDto = userServiceImpl.getUser(5L);

		Assertions.assertEquals(user.getEmail(), userDto.getEmail());
		Assertions.assertEquals(user.getFirstName(), userDto.getFirstName());
		Assertions.assertEquals(user.getLastName(), userDto.getLastName());
		Assertions.assertEquals(user.getPhoneNumber(), userDto.getPhoneNumber());
	}

	@Test
	@DisplayName("Method: getUser -> throws when not found")
	void getUser_WhenNotFound_ShouldThrow() {
		when(mockUserRepository.findById(99L)).thenReturn(Optional.empty());
		Assertions.assertThrows(ResourceException.class, () -> userServiceImpl.getUser(99L));
	}

	@Test
	@DisplayName("Method: updateUser -> admin can update others")
	void updateUser_WhenAdmin_ShouldUpdate() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			Long targetId = 10L;

			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(1L));
			util.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(true);

			User existing = User.builder().id(targetId).email("old@e.com").firstName("Old").lastName("Name")
					.phoneNumber("0909").status(UserStatus.ACTIVE).role(Role.USER).build();
			when(mockUserRepository.findById(targetId)).thenReturn(Optional.of(existing));

			UserDto dto = UserDto.builder().email("new@e.com").firstName("NewFN").lastName("NewLN").phoneNumber("0911")
					.build();

			when(mockUserRepository.existsByEmail("new@e.com")).thenReturn(Optional.of(false));

			userServiceImpl.updateUser(targetId, dto);

			verify(mockUserRepository).update(userArgumentCaptor.capture());
			User updated = userArgumentCaptor.getValue();
			Assertions.assertEquals("new@e.com", updated.getEmail());
			Assertions.assertEquals("NewFN", updated.getFirstName());
			Assertions.assertEquals("NewLN", updated.getLastName());
			Assertions.assertEquals("0911", updated.getPhoneNumber());
			Assertions.assertNotNull(updated.getUpdated());
		}
	}

	@Test
	@DisplayName("Method: updateUser -> non-admin cannot update others (FORBIDDEN)")
	void updateUser_WhenNonAdminUpdatingOthers_ShouldThrowForbidden() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(2L));
			util.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

			Assertions.assertThrows(ResourceException.class, () -> {
				userServiceImpl.updateUser(3L, UserDto.builder().firstName("X").build());
			});
		}
	}

	@Test
	@DisplayName("Method: updateUser -> throws UNWANTED_EXCEPTION when no actor id")
	void updateUser_WhenNoCurrentUser_ShouldThrowUnwantedException() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.empty());

			Assertions.assertThrows(ResourceException.class, () -> {
				userServiceImpl.updateUser(1L, UserDto.builder().build());
			});
		}
	}

	@Test
	@DisplayName("Method: updateUser -> throws NOT_FOUND when target user not found")
	void updateUser_WhenTargetNotFound_ShouldThrowNotFound() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(5L));
			util.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(true);

			when(mockUserRepository.findById(99L)).thenReturn(Optional.empty());

			Assertions.assertThrows(ResourceException.class, () -> {
				userServiceImpl.updateUser(99L, UserDto.builder().build());
			});
		}
	}

	@Test
	@DisplayName("Method: updateUser -> change email triggers existsByEmail check")
	void updateUser_WhenEmailChanged_ShouldCheckExistsByEmail() {
		try (MockedStatic<SecurityUtil> util = mockStatic(SecurityUtil.class)) {
			Long id = 7L;

			util.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(id));
			util.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

			User existing = User.builder().id(id).email("old@e.com").firstName("A").lastName("B").phoneNumber("0909")
					.status(UserStatus.ACTIVE).role(Role.USER).build();
			when(mockUserRepository.findById(id)).thenReturn(Optional.of(existing));

			UserDto dto = UserDto.builder().email("new@e.com").build();

			when(mockUserRepository.existsByEmail("new@e.com")).thenReturn(Optional.of(false));

			userServiceImpl.updateUser(id, dto);

			verify(mockUserRepository).existsByEmail("new@e.com");
			verify(mockUserRepository).update(any(User.class));
		}
	}

	@Test
	@DisplayName("Method: refresh -> Throws TokenRefreshException when refresh token does not exist")
	void refresh_WhenRefreshTokenNotFound_ShouldThrowTokenRefreshException() {
		for (int i = 0; i < maxTestCase; ++i) {
			String invalidToken = UUID.randomUUID().toString();
			Mockito.when(mockRefreshTokenService.findByToken(invalidToken)).thenReturn(Optional.empty());

			Assertions.assertThrows(ResourceException.class, () -> {
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

			Assertions.assertThrows(ResourceException.class, () -> {
				userServiceImpl.refresh(refreshToken.getToken());
			});
		}
	}

	@Test
	@DisplayName("Method: getResponseCookieRefreshToken -> sets correct cookie attributes")
	void getResponseCookieRefreshToken_ShouldReturnCookieWithConfiguredMaxAge() {
		ReflectionTestUtils.setField(userServiceImpl, "refreshTokenExpirationMs", 7200000L); // 2h
		String token = UUID.randomUUID().toString();

		ResponseCookie cookie = userServiceImpl.getResponseCookieRefreshToken(token);

		Assertions.assertEquals("refresh_token", cookie.getName());
		Assertions.assertEquals(token, cookie.getValue());
		Assertions.assertTrue(cookie.isHttpOnly());
		Assertions.assertTrue(cookie.isSecure());
		Assertions.assertEquals("/", cookie.getPath());
		Assertions.assertEquals(7200000L / 1000, cookie.getMaxAge().getSeconds());
		Assertions.assertEquals("Strict", cookie.getSameSite());
	}

}