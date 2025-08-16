package vn.io.nghlong3004.apartment_management.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
import vn.io.nghlong3004.apartment_management.service.validator.UserServiceValidator;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository mockUserRepository;
	@Mock
	private PasswordEncoder mockPasswordEncoder;
	@Mock
	private JWTService mockJwtService;
	@Mock
	private RefreshTokenService mockRefreshTokenService;
	@Mock
	private UserServiceValidator mockUserServiceValidator;

	@InjectMocks
	private UserServiceImpl userServiceImpl;

	@Captor
	private ArgumentCaptor<User> userArgumentCaptor;

	private final int maxTestCase = 15;

	private RegisterRequest createSampleRegisterRequest() {
		return new RegisterRequest(generateFirstName(), generateLastName(), generateEmail(), generatePassword(),
				generatePhoneNumber());
	}

	private LoginRequest createSampleLoginRequest() {
		return new LoginRequest(generateEmail(), generatePassword());

	}

	private RefreshToken createSampleRefreshToken(Long userId) {
		return RefreshToken.builder().id(1L).userId(userId).token(UUID.randomUUID().toString())
				.expiryDate(Instant.now().plusSeconds(3600)).build();
	}

	@BeforeEach
	private void setup() throws Exception {
		Field f = UserServiceImpl.class.getDeclaredField("refreshTokenExpirationMs");
		f.setAccessible(true);
		f.set(userServiceImpl, 7L * 24 * 60 * 60 * 1000);
	}

	@Test
	@DisplayName("Method: register -> should save user when email does not exist (via validator)")
	void register_WhenEmailDoesNotExist_ShouldSaveUserSuccessfully() {
		RegisterRequest registerRequest = createSampleRegisterRequest();
		Mockito.doNothing().when(mockUserServiceValidator).ensureEmailNotExists(anyString());

		String encoded = UUID.randomUUID().toString();
		when(mockPasswordEncoder.encode(registerRequest.password())).thenReturn(encoded);

		userServiceImpl.register(registerRequest);

		verify(mockUserRepository).save(userArgumentCaptor.capture());
		User saved = userArgumentCaptor.getValue();

		Assertions.assertNotNull(saved);
		Assertions.assertEquals(registerRequest.firstName(), saved.getFirstName());
		Assertions.assertEquals(registerRequest.lastName(), saved.getLastName());
		Assertions.assertEquals(registerRequest.phoneNumber(), saved.getPhoneNumber());

		String expectedEmail = registerRequest.email() == null ? null : registerRequest.email().trim().toLowerCase();
		Assertions.assertEquals(expectedEmail, saved.getEmail());

		Assertions.assertEquals(encoded, saved.getPassword());
		Assertions.assertEquals(Role.USER, saved.getRole());
		Assertions.assertEquals(UserStatus.ACTIVE, saved.getStatus());
	}

	@Test
	@DisplayName("Method: register -> should throw EMAIL_ALREADY_EXISTS when email exists (validator throws)")
	void register_WhenEmailAlreadyExists_ShouldThrow() {
		for (int i = 0; i < maxTestCase; i++) {
			RegisterRequest req = createSampleRegisterRequest();
			Mockito.doThrow(new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.EMAIL_ALREADY_EXISTS))
					.when(mockUserServiceValidator).ensureEmailNotExists(anyString());

			ResourceException ex = Assertions.assertThrows(ResourceException.class,
					() -> userServiceImpl.register(req));
			Assertions.assertEquals(ErrorMessageConstant.EMAIL_ALREADY_EXISTS, ex.getMessage());
		}
	}

	@Test
	@DisplayName("Method: login -> should throw INVALID_CREDENTIALS when user not found")
	void login_UserNotFound_ShouldThrow() {
		for (int i = 0; i < maxTestCase; i++) {
			LoginRequest loginRequest = createSampleLoginRequest();
			when(mockUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());

			ResourceException ex = Assertions.assertThrows(ResourceException.class,
					() -> userServiceImpl.login(loginRequest));
			Assertions.assertEquals(ErrorMessageConstant.INVALID_CREDENTIALS, ex.getMessage());
		}
	}

	@Test
	@DisplayName("Method: login -> should throw INVALID_CREDENTIALS when password is wrong (validator throws)")
	void login_WrongPassword_ShouldThrow() {
		for (int i = 0; i < maxTestCase; i++) {
			LoginRequest loginRequest = createSampleLoginRequest();
			User user = User.builder().id(42L).email(loginRequest.email().trim().toLowerCase()).password("encoded")
					.role(Role.USER).status(UserStatus.ACTIVE).build();

			when(mockUserRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
			Mockito.doThrow(new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_CREDENTIALS))
					.when(mockUserServiceValidator).validateCredentials(anyString(), Mockito.eq(user));

			ResourceException ex = Assertions.assertThrows(ResourceException.class,
					() -> userServiceImpl.login(loginRequest));
			Assertions.assertEquals(ErrorMessageConstant.INVALID_CREDENTIALS, ex.getMessage());

			Mockito.verifyNoInteractions(mockJwtService, mockRefreshTokenService);
		}
	}

	@Test
	@DisplayName("Method: login -> should throw ACCOUNT_INACTIVE when user is not ACTIVE (validator throws)")
	void login_InactiveAccount_ShouldThrow() {
		for (int i = 0; i < maxTestCase; i++) {
			LoginRequest loginRequest = createSampleLoginRequest();
			User user = User.builder().id(7L).email(loginRequest.email().trim().toLowerCase()).password("encoded")
					.role(Role.USER).status(UserStatus.INACTIVE).build();

			when(mockUserRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
			Mockito.doThrow(new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ACCOUNT_INACTIVE))
					.when(mockUserServiceValidator).validateCredentials(anyString(), Mockito.eq(user));

			ResourceException ex = Assertions.assertThrows(ResourceException.class,
					() -> userServiceImpl.login(loginRequest));
			Assertions.assertEquals(ErrorMessageConstant.ACCOUNT_INACTIVE, ex.getMessage());

			Mockito.verifyNoInteractions(mockJwtService, mockRefreshTokenService);
		}
	}

	@Test
	@DisplayName("Method: login -> should return access & refresh tokens when credentials are valid")
	void login_WhenValidCredentials_ShouldReturnToken() {
		for (int i = 0; i < maxTestCase; i++) {
			String email = createSampleLoginRequest().email();
			User user = User.builder().id(123L).email(email.trim().toLowerCase()).password("ENC_PWD").role(Role.USER)
					.status(UserStatus.ACTIVE).build();

			LoginRequest req = new LoginRequest(email, "raw");

			String access = UUID.randomUUID().toString();
			RefreshToken rt = createSampleRefreshToken(user.getId());

			when(mockUserRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
			Mockito.doNothing().when(mockUserServiceValidator).validateCredentials(anyString(), Mockito.eq(user));
			when(mockJwtService.generateAccessToken(user.getId(), user.getRole())).thenReturn(access);
			when(mockRefreshTokenService.createRefreshToken(user.getId())).thenReturn(rt);

			Token token = userServiceImpl.login(req);

			Assertions.assertNotNull(token);
			Assertions.assertEquals(access, token.getAccessToken());
			Assertions.assertEquals(rt.getToken(), token.getRefreshToken());
		}
	}

	@Test
	@DisplayName("Method: getUser -> should return UserDto when found")
	void getUser_WhenFound_ShouldReturnDto() {
		User user = User.builder().id(5L).email("a@b.com").firstName("A").lastName("B").phoneNumber("0909").build();

		when(mockUserRepository.findById(5L)).thenReturn(Optional.of(user));

		UserDto dto = userServiceImpl.getUser(5L);

		Assertions.assertEquals(user.getEmail(), dto.getEmail());
		Assertions.assertEquals(user.getFirstName(), dto.getFirstName());
		Assertions.assertEquals(user.getLastName(), dto.getLastName());
		Assertions.assertEquals(user.getPhoneNumber(), dto.getPhoneNumber());
	}

	@Test
	@DisplayName("Method: getUser -> should throw when not found")
	void getUser_WhenNotFound_ShouldThrow() {
		when(mockUserRepository.findById(99L)).thenReturn(Optional.empty());
		ResourceException ex = Assertions.assertThrows(ResourceException.class, () -> userServiceImpl.getUser(99L));
		Assertions.assertEquals(ErrorMessageConstant.ID_NOT_FOUND, ex.getMessage());
	}

	@Test
	@DisplayName("Method: updateUser -> should update when validator allows")
	void updateUser_WhenAllowed_ShouldUpdate() {
		Long targetId = 10L;

		Mockito.doNothing().when(mockUserServiceValidator).ensureCanUpdateUser(targetId);

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
	}

	@Test
	@DisplayName("Method: updateUser -> should throw when validator blocks")
	void updateUser_WhenValidatorBlocks_ShouldThrow() {
		Long targetId = 3L;
		Mockito.doThrow(new ResourceException(HttpStatus.FORBIDDEN, "FORBIDDEN")).when(mockUserServiceValidator)
				.ensureCanUpdateUser(targetId);

		Assertions.assertThrows(ResourceException.class,
				() -> userServiceImpl.updateUser(targetId, UserDto.builder().firstName("X").build()));

		Mockito.verifyNoInteractions(mockUserRepository);
	}

	@Test
	@DisplayName("Method: updateUser -> should throw NOT_FOUND when target user not found")
	void updateUser_WhenTargetNotFound_ShouldThrowNotFound() {
		Long targetId = 99L;
		Mockito.doNothing().when(mockUserServiceValidator).ensureCanUpdateUser(targetId);
		when(mockUserRepository.findById(targetId)).thenReturn(Optional.empty());

		ResourceException ex = Assertions.assertThrows(ResourceException.class,
				() -> userServiceImpl.updateUser(targetId, UserDto.builder().build()));
		Assertions.assertEquals(ErrorMessageConstant.ID_NOT_FOUND, ex.getMessage());
	}

	@Test
	@DisplayName("updateUser -> should check existsByEmail when email is changed")
	void updateUser_WhenEmailChanged_ShouldCheckExistsByEmail() {
		Long id = 7L;

		Mockito.doNothing().when(mockUserServiceValidator).ensureCanUpdateUser(id);

		User existing = User.builder().id(id).email("old@e.com").firstName("A").lastName("B").phoneNumber("0909")
				.status(UserStatus.ACTIVE).role(Role.USER).build();
		when(mockUserRepository.findById(id)).thenReturn(Optional.of(existing));

		UserDto dto = UserDto.builder().email("new@e.com").build();

		when(mockUserRepository.existsByEmail("new@e.com")).thenReturn(Optional.of(false));

		userServiceImpl.updateUser(id, dto);

		verify(mockUserRepository).existsByEmail("new@e.com");
		verify(mockUserRepository).update(any(User.class));
	}

	@Test
	@DisplayName("Method: refresh -> should throw INVALID_REFRESH_TOKEN when validator fails")
	void refresh_WhenRefreshTokenInvalid_ShouldThrow() {
		for (int i = 0; i < maxTestCase; i++) {
			String invalid = UUID.randomUUID().toString();
			Mockito.when(mockUserServiceValidator.findAndVerifyRefreshToken(invalid)).thenThrow(
					new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_REFRESH_TOKEN));

			ResourceException ex = Assertions.assertThrows(ResourceException.class,
					() -> userServiceImpl.refresh(invalid));
			Assertions.assertEquals(ErrorMessageConstant.INVALID_REFRESH_TOKEN, ex.getMessage());
		}
	}

	@Test
	@DisplayName("Method: refresh -> should throw INVALID_REFRESH_TOKEN when userId in token does not exist")
	void refresh_WhenUserOfTokenNotFound_ShouldThrow() {
		Long ghostUserId = 777L;
		RefreshToken rt = createSampleRefreshToken(ghostUserId);

		when(mockUserServiceValidator.findAndVerifyRefreshToken(rt.getToken())).thenReturn(rt);
		when(mockUserRepository.findById(ghostUserId)).thenReturn(Optional.empty());

		ResourceException ex = Assertions.assertThrows(ResourceException.class,
				() -> userServiceImpl.refresh(rt.getToken()));
		Assertions.assertEquals(ErrorMessageConstant.INVALID_REFRESH_TOKEN, ex.getMessage());
	}

	@Test
	@DisplayName("Method: refresh -> should return new access token while keeping refresh token")
	void refresh_WhenValid_ShouldReturnNewAccessToken() {
		Long userId = 55L;
		User user = User.builder().id(userId).email("u@e.com").role(Role.USER).status(UserStatus.ACTIVE).build();
		RefreshToken rt = createSampleRefreshToken(userId);

		when(mockUserServiceValidator.findAndVerifyRefreshToken(rt.getToken())).thenReturn(rt);
		when(mockUserRepository.findById(userId)).thenReturn(Optional.of(user));
		String newAccess = UUID.randomUUID().toString();
		when(mockJwtService.generateAccessToken(userId, user.getRole())).thenReturn(newAccess);

		Token token = userServiceImpl.refresh(rt.getToken());

		Assertions.assertEquals(newAccess, token.getAccessToken());
		Assertions.assertEquals(rt.getToken(), token.getRefreshToken());
	}

	@Test
	@DisplayName("Method: getResponseCookieRefreshToken -> should set correct cookie attributes & maxAge")
	void getResponseCookieRefreshToken_ShouldReturnCookieWithConfiguredMaxAge() {
		ReflectionTestUtils.setField(userServiceImpl, "refreshTokenExpirationMs", 7200000L);
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
