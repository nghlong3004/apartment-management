package vn.io.nghlong3004.apartment_management.service;

import org.springframework.http.ResponseCookie;

import vn.io.nghlong3004.apartment_management.model.dto.LoginRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;
import vn.io.nghlong3004.apartment_management.model.dto.Token;
import vn.io.nghlong3004.apartment_management.model.dto.UserDto;

public interface UserService {

	void register(RegisterRequest registerRequest);

	Token login(LoginRequest loginRequest);

	Token refresh(String requestRefreshToken);

	ResponseCookie getResponseCookieRefreshToken(String refreshToken);

	void updateUser(Long id, UserDto userDto);

	UserDto getUser(Long id);

}
