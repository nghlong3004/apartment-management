package vn.io.nghlong3004.apartment_management.service;

import java.util.Optional;

import vn.io.nghlong3004.apartment_management.model.RefreshToken;

public interface RefreshTokenService {

	Optional<RefreshToken> findByToken(String token);

	RefreshToken createRefreshToken(Long userId);

	void verifyExpiration(RefreshToken token);

}
