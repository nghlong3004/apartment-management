package vn.io.nghlong3004.apartment_management.repository;

import static org.assertj.core.api.Assertions.within;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import vn.io.nghlong3004.apartment_management.model.RefreshToken;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class RefreshTokenRepositoryTest {

	@Value("${jwt.refresh-token-expiration-ms}")
	private String REFRESH_TOKEN_EXPIRATION_MS;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	private final int maxTestCaseAll = 10;

	private RefreshToken createRefreshToken() {

		Random random = new Random();

		Long userId = random.nextLong();

		String token = UUID.randomUUID().toString();

		RefreshToken refreshToken = RefreshToken.builder().userId(userId).token(token)
				.expiryDate(Instant.now(Clock.systemUTC()).plusMillis(Long.parseLong(REFRESH_TOKEN_EXPIRATION_MS)))
				.build();

		return refreshToken;
	}

	@Test
	@DisplayName("Method: FindByToken -> Null")
	void findByToken_WhenTokenDoesNotExistShould_ReturnNull() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			String token = UUID.randomUUID().toString();
			RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElse(null);
			Assertions.assertThat(refreshToken).isNull();
		}
	}

	@Test
	@DisplayName("Method: FindByToken -> RefreshToken")
	void findByToken_WhenTokenDoesNotExistShould_ReturnRefreshToken() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			RefreshToken oldRefreshToken = createRefreshToken();
			refreshTokenRepository.save(oldRefreshToken.getUserId(), oldRefreshToken.getToken(),
					oldRefreshToken.getExpiryDate());
			RefreshToken refreshToken = refreshTokenRepository.findByToken(oldRefreshToken.getToken()).orElse(null);

			Assertions.assertThat(refreshToken.getExpiryDate()).isCloseTo(oldRefreshToken.getExpiryDate(),
					within(100, ChronoUnit.MILLIS));
			refreshToken.setExpiryDate(null);
			oldRefreshToken.setExpiryDate(null);
			refreshToken.setId(null);
			Assertions.assertThat(refreshToken).isEqualTo(oldRefreshToken);
		}
	}

	@Test
	@DisplayName("Method: DeleteByUserId")
	void deleteByUserId_ShouldDeleteRefreshTokenByUserId() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			RefreshToken oldRefreshToken = createRefreshToken();
			refreshTokenRepository.save(oldRefreshToken.getUserId(), oldRefreshToken.getToken(),
					oldRefreshToken.getExpiryDate());
			RefreshToken refreshToken = refreshTokenRepository.findByToken(oldRefreshToken.getToken()).orElse(null);
			Assertions.assertThat(refreshToken).isNotNull();

			refreshTokenRepository.deleteByUserId(oldRefreshToken.getUserId());
			RefreshToken deletedRefreshToken = refreshTokenRepository.findByToken(oldRefreshToken.getToken())
					.orElse(null);
			Assertions.assertThat(deletedRefreshToken).isNull();

		}
	}

}
