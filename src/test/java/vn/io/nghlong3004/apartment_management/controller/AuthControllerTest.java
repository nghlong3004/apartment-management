package vn.io.nghlong3004.apartment_management.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.dto.LoginRequest;
import vn.io.nghlong3004.apartment_management.model.dto.LoginResponse;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;
import vn.io.nghlong3004.apartment_management.model.dto.Token;
import vn.io.nghlong3004.apartment_management.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@Mock
	private UserService mockUserService;

	@InjectMocks
	private AuthController authController;

	@Captor
	private ArgumentCaptor<RegisterRequest> registerRequestCaptor;

	@Captor
	private ArgumentCaptor<LoginRequest> loginRequestCaptor;

	private RegisterRequest createSampleRegisterRequest() {
		return new RegisterRequest("John", "Doe", "john" + UUID.randomUUID() + "@example.com", "Passw0rd!@#",
				"0909123456");
	}

	private LoginRequest createSampleLoginRequest() {
		return new LoginRequest("john" + UUID.randomUUID() + "@example.com", "Passw0rd!@#");
	}

	private Token createSampleToken() {
		return Token.builder().accessToken("access-" + UUID.randomUUID()).refreshToken("refresh-" + UUID.randomUUID())
				.build();
	}

	@Test
	@DisplayName("POST /register -> should call userService.register with correct request")
	void registerUser_ShouldDelegateToService() {
		RegisterRequest request = createSampleRegisterRequest();

		authController.registerUser(request);

		verify(mockUserService).register(registerRequestCaptor.capture());
		RegisterRequest captured = registerRequestCaptor.getValue();
		Assertions.assertEquals(request, captured);
	}

	@Test
	@DisplayName("POST /login -> should return access token in body and refresh token in cookie")
	void loginUser_ShouldReturnTokenAndCookie() {
		LoginRequest request = createSampleLoginRequest();
		Token token = createSampleToken();
		ResponseCookie cookie = ResponseCookie.from("refresh_token", token.getRefreshToken()).build();

		when(mockUserService.login(request)).thenReturn(token);
		when(mockUserService.getResponseCookieRefreshToken(token.getRefreshToken())).thenReturn(cookie);

		ResponseEntity<LoginResponse> response = authController.loginUser(request);

		verify(mockUserService).login(loginRequestCaptor.capture());
		Assertions.assertEquals(request, loginRequestCaptor.getValue());

		Assertions.assertEquals(200, response.getStatusCode().value());
		Assertions.assertEquals(token.getAccessToken(), response.getBody().getAccessToken());
		Assertions.assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
		String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
		Assertions.assertTrue(setCookie.contains(token.getRefreshToken()));
	}

	@Test
	@DisplayName("POST /refresh-token -> should return new access token and set cookie")
	void refreshToken_ShouldReturnNewTokenAndCookie() {
		String refreshTokenValue = "refresh-" + UUID.randomUUID();
		Token token = createSampleToken();
		ResponseCookie cookie = ResponseCookie.from("refresh_token", token.getRefreshToken()).build();

		when(mockUserService.refresh(refreshTokenValue)).thenReturn(token);
		when(mockUserService.getResponseCookieRefreshToken(token.getRefreshToken())).thenReturn(cookie);

		ResponseEntity<LoginResponse> response = authController.refreshToken(refreshTokenValue);

		verify(mockUserService).refresh(refreshTokenValue);
		Assertions.assertEquals(200, response.getStatusCode().value());
		Assertions.assertEquals(token.getAccessToken(), response.getBody().getAccessToken());
		Assertions.assertTrue(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains(token.getRefreshToken()));
	}

	@Test
	@DisplayName("POST /refresh-token -> propagates ResourceException when service fails (ERROR_REFRESH_TOKEN)")
	void refreshToken_ShouldPropagateException_WhenServiceThrows() {
		String invalidRefreshToken = "refresh-" + UUID.randomUUID();

		when(mockUserService.refresh(invalidRefreshToken))
				.thenThrow(new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_REFRESH_TOKEN));

		Assertions.assertThrows(ResourceException.class, () -> authController.refreshToken(invalidRefreshToken));
	}
}
