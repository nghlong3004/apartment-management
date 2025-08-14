package vn.io.nghlong3004.apartment_management.repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.Random;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.transaction.TestTransaction;

import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RoomRepositoryTest {

	@Autowired
	private RoomRepository roomRepository;

	private final int maxTestCaseAll = 10;

	@Test
	@DisplayName("Method: findRoomByFloorIdAndRoomId -> empty when not found")
	void findRoomByFloorIdAndRoomId_WhenNotFound_ShouldReturnEmpty() {
		for (int i = 0; i < maxTestCaseAll; ++i) {
			Long randomFloor = Math.abs(new Random().nextLong()) + 1;
			Long randomRoom = Math.abs(new Random().nextLong()) + 1;

			Optional<Room> result = roomRepository.findRoomByFloorIdAndRoomId(randomFloor, -randomRoom);
			Assertions.assertThat(result).isEmpty();
		}
	}

	@Test
	@DisplayName("Method: findRoomByFloorIdAndRoomId -> returns Room when found")
	void findRoomByFloorIdAndRoomId_WhenFound_ShouldReturnRoom() {
		Long floorId = 1L;
		String name = "Room-" + 1;
		RoomStatus status = RoomStatus.AVAILABLE;

		Long id = 1L;

		Optional<Room> found = roomRepository.findRoomByFloorIdAndRoomId(floorId, id);
		Assertions.assertThat(found).isPresent();

		Room r = found.get();
		Assertions.assertThat(r.getId()).isEqualTo(id);
		Assertions.assertThat(r.getFloorId()).isEqualTo(floorId);
		Assertions.assertThat(r.getName()).isEqualTo(name);
		Assertions.assertThat(r.getStatus()).isEqualTo(status);
	}

	@Test
	@DisplayName("Method: updateRoom -> modify fields (userId, name, status), keep 'created', change 'updated'")
	void updateRoom_ShouldModifyFields_KeepCreated_ChangeUpdated() {
		Long floorId = 1L;
		Long id = 1L;

		Room before = roomRepository.findRoomByFloorIdAndRoomId(floorId, id).orElseThrow();
		Timestamp createdBefore = before.getCreated();
		Timestamp updatedBefore = before.getUpdated();

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		Room toUpdate = Room.builder().id(id).floorId(floorId).name("R-Updated-" + 2).status(RoomStatus.SOLD).build();

		roomRepository.updateRoom(toUpdate);

		Room after = roomRepository.findRoomByFloorIdAndRoomId(floorId, id).orElseThrow();

		Assertions.assertThat(after.getName()).isEqualTo("R-Updated-" + 2);
		Assertions.assertThat(after.getStatus()).isEqualTo(RoomStatus.SOLD);

		if (createdBefore != null && after.getCreated() != null) {
			Assertions.assertThat(after.getCreated()).isEqualTo(createdBefore);
		}
		if (updatedBefore != null && after.getUpdated() != null) {
			Assertions.assertThat(after.getUpdated()).isNotEqualTo(updatedBefore);
		} else {
			Assertions.assertThat(after.getUpdated()).isNotNull();
		}

	}

	@Test
	@DisplayName("Method: updateRoom -> change floor_id and verify querying by new floor works")
	void updateRoom_ShouldChangeFloorAndBeQueryableByNewFloor() {
		Long oldFloor = 2L;
		Long id = 1l;

		Long newFloor = 1L;

		Room toUpdate = Room.builder().id(id).floorId(newFloor).name("Moved-" + 1).status(RoomStatus.RESERVED).build();

		roomRepository.updateRoom(toUpdate);

		Assertions.assertThat(roomRepository.findRoomByFloorIdAndRoomId(oldFloor, id)).isEmpty();

		Room after = roomRepository.findRoomByFloorIdAndRoomId(newFloor, id).orElseThrow();
		Assertions.assertThat(after.getFloorId()).isEqualTo(newFloor);
		Assertions.assertThat(after.getName()).isEqualTo("Moved-" + 1);
		Assertions.assertThat(after.getStatus()).isEqualTo(RoomStatus.RESERVED);
		Assertions.assertThat(after.getUpdated()).isNotNull();
	}
}
