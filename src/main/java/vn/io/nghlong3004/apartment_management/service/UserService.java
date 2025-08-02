package vn.io.nghlong3004.apartment_management.service;

import vn.io.nghlong3004.apartment_management.exception.AppException;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;

public interface UserService {

	public void register(RegisterRequest registerRequest) throws AppException;

}
