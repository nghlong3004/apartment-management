package vn.io.nghlong3004.apartment_management.service;

import vn.io.nghlong3004.apartment_management.model.dto.LoginRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;
import vn.io.nghlong3004.apartment_management.model.dto.Token;

public interface UserService {

	public void register(RegisterRequest registerRequest);

	public Token login(LoginRequest loginRequest);

}
