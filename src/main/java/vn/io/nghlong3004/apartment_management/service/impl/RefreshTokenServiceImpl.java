package vn.io.nghlong3004.apartment_management.service.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.io.nghlong3004.apartment_management.constants.ApplicationConstants;
import vn.io.nghlong3004.apartment_management.exception.TokenRefreshException;
import vn.io.nghlong3004.apartment_management.model.RefreshToken;
import vn.io.nghlong3004.apartment_management.repository.RefreshTokenRepository;
import vn.io.nghlong3004.apartment_management.service.RefreshTokenService;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

	@Override
	public Optional<RefreshToken> findByToken(String token) {
		return refreshTokenRepository.findByToken(token);
	}

	@Override
	public RefreshToken createRefreshToken(Long userId) {
		refreshTokenRepository.deleteByUserId(userId);
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setToken(UUID.randomUUID().toString());
		refreshToken.setExpiryDate(Instant.now().plusMillis(ApplicationConstants.EXPIRY_DATE_REFRESH_TOKEN_MS));

		refreshTokenRepository.save(userId, refreshToken.getToken(), refreshToken.getExpiryDate());

		return refreshToken;
	}

	@Override
	public RefreshToken verifyExpiration(RefreshToken token) {
		if (token.getExpiryDate().isBefore(Instant.now())) {
			refreshTokenRepository.deleteByUserId(token.getUserId());
			throw new TokenRefreshException();
		}
		return token;
	}

}
