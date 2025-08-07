package vn.io.nghlong3004.apartment_management.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generateEmail;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generateFirstName;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generateLastName;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generatePassword;
import static vn.io.nghlong3004.apartment_management.util.GenerateUtil.generatePhoneNumber;

import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import vn.io.nghlong3004.apartment_management.constants.MessageConstants;
import vn.io.nghlong3004.apartment_management.exception.AccountResourcesException;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.exception.TokenRefreshException;
import vn.io.nghlong3004.apartment_management.model.dto.LoginRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;
import vn.io.nghlong3004.apartment_management.model.dto.Token;
import vn.io.nghlong3004.apartment_management.service.UserService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	private RegisterRequest createSampleRegisterRequest() {
		return RegisterRequest.builder().firstName(generateFirstName()).lastName(generateLastName())
				.email(generateEmail()).password(generatePassword()).phoneNumber(generatePhoneNumber()).build();
	}

	private LoginRequest createSampleLoginRequest() {
		return LoginRequest.builder().email(generateEmail()).password(generatePassword()).build();
	}

	private Token createSampleToken() {
		return Token.builder().accessToken(UUID.randomUUID().toString()).refreshToken(UUID.randomUUID().toString())
				.build();
	}

	@Test
	@DisplayName("Method: registerUser - Should call service and return 201 CREATED when request is valid")
	void registerUser_WhenRequestIsValidShouldReturnCreateds() throws JsonProcessingException, Exception {
		RegisterRequest registerRequest = createSampleRegisterRequest();
		Mockito.doNothing().when(userService).register(any(RegisterRequest.class));

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registerRequest))).andExpect(status().isCreated());
	}

	@Test
	@DisplayName("Method: registerUser - Should return 400 BAD REQUEST when request is invalid")
	void registerUser_WhenRequestIsInvalid_ShouldReturnBadRequest() throws Exception {
		RegisterRequest invalidRequest = RegisterRequest.builder().build();

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Method: registerUser - Should return 400 BAD REQUEST when email already exists")
	void registerUser_WhenServiceThrowsAppException_ShouldReturnBadRequest() throws Exception {

		RegisterRequest validRegisterRequest = createSampleRegisterRequest();

		doThrow(new ResourceException(HttpStatus.BAD_REQUEST, MessageConstants.EXISTS_EMAIL)).when(userService)
				.register(any(RegisterRequest.class));

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRegisterRequest))).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Method: loginUser - Returns 200 OK and tokens on successful login")
	void loginUser_WhenCredentialsAreValid_ShouldReturnOkAndTokens() throws Exception {
		LoginRequest loginRequest = createSampleLoginRequest();
		Token token = createSampleToken();
		ResponseCookie cookie = ResponseCookie.from("refresh_token", token.getRefreshToken()).httpOnly(true).path("/")
				.build();

		when(userService.login(any(LoginRequest.class))).thenReturn(token);
		when(userService.getResponseCookieRefreshToken(token.getRefreshToken())).thenReturn(cookie);

		mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken", Matchers.is(token.getAccessToken())))
				.andExpect(MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE, cookie.toString()));

		verify(userService).login(any(LoginRequest.class));
		verify(userService).getResponseCookieRefreshToken(token.getRefreshToken());
	}

	@Test
	@DisplayName("Method: loginUser - Returns 400 BadRequest when login fails")
	void loginUser_WhenCredentialsAreInvalid_ShouldReturnUnauthorized() throws Exception {
		LoginRequest loginRequest = createSampleLoginRequest();
		when(userService.login(any(LoginRequest.class))).thenThrow(new AccountResourcesException());

		mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isBadRequest());

	}

	@Test
	@DisplayName("Method: refreshToken - Return 200 OK and new token when refresh token is valid")
	void refreshToken_WhenCookieIsValid_ShouldReturnOkAndNewTokens() throws Exception {
		String oldRefreshToken = UUID.randomUUID().toString();
		Token newToken = Token.builder().accessToken(UUID.randomUUID().toString())
				.refreshToken(UUID.randomUUID().toString()).build();
		ResponseCookie newCookie = ResponseCookie.from("refresh_token", newToken.getRefreshToken()).httpOnly(true)
				.path("/").build();
		Cookie refreshTokenCookie = new Cookie("refresh_token", oldRefreshToken);

		when(userService.refresh(oldRefreshToken)).thenReturn(newToken);
		when(userService.getResponseCookieRefreshToken(newToken.getRefreshToken())).thenReturn(newCookie);

		mockMvc.perform(post("/api/v1/auth/refresh-token").cookie(refreshTokenCookie)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken", Matchers.is(newToken.getAccessToken())))
				.andExpect(MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE, newCookie.toString()));

		verify(userService).refresh(oldRefreshToken);
		verify(userService).getResponseCookieRefreshToken(newToken.getRefreshToken());
	}

	@Test
	@DisplayName("Method: refreshToken - Returns 400 BAD REQUEST when there is no refresh token cookie")
	void refreshToken_WhenCookieIsMissing_ShouldReturnBadRequest() throws Exception {
		mockMvc.perform(post("/api/v1/auth/refresh-token")).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Method: refreshToken - Returns 400 BadRequest when refresh token is invalid")
	void refreshToken_WhenTokenIsInvalid_ShouldReturnUnauthorized() throws Exception {
		String invalidRefreshToken = UUID.randomUUID().toString();
		Cookie refreshTokenCookie = new Cookie(UUID.randomUUID().toString(), invalidRefreshToken);
		when(userService.refresh(anyString())).thenThrow(new TokenRefreshException());

		mockMvc.perform(post("/api/v1/auth/refresh-token").cookie(refreshTokenCookie))
				.andExpect(status().isBadRequest());
	}

}
