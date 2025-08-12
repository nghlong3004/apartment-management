package vn.io.nghlong3004.apartment_management.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.RefreshToken;
import vn.io.nghlong3004.apartment_management.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

	@Mock
	private RefreshTokenRepository mockRefreshTokenRepository;

	@InjectMocks
	private RefreshTokenServiceImpl refreshTokenServiceImpl;

	@Captor
	private ArgumentCaptor<String> tokenCaptor;

	@Captor
	private ArgumentCaptor<Instant> expiryDateCaptor;

	private int maxTestCase = 10;

	@Test
	@DisplayName("Method: findByToken -> Returns Optional containing token if token exists")
	void findByToken_WhenTokenExists_ShouldReturnOptionalOfToken() {
		for (int i = 0; i < maxTestCase; ++i) {
			String tokenValue = UUID.randomUUID().toString();
			RefreshToken expectedToken = RefreshToken.builder().id(1L).token(tokenValue).userId(123L)
					.expiryDate(Instant.now().plus(1, ChronoUnit.DAYS)).build();
			Mockito.when(mockRefreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expectedToken));

			Optional<RefreshToken> actualTokenOptional = refreshTokenServiceImpl.findByToken(tokenValue);

			Assertions.assertTrue(actualTokenOptional.isPresent());
			Assertions.assertEquals(expectedToken, actualTokenOptional.get());
			Mockito.verify(mockRefreshTokenRepository).findByToken(tokenValue);
		}
	}

	@Test
	@DisplayName("Method: findByToken -> Returns empty Optional when token does not exist")
	void findByToken_WhenTokenDoesNotExist_ShouldReturnEmptyOptional() {
		for (int i = 0; i < maxTestCase; ++i) {
			String tokenValue = UUID.randomUUID().toString();
			Mockito.when(mockRefreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

			Optional<RefreshToken> actualTokenOptional = refreshTokenServiceImpl.findByToken(tokenValue);

			Assertions.assertFalse(actualTokenOptional.isPresent());
			Mockito.verify(mockRefreshTokenRepository).findByToken(tokenValue);
		}
	}

	@Test
	@DisplayName("Method: createRefreshToken -> Delete old token and save new token successfully")
	void createRefreshToken_WhenCalled_ShouldDeleteOldAndSaveNewToken() {
		for (int i = 0; i < maxTestCase; ++i) {
			Long userId = new Random().nextLong();
			Mockito.doNothing().when(mockRefreshTokenRepository).deleteByUserId(userId);
			Mockito.doNothing().when(mockRefreshTokenRepository).save(Mockito.eq(userId), tokenCaptor.capture(),
					expiryDateCaptor.capture());

			RefreshToken createdToken = refreshTokenServiceImpl.createRefreshToken(userId);

			Assertions.assertNotNull(createdToken);
			Assertions.assertNotNull(createdToken.getToken());

			Mockito.verify(mockRefreshTokenRepository).deleteByUserId(userId);

			Mockito.verify(mockRefreshTokenRepository).save(Mockito.eq(userId), Mockito.anyString(),
					Mockito.any(Instant.class));

			Assertions.assertEquals(createdToken.getToken(), tokenCaptor.getValue());
			Assertions.assertEquals(createdToken.getExpiryDate(), expiryDateCaptor.getValue());
		}
	}

	@Test
	@DisplayName("Method: verifyExpiration -> Nothing")
	void verifyExpiration_WhenTokenIsNotExpired_ShouldDoNothing() {
		for (int i = 0; i < maxTestCase; ++i) {
			RefreshToken validToken = RefreshToken.builder().userId(new Random().nextLong())
					.expiryDate(Instant.now().plus(100, ChronoUnit.MILLIS)).build();

			Assertions.assertDoesNotThrow(() -> {
				refreshTokenServiceImpl.verifyExpiration(validToken);
			});

			Mockito.verify(mockRefreshTokenRepository, Mockito.never()).deleteByUserId(Mockito.anyLong());
		}
	}

	@Test
	@DisplayName("Method: verifyExpiration -> Throw TokenRefreshException and delete token when expired")
	void verifyExpiration_WhenTokenIsExpired_ShouldDeleteTokenAndThrowException() {
		for (int i = 0; i < maxTestCase; ++i) {
			Long userId = new Random().nextLong();
			RefreshToken expiredToken = RefreshToken.builder().userId(userId)
					.expiryDate(Instant.now().minus(100, ChronoUnit.MILLIS)).build();
			Mockito.doNothing().when(mockRefreshTokenRepository).deleteByUserId(userId);

			Assertions.assertThrows(ResourceException.class, () -> {
				refreshTokenServiceImpl.verifyExpiration(expiredToken);
			});

			Mockito.verify(mockRefreshTokenRepository).deleteByUserId(userId);
		}
	}
}
