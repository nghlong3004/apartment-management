package vn.io.nghlong3004.apartment_management.repository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import vn.io.nghlong3004.apartment_management.model.Role;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserStatus;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	private User createSampleUser() {
		User user = User.builder().firstName("Long").lastName("Nguyen").email("nghlong3004@example.com")
				.password("matkhaune!A@1234").phoneNumber("0987654321").role(Role.USER).status(UserStatus.ACTIVE)
				.floor(null).build();
		return user;
	}

	@Test
	@DisplayName("Method: ExitstByEmail -> False")
	void UserRepository_ExistsByEmail_WhenEmailDoesNotExistShould_ReturnFalse() {

		Optional<Boolean> exists = userRepository.existsByEmail("nghlong3004@example.com");
		Assertions.assertThat(exists.orElse(false)).isEqualTo(false);
	}

	@Test
	@DisplayName("Method: ExitstByEmail -> True")
	void UserRepository_ExistsByEmail_WhenEmailExistsShould_ReturnTrue() {
		userRepository.save(createSampleUser());

		Optional<Boolean> exists = userRepository.existsByEmail("nghlong3004@example.com");

		Assertions.assertThat(exists.orElse(false)).isEqualTo(true);
	}
}