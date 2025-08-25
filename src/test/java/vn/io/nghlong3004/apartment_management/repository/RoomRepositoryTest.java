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

	private String shortRoomName() {
		String rnd = UUID.randomUUID().toString().replace("-", "");
		return "Room " + rnd.substring(0, 4);
	}

	private String shortFloorName() {
		String rnd = UUID.randomUUID().toString().replace("-", "");
		return "Room " + rnd.substring(0, 4);
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
		String name = shortRoomName();
		roomRepository.insert(buildRoom(floorId, name, RoomStatus.AVAILABLE));

		List<Room> all = roomRepository.findAllRoomsByFloorId(floorId);
		Long roomId = all.get(0).getId();

		Optional<Room> got = roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId);

		assertThat(got).isPresent();
		assertThat(got.get().getId()).isEqualTo(roomId);
		assertThat(got.get().getFloorId()).isEqualTo(floorId);
		assertThat(got.get().getName()).isEqualTo(name);
		assertThat(got.get().getStatus()).isEqualTo(RoomStatus.AVAILABLE);
	}

	@Test
	@DisplayName("findRoomByFloorIdAndRoomId returns empty when not found or wrong floor")
	void findRoomByFloorIdAndRoomId_empty() {
		Long f1 = createFloor();
		Long f2 = createFloor();
		String name = shortRoomName();
		roomRepository.insert(buildRoom(f1, name, RoomStatus.SOLD));
		Long roomInF1 = roomRepository.findAllRoomsByFloorId(f1).get(0).getId();

		assertThat(roomRepository.findRoomByFloorIdAndRoomId(f2, roomInF1)).isEmpty();
		assertThat(roomRepository.findRoomByFloorIdAndRoomId(f1, -12345L)).isEmpty();
	}

	@Test
	@DisplayName("findAllRoomsByFloorId returns all rooms in floor")
	void findAllRoomsByFloorId_list() {
		Long floorId = createFloor();
		String name1 = shortRoomName();
		String name2 = shortRoomName();
		roomRepository.insert(buildRoom(floorId, name1, RoomStatus.AVAILABLE));
		roomRepository.insert(buildRoom(floorId, name2, RoomStatus.RESERVED));

		List<Room> rooms = roomRepository.findAllRoomsByFloorId(floorId);

		assertThat(rooms).hasSize(2);
		assertThat(rooms.stream().allMatch(r -> r.getFloorId().equals(floorId))).isTrue();
		assertThat(rooms.stream().map(Room::getName)).containsExactlyInAnyOrder(name1, name2);
	}

	@Test
	@DisplayName("insert creates new room and can be read back")
	void insert_createAndReadBack() {
		Long floorId = createFloor();
		String name = shortRoomName();
		roomRepository.insert(buildRoom(floorId, name, RoomStatus.RESERVED));

		List<Room> list = roomRepository.findAllRoomsByFloorId(floorId);
		assertThat(list).hasSize(1);
		Room got = list.get(0);
		assertThat(got.getName()).isEqualTo(name);
		assertThat(got.getStatus()).isEqualTo(RoomStatus.RESERVED);
	}

	@Test
	@DisplayName("updateRoom updates name/userId/status")
	void updateRoom_modifyFields() {
		Long floorId = createFloor();
		String name = shortRoomName();
		roomRepository.insert(buildRoom(floorId, "E501", RoomStatus.AVAILABLE));
		Room before = roomRepository.findAllRoomsByFloorId(floorId).get(0);

		before.setName(name);
		before.setStatus(RoomStatus.SOLD);

		roomRepository.updateRoom(before);

		Room after = roomRepository.findRoomByFloorIdAndRoomId(floorId, before.getId()).orElseThrow();
		assertThat(after.getName()).isEqualTo(name);
		assertThat(after.getStatus()).isEqualTo(RoomStatus.SOLD);
	}

	@Test
	@DisplayName("deleteByIdAndFloorId deletes correct room")
	void deleteByIdAndFloorId_delete() {
		Long floorId = createFloor();
		String name = shortRoomName();
		roomRepository.insert(buildRoom(floorId, name, RoomStatus.RESERVED));
		Long roomId = roomRepository.findAllRoomsByFloorId(floorId).get(0).getId();

		assertThat(roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)).isPresent();

		roomRepository.deleteByIdAndFloorId(roomId, floorId);

		assertThat(roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)).isEmpty();
	}
}
