package vn.io.nghlong3004.apartment_management.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import vn.io.nghlong3004.apartment_management.model.Floor;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RoomRepositoryTest {

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private FloorRepository floorRepository;

	private String shortFloorName() {
		String rnd = UUID.randomUUID().toString().replace("-", "");
		return "F" + rnd.substring(0, 5);
	}

	private Long createFloor() {
		Floor floor = Floor.builder().name(shortFloorName()).roomCount(0).build();
		floorRepository.insert(floor);
		Floor newFloor = floorRepository.findByName(floor.getName()).orElse(null);
		assertThat(newFloor.getId()).isNotNull();
		return newFloor.getId();
	}

	private Room buildRoom(Long floorId, String name, RoomStatus status) {
		return Room.builder().floorId(floorId).name(name).status(status).build();
	}

	@Test
	@DisplayName("findRoomByFloorIdAndRoomId returns Optional<Room> when exists")
	void findRoomByFloorIdAndRoomId_found() {
		Long floorId = createFloor();
		roomRepository.insert(buildRoom(floorId, "A101", RoomStatus.AVAILABLE));

		List<Room> all = roomRepository.findAllRoomsByFloorId(floorId);
		Long roomId = all.get(0).getId();

		Optional<Room> got = roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId);

		assertThat(got).isPresent();
		assertThat(got.get().getId()).isEqualTo(roomId);
		assertThat(got.get().getFloorId()).isEqualTo(floorId);
		assertThat(got.get().getName()).isEqualTo("A101");
		assertThat(got.get().getStatus()).isEqualTo(RoomStatus.AVAILABLE);
	}

	@Test
	@DisplayName("findRoomByFloorIdAndRoomId returns empty when not found or wrong floor")
	void findRoomByFloorIdAndRoomId_empty() {
		Long f1 = createFloor();
		Long f2 = createFloor();

		roomRepository.insert(buildRoom(f1, "B202", RoomStatus.SOLD));
		Long roomInF1 = roomRepository.findAllRoomsByFloorId(f1).get(0).getId();

		assertThat(roomRepository.findRoomByFloorIdAndRoomId(f2, roomInF1)).isEmpty();
		assertThat(roomRepository.findRoomByFloorIdAndRoomId(f1, -12345L)).isEmpty();
	}

	@Test
	@DisplayName("findAllRoomsByFloorId returns all rooms in floor")
	void findAllRoomsByFloorId_list() {
		Long floorId = createFloor();
		roomRepository.insert(buildRoom(floorId, "C301", RoomStatus.AVAILABLE));
		roomRepository.insert(buildRoom(floorId, "C302", RoomStatus.RESERVED));

		List<Room> rooms = roomRepository.findAllRoomsByFloorId(floorId);

		assertThat(rooms).hasSize(2);
		assertThat(rooms.stream().allMatch(r -> r.getFloorId().equals(floorId))).isTrue();
		assertThat(rooms.stream().map(Room::getName)).containsExactlyInAnyOrder("C301", "C302");
	}

	@Test
	@DisplayName("insert creates new room and can be read back")
	void insert_createAndReadBack() {
		Long floorId = createFloor();

		roomRepository.insert(buildRoom(floorId, "D401", RoomStatus.RESERVED));

		List<Room> list = roomRepository.findAllRoomsByFloorId(floorId);
		assertThat(list).hasSize(1);
		Room got = list.get(0);
		assertThat(got.getName()).isEqualTo("D401");
		assertThat(got.getStatus()).isEqualTo(RoomStatus.RESERVED);
	}

	@Test
	@DisplayName("updateRoom updates name/userId/status")
	void updateRoom_modifyFields() {
		Long floorId = createFloor();
		roomRepository.insert(buildRoom(floorId, "E501", RoomStatus.AVAILABLE));
		Room before = roomRepository.findAllRoomsByFloorId(floorId).get(0);

		before.setName("E501-NEW");
		before.setStatus(RoomStatus.SOLD);

		roomRepository.updateRoom(before);

		Room after = roomRepository.findRoomByFloorIdAndRoomId(floorId, before.getId()).orElseThrow();
		assertThat(after.getName()).isEqualTo("E501-NEW");
		assertThat(after.getStatus()).isEqualTo(RoomStatus.SOLD);
	}

	@Test
	@DisplayName("existsByFloorIdAndName returns true when duplicate exists (case-insensitive)")
	void existsByFloorIdAndName_trueWhenDuplicate() {
		Long floorId = createFloor();
		roomRepository.insert(buildRoom(floorId, "G601", RoomStatus.AVAILABLE));

		assertThat(roomRepository.existsByFloorIdAndName(floorId, "g601").orElse(false)).isTrue();
		assertThat(roomRepository.existsByFloorIdAndName(floorId, "g602").orElse(false)).isFalse();
	}

	@Test
	@DisplayName("existsByFloorIdAndNameExcludingId ignores self when checking")
	void existsByFloorIdAndNameExcludingId_ignoreSelf() {
		Long floorId = createFloor();
		roomRepository.insert(buildRoom(floorId, "H701", RoomStatus.AVAILABLE));
		roomRepository.insert(buildRoom(floorId, "H702", RoomStatus.AVAILABLE));

		Long roomA = roomRepository.findAllRoomsByFloorId(floorId).stream().filter(r -> r.getName().equals("H701"))
				.findFirst().get().getId();
		Long roomB = roomRepository.findAllRoomsByFloorId(floorId).stream().filter(r -> r.getName().equals("H702"))
				.findFirst().get().getId();

		assertThat(roomRepository.existsByFloorIdAndNameExcludingId(floorId, "h701", roomB).orElse(false)).isTrue();
		assertThat(roomRepository.existsByFloorIdAndNameExcludingId(floorId, "h701", roomA).orElse(false)).isFalse();
	}

	@Test
	@DisplayName("deleteByIdAndFloorId deletes correct room")
	void deleteByIdAndFloorId_delete() {
		Long floorId = createFloor();
		roomRepository.insert(buildRoom(floorId, "I801", RoomStatus.RESERVED));
		Long roomId = roomRepository.findAllRoomsByFloorId(floorId).get(0).getId();

		assertThat(roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)).isPresent();

		roomRepository.deleteByIdAndFloorId(roomId, floorId);

		assertThat(roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)).isEmpty();
	}
}
