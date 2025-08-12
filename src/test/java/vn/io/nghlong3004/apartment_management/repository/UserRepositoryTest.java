package vn.io.nghlong3004.apartment_management.repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.transaction.TestTransaction;

import vn.io.nghlong3004.apartment_management.model.Role;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserStatus;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

	@Value("${jwt.refresh-token-expiration-ms}")
	private long refreshTokenExpirationMs;

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
	void existsByEmail_WhenEmailDoesNotExistShould_ReturnFalse() {

		for (int i = 0; i < maxTestCaseAll; ++i) {
			String username = UUID.randomUUID().toString();
			Optional<Boolean> exists = userRepository.existsByEmail(username + "@example.com");
			Assertions.assertThat(exists.orElse(false)).isEqualTo(false);
		}
	}

	@Test
	@DisplayName("Method: ExitstByEmail -> True")
	void existsByEmail_WhenEmailExistsShould_ReturnTrue() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			String username = UUID.randomUUID().toString();

			userRepository.save(createSampleUser(username));

			Optional<Boolean> exists = userRepository.existsByEmail(username + "@example.com");

			Assertions.assertThat(exists.orElse(false)).isEqualTo(true);
		}
	}

	@Test
	@DisplayName("Method: FindByEmail -> User")
	void findByEmail_WhenEmailExistsShould_ReturnUserByEmail() {
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
	void findByEmail_WhenEmailExistsShould_ReturnNull() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			String username = UUID.randomUUID().toString();

			Optional<User> userByEmail = userRepository.findByEmail(username + "@example.com");
			Assertions.assertThat(userByEmail.orElse(null)).isNull();
		}
	}

	@Test
	@DisplayName("Method: findById -> empty when id does not exist")
	void findById_WhenNotExists_ShouldReturnEmpty() {
		Optional<User> notFound = userRepository.findById(-123456789L);
		Assertions.assertThat(notFound).isEmpty();
	}

	@Test
	@DisplayName("Method: save -> findById should return the same user fields (ignoring id/created/updated)")
	void save_then_findById_should_ReturnUser() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			String username = UUID.randomUUID().toString();
			User toSave = createSampleUser(username);

			userRepository.save(toSave);

			Optional<User> fromEmail = userRepository.findByEmail(toSave.getEmail());
			Assertions.assertThat(fromEmail).isPresent();

			Long id = fromEmail.get().getId();
			Optional<User> fromId = userRepository.findById(id);
			Assertions.assertThat(fromId).isPresent();

			User got = fromId.get();
			Assertions.assertThat(got.getFirstName()).isEqualTo(toSave.getFirstName());
			Assertions.assertThat(got.getLastName()).isEqualTo(toSave.getLastName());
			Assertions.assertThat(got.getEmail()).isEqualTo(toSave.getEmail());
			Assertions.assertThat(got.getPhoneNumber()).isEqualTo(toSave.getPhoneNumber());
			Assertions.assertThat(got.getRole()).isEqualTo(toSave.getRole());
			Assertions.assertThat(got.getStatus()).isEqualTo(toSave.getStatus());
		}
	}

	@Test
	@DisplayName("Method: findPasswordByEmail -> return password when email exists")
	void findPasswordByEmail_WhenExists_ShouldReturnPassword() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			String username = UUID.randomUUID().toString();
			User user = createSampleUser(username);
			userRepository.save(user);

			Optional<String> pwd = userRepository.findPasswordByEmail(user.getEmail());
			Assertions.assertThat(pwd).isPresent();
			Assertions.assertThat(pwd.get()).isEqualTo(user.getPassword());
		}
	}

	@Test
	@DisplayName("Method: findPasswordByEmail -> empty when email not found")
	void findPasswordByEmail_WhenNotExists_ShouldReturnEmpty() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			String email = UUID.randomUUID() + "@not-exists.com";
			Optional<String> pwd = userRepository.findPasswordByEmail(email);
			Assertions.assertThat(pwd).isEmpty();
		}
	}

	@Test
	@DisplayName("Method: update -> modify fields, keep 'created' unchanged, and change 'updated'")
	void update_ShouldModifyFields_KeepCreated_ChangeUpdated() {
		String username = UUID.randomUUID().toString();
		User user = createSampleUser(username);
		userRepository.save(user);

		User before = userRepository.findByEmail(user.getEmail()).orElseThrow();
		Timestamp createdBefore = before.getCreated();
		Timestamp updatedBefore = before.getUpdated();

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		String newEmail = UUID.randomUUID().toString() + "@example.com";
		before.setFirstName("UpdatedFirst");
		before.setLastName("UpdatedLast");
		before.setPhoneNumber("0900000000");
		before.setEmail(newEmail);
		userRepository.update(before);
		User after = userRepository.findByEmail(newEmail).orElseThrow();
		Assertions.assertThat(after.getFirstName()).isEqualTo("UpdatedFirst");
		Assertions.assertThat(after.getLastName()).isEqualTo("UpdatedLast");
		Assertions.assertThat(after.getPhoneNumber()).isEqualTo("0900000000");
		Assertions.assertThat(after.getEmail()).isEqualTo(newEmail);

		if (createdBefore != null && after.getCreated() != null) {
			Assertions.assertThat(after.getCreated()).isEqualTo(createdBefore);
		}

		if (updatedBefore != null && after.getUpdated() != null) {
			Assertions.assertThat(after.getUpdated()).isNotEqualTo(updatedBefore);
		} else {
			Assertions.assertThat(after.getUpdated()).isNotNull();
		}
	}

}