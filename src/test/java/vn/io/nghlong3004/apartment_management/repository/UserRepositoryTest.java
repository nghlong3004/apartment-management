package vn.io.nghlong3004.apartment_management.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import vn.io.nghlong3004.apartment_management.constants.ApplicationConstants;
import vn.io.nghlong3004.apartment_management.model.Role;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserStatus;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	private final int maxTestCaseAll = 10;

	private User createSampleUser(String username) {
		User user = User.builder().firstName("Long").lastName("Nguyen").email(username + "@example.com")
				.password("matkhaune!A@1234").phoneNumber("0987654321").role(Role.USER).status(UserStatus.ACTIVE)
				.floor(null).build();
		return user;
	}

	@Test
	@DisplayName("Method: ExitstByEmail -> False")
	void UserRepository_ExistsByEmail_WhenEmailDoesNotExistShould_ReturnFalse() {

		for (int i = 0; i < maxTestCaseAll; ++i) {
			String username = UUID.randomUUID().toString();
			Optional<Boolean> exists = userRepository.existsByEmail(username + "@example.com");
			Assertions.assertThat(exists.orElse(false)).isEqualTo(false);
		}
	}

	@Test
	@DisplayName("Method: ExitstByEmail -> True")
	void UserRepository_ExistsByEmail_WhenEmailExistsShould_ReturnTrue() {
		System.out.println(Instant.now().getEpochSecond());
		System.out
				.println(Instant.now().plusMillis(ApplicationConstants.EXPIRY_DATE_REFRESH_TOKEN_MS).getEpochSecond());
		for (int i = 0; i < maxTestCaseAll; ++i) {
			String username = UUID.randomUUID().toString();

			userRepository.save(createSampleUser(username));

			Optional<Boolean> exists = userRepository.existsByEmail(username + "@example.com");

			Assertions.assertThat(exists.orElse(false)).isEqualTo(true);
		}
	}

	@Test
	@DisplayName("Method: FindByEmail -> User")
	void UserRepository_FindByEmail_WhenEmailExistsShould_ReturnUserByEmail() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			String username = UUID.randomUUID().toString();

			User user = createSampleUser(username);
			if (!userRepository.existsByEmail(user.getEmail()).orElse(false)) {
				userRepository.save(user);
			}

			Optional<User> userByEmail = userRepository.findByEmail(user.getEmail());
			userByEmail.orElse(user).setId(null);
			userByEmail.orElse(user).setCreated(null);
			userByEmail.orElse(user).setUpdated(null);
			Assertions.assertThat(userByEmail.orElse(null)).isEqualTo(user);
		}
	}

	@Test
	@DisplayName("Method: FindByEmail -> Null")
	void UserRepository_FindByEmail_WhenEmailExistsShould_ReturnNull() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			String username = UUID.randomUUID().toString();

			Optional<User> userByEmail = userRepository.findByEmail(username + "@example.com");
			Assertions.assertThat(userByEmail.orElse(null)).isNull();
		}
	}

}