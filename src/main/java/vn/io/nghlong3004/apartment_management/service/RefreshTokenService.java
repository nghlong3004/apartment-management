package vn.io.nghlong3004.apartment_management.service;

import java.util.Optional;

import vn.io.nghlong3004.apartment_management.model.RefreshToken;

public interface RefreshTokenService {

	public Optional<RefreshToken> findByToken(String token);

	public RefreshToken createRefreshToken(Long userId);

	public void verifyExpiration(RefreshToken token);

}
