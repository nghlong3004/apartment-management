package vn.io.nghlong3004.apartment_management.service.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.io.nghlong3004.apartment_management.exception.TokenRefreshException;
import vn.io.nghlong3004.apartment_management.model.RefreshToken;
import vn.io.nghlong3004.apartment_management.repository.RefreshTokenRepository;
import vn.io.nghlong3004.apartment_management.service.RefreshTokenService;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

	@Value("${jwt.refresh-token-expiration-ms}")
	private String REFRESH_TOKEN_EXPIRATION_MS;

	private final RefreshTokenRepository refreshTokenRepository;

	@Override
	public Optional<RefreshToken> findByToken(String token) {
		return refreshTokenRepository.findByToken(token);
	}

	@Override
	public RefreshToken createRefreshToken(Long userId) {
		refreshTokenRepository.deleteByUserId(userId);
		RefreshToken refreshToken = RefreshToken.builder().token(UUID.randomUUID().toString())
				.expiryDate(Instant.now().plusMillis(Long.parseLong(REFRESH_TOKEN_EXPIRATION_MS))).build();

		refreshTokenRepository.save(userId, refreshToken.getToken(), refreshToken.getExpiryDate());

		return refreshToken;
	}

	@Override
	public void verifyExpiration(RefreshToken token) {
		if (token.getExpiryDate().isBefore(Instant.now())) {
			refreshTokenRepository.deleteByUserId(token.getUserId());
			throw new TokenRefreshException();
		}
	}

}
