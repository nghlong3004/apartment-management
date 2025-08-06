package vn.io.nghlong3004.apartment_management.service;

import org.springframework.http.ResponseCookie;

import vn.io.nghlong3004.apartment_management.model.dto.LoginRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;
import vn.io.nghlong3004.apartment_management.model.dto.Token;

public interface UserService {

	public void register(RegisterRequest registerRequest);

	public Token login(LoginRequest loginRequest);

	public Token refresh(String requestRefreshToken);

	public ResponseCookie getResponseCookieRefreshToken(String refreshToken);

}
