package vn.io.nghlong3004.apartment_management.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import vn.io.nghlong3004.apartment_management.model.RequestStatus;
import vn.io.nghlong3004.apartment_management.model.RequestType;
import vn.io.nghlong3004.apartment_management.model.Role;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserStatus;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class FloorRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FloorRepository floorRepository;

	private final int maxTestCaseAll = 10;

	private User createSampleUser(String username) {
		User user = User.builder().firstName("Long").lastName("Nguyen").email(username + "@example.com")
				.password("matkhaune!A@1234").phoneNumber("0987654321").role(Role.USER).status(UserStatus.ACTIVE)
				.floor(null).build();
		return user;
	}

	@Test
	@DisplayName("Method: existsPendingRequest -> false when no pending request")
	void existsPendingRequest_ShouldReturnFalse_WhenNoRequest() {
		for (int i = 0; i < maxTestCaseAll; i++) {
			Long randomUserId = Math.abs(new Random().nextLong());
			Optional<Boolean> exists = floorRepository.existsPendingRequest(randomUserId, RequestType.JOIN);
			assertThat(exists.orElse(false)).isFalse();
		}
	}

	@Test
	@DisplayName("Method: createRequest -> should insert and existsPendingRequest return true")
	void createRequest_ShouldInsertAndReturnTrue() {
		for (int i = 0; i < maxTestCaseAll; i++) {
			String username = String.valueOf(UUID.randomUUID());
			User user = createSampleUser(username);
			userRepository.save(user);
			Long floorId = 1L;
			Long roomId = 1L;
			user = userRepository.findByEmail(user.getEmail()).orElse(null);
			floorRepository.createRequest(user.getId(), floorId, roomId, RequestType.JOIN, RequestStatus.PENDING);

			Optional<Boolean> exists = floorRepository.existsPendingRequest(user.getId(), RequestType.JOIN);
			assertThat(exists.orElse(false)).isTrue();
		}
	}

	@Test
	@DisplayName("Method: existsPendingRequest -> only returns true for correct type")
	void existsPendingRequest_ShouldBeTypeSpecific() {
		String username = String.valueOf(UUID.randomUUID());
		User user = createSampleUser(username);
		userRepository.save(user);
		Long floorId = 1L;
		Long roomId = 1L;
		user = userRepository.findByEmail(user.getEmail()).orElse(user);
		floorRepository.createRequest(user.getId(), floorId, roomId, RequestType.MOVE, RequestStatus.PENDING);

		assertThat(floorRepository.existsPendingRequest(user.getId(), RequestType.MOVE).orElse(false)).isTrue();
		assertThat(floorRepository.existsPendingRequest(user.getId(), RequestType.JOIN).orElse(false)).isFalse();
	}
}
