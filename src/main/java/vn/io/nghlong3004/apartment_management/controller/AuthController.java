package vn.io.nghlong3004.apartment_management.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.io.nghlong3004.apartment_management.model.dto.LoginRequest;
import vn.io.nghlong3004.apartment_management.model.dto.LoginResponse;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;
import vn.io.nghlong3004.apartment_management.model.dto.Token;
import vn.io.nghlong3004.apartment_management.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;

	@PostMapping("/register")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
		userService.register(registerRequest);
	}

	@PostMapping(path = "/login", consumes = "application/json")
	public LoginResponse loginUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
		Token token = userService.login(loginRequest);
		return returnAccessTokenAndRefreshToken(token, response);
	}

	@PostMapping(path = "/refresh-token", consumes = "application/json")
	public LoginResponse refreshToken(@CookieValue(name = "refresh_token") String requestRefreshToken,
			HttpServletResponse response) {
		Token token = userService.refresh(requestRefreshToken);
		return returnAccessTokenAndRefreshToken(token, response);
	}

	private LoginResponse returnAccessTokenAndRefreshToken(Token token, HttpServletResponse response) {
		LoginResponse loginResponse = LoginResponse.builder().accessToken(token.getAccessToken()).build();

		ResponseCookie refreshTokenCookie = userService.getResponseCookieRefreshToken(token.getRefreshToken());

		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

		return loginResponse;
	}

}