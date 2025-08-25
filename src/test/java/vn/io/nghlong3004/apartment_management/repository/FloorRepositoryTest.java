package vn.io.nghlong3004.apartment_management.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import vn.io.nghlong3004.apartment_management.model.Floor;
import vn.io.nghlong3004.apartment_management.model.Role;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserStatus;
import vn.io.nghlong3004.apartment_management.util.HelperUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FloorRepositoryTest {

    private static final AtomicLong COUNTER = new AtomicLong(1);
    @Autowired
    FloorRepository floorRepository;
    @Autowired
    UserRepository userRepository;

    private String nextFloorName() {
        return HelperUtil.generateFloorName(COUNTER.getAndIncrement()); // "Floor n"
    }

    private String uniqueEmail() {
        return "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "@example.com";
    }

    /**
     * Tạo một user hợp lệ trong floor_user và trả về id để dùng làm manager_id
     */
    private Long createManagerAndGetId() {
        User u = User.builder()
                .firstName("Test")
                .lastName("Manager")
                .email(uniqueEmail())
                .password("secret")
                .phoneNumber("0123456789")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(u);
        return userRepository.findByEmail(u.getEmail()).orElseThrow().getId();
    }

    private Floor insertFloor(String name, Long managerId, int roomCount) {
        Floor f = Floor.builder().name(name).managerId(managerId).roomCount(roomCount).build();
        floorRepository.insert(f);
        return floorRepository.findByName(name).orElseThrow();
    }

    @Test
    @DisplayName("insert + findByName: persists and retrieves (case-insensitive)")
    void insert_and_findByName() {
        Floor saved = insertFloor(nextFloorName(), null, 5);

        Optional<Floor> lower = floorRepository.findByName(saved.getName().toLowerCase());
        Optional<Floor> upper = floorRepository.findByName(saved.getName().toUpperCase());

        assertThat(saved.getId()).isNotNull();
        assertThat(lower).isPresent();
        assertThat(upper).isPresent();
        assertThat(lower.get().getName()).isEqualTo(saved.getName());
        assertThat(lower.get().getRoomCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("findById: returns row after insert")
    void findById_returns() {
        Floor saved = insertFloor(nextFloorName(), null, 3);

        Optional<Floor> got = floorRepository.findById(saved.getId());
        assertThat(got).isPresent();
        assertThat(got.get().getName()).isEqualTo(saved.getName());
        assertThat(got.get().getRoomCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("updateFloor: updates name/manager/roomCount")
    void updateFloor_updates() {
        Floor saved = insertFloor(nextFloorName(), null, 1);
        Long managerId = createManagerAndGetId();

        String newName = nextFloorName();
        Floor toUpdate = Floor.builder()
                .id(saved.getId())
                .name(newName)
                .managerId(managerId)
                .roomCount(9)
                .build();
        floorRepository.updateFloor(toUpdate);

        Floor after = floorRepository.findById(saved.getId()).orElseThrow();
        assertThat(after.getName()).isEqualTo(newName);
        assertThat(after.getManagerId()).isEqualTo(managerId);
        assertThat(after.getRoomCount()).isEqualTo(9);
    }

    @Test
    @DisplayName("deleteById: removes row")
    void deleteById_removes() {
        Floor saved = insertFloor(nextFloorName(), null, 0);

        assertThat(floorRepository.findById(saved.getId())).isPresent();
        floorRepository.deleteById(saved.getId());
        assertThat(floorRepository.findById(saved.getId())).isNotPresent();
    }

    @Test
    @DisplayName("floorExists: true/false")
    void floorExists_true_false() {
        Floor saved = insertFloor(nextFloorName(), null, 0);

        assertThat(floorRepository.floorExists(saved.getId())).contains(true);
    }

    @Test
    @DisplayName("countAll: increases by N after N inserts")
    void countAll_increases() {
        long before = floorRepository.countAll();

        insertFloor(nextFloorName(), null, 1);
        insertFloor(nextFloorName(), null, 2);
        insertFloor(nextFloorName(), null, 0);

        long after = floorRepository.countAll();
        assertThat(after - before).isEqualTo(3L);
    }

    @Test
    @DisplayName("increment/decrement roomCount; never below zero")
    void inc_dec_roomCount() {
        Floor saved = insertFloor(nextFloorName(), null, 1);

        floorRepository.incrementRoomCount(saved.getId());
        assertThat(floorRepository.findById(saved.getId()).orElseThrow().getRoomCount()).isEqualTo(2);

        floorRepository.decrementRoomCount(saved.getId());
        floorRepository.decrementRoomCount(saved.getId());
        floorRepository.decrementRoomCount(saved.getId());

        assertThat(floorRepository.findById(saved.getId()).orElseThrow().getRoomCount()).isZero();
    }

    @Test
    @DisplayName("findPage: orders/limits/offsets by orderBy")
    void findPage_orders_limits_offsets() {
        Floor a = insertFloor(HelperUtil.generateFloorName(100), null, 0);
        Floor b = insertFloor(HelperUtil.generateFloorName(101), null, 0);
        Floor c = insertFloor(HelperUtil.generateFloorName(102), null, 0);
        Floor d = insertFloor(HelperUtil.generateFloorName(103), null, 0);

        List<Floor> page = floorRepository.findPage("name ASC", 2, 1);
        assertThat(page).hasSize(2);
        assertThat(page.get(0).getName()).isLessThanOrEqualTo(page.get(1).getName());
        assertThat(List.of(a.getName(), b.getName(), c.getName(), d.getName()))
                .contains(page.get(0).getName(), page.get(1).getName());
    }

    @Test
    @DisplayName("updateManager + managerIdExists: requires FK to existing floor_user")
    void updateManager_and_managerIdExists() {
        Floor saved = insertFloor(nextFloorName(), null, 0);
        Long managerId = createManagerAndGetId();

        floorRepository.updateManager(saved.getId(), managerId);
        Floor after = floorRepository.findById(saved.getId()).orElseThrow();
        assertThat(after.getManagerId()).isEqualTo(managerId);

        assertThat(floorRepository.managerIdExists(managerId)).contains(true);
    }
}
