package vn.io.nghlong3004.apartment_management.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

	@PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.CREATED)
	public void registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
		userService.register(registerRequest);
	}

	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
		Token token = userService.login(loginRequest);
		return returnAccessTokenAndRefreshToken(token);
	}

	@PostMapping(value = "/refresh-token", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoginResponse> refreshToken(@CookieValue(name = "refresh_token") String requestRefreshToken) {
		Token token = userService.refresh(requestRefreshToken);
		return returnAccessTokenAndRefreshToken(token);
	}

	private ResponseEntity<LoginResponse> returnAccessTokenAndRefreshToken(Token token) {
		LoginResponse loginResponse = LoginResponse.builder().accessToken(token.getAccessToken()).build();
		ResponseCookie refreshTokenCookie = userService.getResponseCookieRefreshToken(token.getRefreshToken());
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString()).body(loginResponse);
	}

}