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
	private long refreshTokenExpirationMs;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	private final int maxTestCaseAll = 10;

	private RefreshToken createRefreshToken() {

		Random random = new Random();

		Long userId = random.nextLong();

		String token = UUID.randomUUID().toString();

		RefreshToken refreshToken = RefreshToken.builder().userId(userId).token(token)
				.expiryDate(Instant.now(Clock.systemUTC()).plusMillis(refreshTokenExpirationMs)).build();

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
	@DisplayName("Method: DeleteByUserId -> Null")
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

	@Test
	@DisplayName("Method: save/find -> multiple tokens for same user, deleteByUserId removes all")
	void saveMultipleTokensSameUser_thenDeleteAllByUserId() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			Long userId = Math.abs(new Random().nextLong());
			Instant exp1 = Instant.now(Clock.systemUTC()).plusSeconds(600);
			Instant exp2 = Instant.now(Clock.systemUTC()).plusSeconds(1200);

			String t1 = UUID.randomUUID().toString();
			String t2 = UUID.randomUUID().toString();

			refreshTokenRepository.save(userId, t1, exp1);
			refreshTokenRepository.save(userId, t2, exp2);

			Assertions.assertThat(refreshTokenRepository.findByToken(t1)).isPresent();
			Assertions.assertThat(refreshTokenRepository.findByToken(t2)).isPresent();

			refreshTokenRepository.deleteByUserId(userId);

			Assertions.assertThat(refreshTokenRepository.findByToken(t1)).isEmpty();
			Assertions.assertThat(refreshTokenRepository.findByToken(t2)).isEmpty();
		}
	}

	@Test
	@DisplayName("Method: deleteByUserId -> must not affect other user is token")
	void deleteByUserId_ShouldNotAffectOtherUsers() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			Long userA = Math.abs(new Random().nextLong());
			Long userB = Math.abs(new Random().nextLong());

			String tA = UUID.randomUUID().toString();
			String tB = UUID.randomUUID().toString();

			Instant exp = Instant.now(Clock.systemUTC()).plusSeconds(600);
			refreshTokenRepository.save(userA, tA, exp);
			refreshTokenRepository.save(userB, tB, exp);

			refreshTokenRepository.deleteByUserId(userA);

			Assertions.assertThat(refreshTokenRepository.findByToken(tA)).isEmpty();
			Assertions.assertThat(refreshTokenRepository.findByToken(tB)).isPresent();
		}
	}

	@Test
	@DisplayName("Method: save/find -> allow past expiryDate (repo stores verbatim)")
	void saveWithPastExpiry_ShouldBeReadable() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			Long userId = Math.abs(new Random().nextLong());
			String token = UUID.randomUUID().toString();

			Instant past = Instant.now(Clock.systemUTC()).minusSeconds(300);
			refreshTokenRepository.save(userId, token, past);

			RefreshToken found = refreshTokenRepository.findByToken(token).orElse(null);
			Assertions.assertThat(found).isNotNull();
			Assertions.assertThat(found.getExpiryDate()).isCloseTo(past, within(50, ChronoUnit.MILLIS));
			Assertions.assertThat(found.getUserId()).isEqualTo(userId);
			Assertions.assertThat(found.getToken()).isEqualTo(token);
		}
	}

	@Test
	@DisplayName("Method: findByToken -> exact match only")
	void findByToken_ShouldReturnOnlyExactMatch() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			Long userId = Math.abs(new Random().nextLong());
			String exact = UUID.randomUUID().toString();
			String other = UUID.randomUUID().toString();

			Instant exp = Instant.now(Clock.systemUTC()).plusSeconds(600);
			refreshTokenRepository.save(userId, exact, exp);

			Assertions.assertThat(refreshTokenRepository.findByToken(exact)).isPresent();
			Assertions.assertThat(refreshTokenRepository.findByToken(other)).isEmpty();
		}
	}

	@Test
	@DisplayName("Method: deleteByUserId -> no-op when user does not exist")
	void deleteByUserId_WhenUserNotExist_ShouldDoNothing() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			Long existingUser = Math.abs(new Random().nextLong());
			Long nonExisting = Math.abs(new Random().nextLong());

			String token = UUID.randomUUID().toString();
			Instant exp = Instant.now(Clock.systemUTC()).plusSeconds(600);

			refreshTokenRepository.save(existingUser, token, exp);

			refreshTokenRepository.deleteByUserId(nonExisting);

			Assertions.assertThat(refreshTokenRepository.findByToken(token)).isPresent();
		}
	}

}
