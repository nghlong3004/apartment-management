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

import vn.io.nghlong3004.apartment_management.model.Floor;
import vn.io.nghlong3004.apartment_management.model.RequestStatus;
import vn.io.nghlong3004.apartment_management.model.RequestType;
import vn.io.nghlong3004.apartment_management.model.Role;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserStatus;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class FloorRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FloorRepository floorRepository;

	@Autowired
	private RoomRepository roomRepository;

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
			Long floorId = insertFloorAndGetId(shortName("F-", 10), 0);
			user = userRepository.findByEmail(user.getEmail()).orElse(user);
			Room room = Room.builder().name("R-101").floorId(floorId).build();
			roomRepository.insert(room);
			Long roomId = roomRepository.findByFloorIdAndName(floorId, room.getName()).orElse(null).getId();
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
		Long floorId = insertFloorAndGetId(shortName("F-", 10), 0);
		user = userRepository.findByEmail(user.getEmail()).orElse(user);
		Room room = Room.builder().name("R-101").floorId(floorId).build();
		roomRepository.insert(room);
		Long roomId = roomRepository.findByFloorIdAndName(floorId, room.getName()).orElse(null).getId();
		floorRepository.createRequest(user.getId(), floorId, roomId, RequestType.MOVE, RequestStatus.PENDING);

		assertThat(floorRepository.existsPendingRequest(user.getId(), RequestType.MOVE).orElse(false)).isTrue();
		assertThat(floorRepository.existsPendingRequest(user.getId(), RequestType.JOIN).orElse(false)).isFalse();
	}

	private String shortName(String prefix, int max) {
		String rnd = UUID.randomUUID().toString().replace("-", "");
		String base = prefix + rnd;
		return base.substring(0, Math.min(base.length(), max));
	}

	private Floor buildFloor(String name, int roomCount) {
		return Floor.builder().name(name).roomCount(roomCount).build();
	}

	private Long insertFloorAndGetId(String name, int roomCount) {
		Floor f = buildFloor(name, roomCount);
		floorRepository.insert(f);
		Long id = floorRepository.findByName(name).orElseThrow().getId();
		return id;
	}

	private void insertRoom(Long floorId, String name, RoomStatus status) {
		Room r = Room.builder().floorId(floorId).name(name).status(status).build();
		roomRepository.insert(r);
	}

	@Test
	@DisplayName("findById -> empty when not exists")
	void findById_empty() {
		Optional<Floor> got = floorRepository.findById(-99999L);
		assertThat(got).isEmpty();
	}

	@Test
	@DisplayName("insert -> findById returns same core fields")
	void insert_then_findById() {
		String name = shortName("F", 10);
		Long id = insertFloorAndGetId(name, 0);

		Floor got = floorRepository.findById(id).orElseThrow();
		assertThat(got.getName()).isEqualTo(name);
		assertThat(got.getRoomCount()).isEqualTo(0);
	}

	@Test
	@DisplayName("findByName (case-insensitive) -> returns floor")
	void findByName_caseInsensitive() {
		String name = shortName("Fx", 10);
		Long id = insertFloorAndGetId(name, 0);

		Optional<Floor> lower = floorRepository.findByName(name.toLowerCase());
		Optional<Floor> upper = floorRepository.findByName(name.toUpperCase());

		assertThat(lower).isPresent();
		assertThat(upper).isPresent();
		assertThat(lower.get().getId()).isEqualTo(id);
		assertThat(upper.get().getId()).isEqualTo(id);
	}

	@Test
	@DisplayName("updateFloor -> modifies fields and keeps id")
	void updateFloor_updates() {
		String name = shortName("FU", 10);
		Long id = insertFloorAndGetId(name, 2);

		Floor existing = floorRepository.findById(id).orElseThrow();
		existing.setName(shortName("NEW", 10));
		existing.setRoomCount(9);

		floorRepository.updateFloor(existing);

		Floor updated = floorRepository.findById(id).orElseThrow();
		assertThat(updated.getName()).isEqualTo(existing.getName());
		assertThat(updated.getRoomCount()).isEqualTo(9);
	}

	@Test
	@DisplayName("deleteById -> removes row")
	void deleteById_removes() {
		String name = shortName("FD", 10);
		Long id = insertFloorAndGetId(name, 0);

		assertThat(floorRepository.findById(id)).isPresent();

		floorRepository.deleteById(id);

		assertThat(floorRepository.findById(id)).isEmpty();
	}

	@Test
	@DisplayName("floorExists -> true/false")
	void floorExists_trueFalse() {
		Long ghost = -1L;
		assertThat(floorRepository.floorExists(ghost).orElse(false)).isFalse();

		String name = shortName("FE", 10);
		Long id = insertFloorAndGetId(name, 0);

		assertThat(floorRepository.floorExists(id).orElse(false)).isTrue();
	}

	@Test
	@DisplayName("incrementRoomCount/decrementRoomCount -> updates counters and not below zero")
	void incDecRoomCount() {
		String name = shortName("FC", 10);
		Long id = insertFloorAndGetId(name, 0);

		int a = floorRepository.incrementRoomCount(id);
		int b = floorRepository.incrementRoomCount(id);
		assertThat(a).isEqualTo(1);
		assertThat(b).isEqualTo(1);

		Floor afterInc = floorRepository.findById(id).orElseThrow();
		assertThat(afterInc.getRoomCount()).isEqualTo(2);

		int c = floorRepository.decrementRoomCount(id);
		int d = floorRepository.decrementRoomCount(id);
		int e = floorRepository.decrementRoomCount(id);
		assertThat(c).isEqualTo(1);
		assertThat(d).isEqualTo(1);
		assertThat(e).isEqualTo(1);

		Floor afterDec = floorRepository.findById(id).orElseThrow();
		assertThat(afterDec.getRoomCount()).isEqualTo(0);
	}

	@Test
	@DisplayName("countRoomsByFloorId -> returns number of rooms in that floor")
	void countRoomsByFloorId_counts() {
		String n1 = shortName("F1", 10);
		String n2 = shortName("F2", 10);
		Long f1 = insertFloorAndGetId(n1, 0);
		Long f2 = insertFloorAndGetId(n2, 0);

		insertRoom(f1, "A101", RoomStatus.AVAILABLE);
		insertRoom(f1, "A102", RoomStatus.SOLD);
		insertRoom(f2, "B201", RoomStatus.RESERVED);

		long c1 = floorRepository.countRoomsByFloorId(f1);
		long c2 = floorRepository.countRoomsByFloorId(f2);
		assertThat(c1).isEqualTo(2L);
		assertThat(c2).isEqualTo(1L);
	}
}
