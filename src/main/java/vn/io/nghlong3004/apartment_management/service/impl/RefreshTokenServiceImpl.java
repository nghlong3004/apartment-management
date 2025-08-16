package vn.io.nghlong3004.apartment_management.service.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.RefreshToken;
import vn.io.nghlong3004.apartment_management.repository.RefreshTokenRepository;
import vn.io.nghlong3004.apartment_management.service.RefreshTokenService;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

	@Value("${jwt.refresh-token-expiration-ms}")
	private long refreshTokenExpirationMs;

	private final RefreshTokenRepository refreshTokenRepository;

	@Override
	@Transactional(readOnly = true)
	public Optional<RefreshToken> findByToken(String token) {
		log.debug("Attempting to find refresh token in the database.");
		Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByToken(token);
		if (tokenOptional.isPresent()) {
			log.debug("Refresh token found for user ID: {}", tokenOptional.get().getUserId());
		} else {
			log.debug("Refresh token not found in the database.");
		}
		return tokenOptional;
	}

	@Override
	@Transactional
	public RefreshToken createRefreshToken(Long userId) {
		log.info("Request to create a new refresh token for user ID: {}", userId);

		log.debug("Deleting any existing refresh token for user ID: {}", userId);
		refreshTokenRepository.deleteByUserId(userId);

		RefreshToken refreshToken = RefreshToken.builder().userId(userId).token(UUID.randomUUID().toString())
				.expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs)).build();

		log.debug("Saving new refresh token to the database for user ID: {}", userId);
		refreshTokenRepository.save(userId, refreshToken.getToken(), refreshToken.getExpiryDate());

		log.info("Successfully created and saved a new refresh token for user ID: {}", userId);
		return refreshToken;
	}

	@Override
	@Transactional
	public void verifyExpiration(RefreshToken token) {
		log.debug("Verifying expiration for the refresh token of user ID: {}", token.getUserId());

		if (token.getExpiryDate().isBefore(Instant.now())) {
			log.warn("Refresh token for user ID: {} has expired. Deleting it from the database.", token.getUserId());
			refreshTokenRepository.deleteByUserId(token.getUserId());
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.REFRESH_TOKEN_EXPIRED);
		}

		log.debug("Refresh token for user ID: {} is still valid.", token.getUserId());
	}
}