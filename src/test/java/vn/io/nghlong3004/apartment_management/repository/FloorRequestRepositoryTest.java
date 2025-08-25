package vn.io.nghlong3004.apartment_management.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import vn.io.nghlong3004.apartment_management.model.*;
import vn.io.nghlong3004.apartment_management.util.HelperUtil;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FloorRequestRepositoryTest {

    @Autowired
    FloorRequestRepository floorRequestRepository;
    @Autowired
    FloorRepository floorRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    UserRepository userRepository;


    private String uniqueEmail() {
        return "u_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10) + "@example.com";
    }

    private Long insertUser(Role role, UserStatus status) {
        User u = User.builder()
                .firstName("T")
                .lastName("U")
                .email(uniqueEmail())
                .password("pwd")
                .phoneNumber("0900000000")
                .role(role)
                .status(status)
                .build();
        userRepository.save(u);
        return userRepository.findByEmail(u.getEmail()).orElseThrow().getId();
    }

    private Long insertFloor() {
        String name = HelperUtil.generateFloorName(1);
        Floor f = Floor.builder().name(name).roomCount(0).managerId(null).build();
        floorRepository.insert(f);
        return floorRepository.findByName(name).orElseThrow().getId();
    }

    private Long insertRoom(Long floorId, Long userId, String name, RoomStatus status) {
        Room r = Room.builder()
                .floorId(floorId)
                .userId(userId)
                .name(name)
                .status(status)
                .build();
        roomRepository.insert(r);
        return roomRepository.findByFloorIdAndName(floorId, name).orElseThrow().getId();
    }

    private FloorRequest lastInserted() {
        List<FloorRequest> page = floorRequestRepository.findPage("id DESC", 1, 0);
        return page.isEmpty() ? null : page.get(0);
    }


    @Test
    @DisplayName("insert + findById: persists and retrieves a floor_request row")
    void insert_and_findById() {
        Long requesterId = insertUser(Role.USER, UserStatus.ACTIVE);
        Long counterpartId = insertUser(Role.USER, UserStatus.ACTIVE);
        Long floorId = insertFloor();

        Long requesterRoomId = insertRoom(floorId, requesterId, HelperUtil.generateRoomName(1, 1), RoomStatus.SOLD);
        Long counterpartRoomId = insertRoom(floorId, counterpartId, HelperUtil.generateRoomName(1, 2), RoomStatus.SOLD);

        FloorRequest fr = FloorRequest.builder()
                .requesterId(requesterId)
                .requesterRoomId(requesterRoomId)
                .counterpartId(counterpartId)
                .counterpartRoomId(counterpartRoomId)
                .approverId(null)
                .status(RequestStatus.PENDING)
                .closedReason(null)
                .build();

        floorRequestRepository.insert(fr);

        FloorRequest got = lastInserted();
        assertThat(got).isNotNull();
        FloorRequest byId = floorRequestRepository.findById(got.getId()).orElseThrow();

        assertThat(byId.getRequesterId()).isEqualTo(requesterId);
        assertThat(byId.getCounterpartId()).isEqualTo(counterpartId);
        assertThat(byId.getRequesterRoomId()).isEqualTo(requesterRoomId);
        assertThat(byId.getCounterpartRoomId()).isEqualTo(counterpartRoomId);
        assertThat(byId.getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    @DisplayName("update: updates fields (status, approverId, closedReason)")
    void update_updates_fields() {
        Long requesterId = insertUser(Role.USER, UserStatus.ACTIVE);
        Long counterpartId = insertUser(Role.USER, UserStatus.ACTIVE);
        Long adminId = insertUser(Role.ADMIN, UserStatus.ACTIVE);
        Long floorId = insertFloor();
        Long reqRoomId = insertRoom(floorId, requesterId, HelperUtil.generateRoomName(1, 3), RoomStatus.SOLD);
        Long cpRoomId = insertRoom(floorId, counterpartId, HelperUtil.generateRoomName(1, 4), RoomStatus.SOLD);

        FloorRequest fr = FloorRequest.builder()
                .requesterId(requesterId)
                .requesterRoomId(reqRoomId)
                .counterpartId(counterpartId)
                .counterpartRoomId(cpRoomId)
                .status(RequestStatus.PENDING)
                .build();
        floorRequestRepository.insert(fr);

        Long id = lastInserted().getId();
        FloorRequest toUpdate = floorRequestRepository.findById(id).orElseThrow();
        toUpdate.setStatus(RequestStatus.REJECTED);
        toUpdate.setApproverId(adminId);
        toUpdate.setClosedReason("Policy");

        floorRequestRepository.update(toUpdate);

        FloorRequest after = floorRequestRepository.findById(id).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(RequestStatus.REJECTED);
        assertThat(after.getApproverId()).isEqualTo(adminId);
        assertThat(after.getClosedReason()).isEqualTo("Policy");
    }

    @Test
    @DisplayName("existsActiveByRequesterId: true when any PENDING/ACCEPTED exists, false otherwise")
    void existsActiveByRequesterId_works() {
        Long requesterId = insertUser(Role.USER, UserStatus.ACTIVE);
        Long counterpartId = insertUser(Role.USER, UserStatus.ACTIVE);
        Long floorId = insertFloor();
        Long reqRoomId = insertRoom(floorId, requesterId, HelperUtil.generateRoomName(1, 5), RoomStatus.SOLD);
        Long cpRoomId = insertRoom(floorId, counterpartId, HelperUtil.generateRoomName(1, 6), RoomStatus.SOLD);

        // inactive row (DECLINED)
        floorRequestRepository.insert(FloorRequest.builder()
                .requesterId(requesterId).requesterRoomId(reqRoomId)
                .counterpartId(counterpartId).counterpartRoomId(cpRoomId)
                .status(RequestStatus.DECLINED).build());

        // active row (PENDING)
        floorRequestRepository.insert(FloorRequest.builder()
                .requesterId(requesterId).requesterRoomId(reqRoomId)
                .counterpartId(counterpartId).counterpartRoomId(cpRoomId)
                .status(RequestStatus.PENDING).build());

        boolean active = floorRequestRepository.existsActiveByRequesterId(requesterId);
        boolean activeOther = floorRequestRepository.existsActiveByRequesterId(counterpartId);

        assertThat(active).isTrue();
        assertThat(activeOther).isFalse();
    }

    @Test
    @DisplayName("existsActiveByCounterpartId: true when any PENDING/ACCEPTED exists, false otherwise")
    void existsActiveByCounterpartId_works() {
        Long requesterId = insertUser(Role.USER, UserStatus.ACTIVE);
        Long counterpartId = insertUser(Role.USER, UserStatus.ACTIVE);
        Long floorId = insertFloor();
        Long reqRoomId = insertRoom(floorId, requesterId, HelperUtil.generateRoomName(1, 7), RoomStatus.SOLD);
        Long cpRoomId = insertRoom(floorId, counterpartId, HelperUtil.generateRoomName(1, 8), RoomStatus.SOLD);

        // inactive row (CANCELLED)
        floorRequestRepository.insert(FloorRequest.builder()
                .requesterId(requesterId).requesterRoomId(reqRoomId)
                .counterpartId(counterpartId).counterpartRoomId(cpRoomId)
                .status(RequestStatus.CANCELLED).build());

        // active row (ACCEPTED)
        floorRequestRepository.insert(FloorRequest.builder()
                .requesterId(requesterId).requesterRoomId(reqRoomId)
                .counterpartId(counterpartId).counterpartRoomId(cpRoomId)
                .status(RequestStatus.ACCEPTED).build());

        boolean active = floorRequestRepository.existsActiveByCounterpartId(counterpartId);
        boolean activeOther = floorRequestRepository.existsActiveByCounterpartId(999999L);

        assertThat(active).isTrue();
        assertThat(activeOther).isFalse();
    }

    @Test
    @DisplayName("find: returns all rows")
    void find_returns_all() {
        Long r1 = insertUser(Role.USER, UserStatus.ACTIVE);
        Long r2 = insertUser(Role.USER, UserStatus.ACTIVE);
        Long floorId = insertFloor();
        Long rr1 = insertRoom(floorId, r1, HelperUtil.generateRoomName(1, 9), RoomStatus.SOLD);
        Long rr2 = insertRoom(floorId, r2, HelperUtil.generateRoomName(1, 10), RoomStatus.SOLD);

        floorRequestRepository.insert(FloorRequest.builder()
                .requesterId(r1).requesterRoomId(rr1).counterpartId(r2).counterpartRoomId(rr2)
                .status(RequestStatus.PENDING).build());
        floorRequestRepository.insert(FloorRequest.builder()
                .requesterId(r2).requesterRoomId(rr2).counterpartId(r1).counterpartRoomId(rr1)
                .status(RequestStatus.PENDING).build());

        List<FloorRequest> all = floorRequestRepository.find();
        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("findPage: orders, limits, offsets by orderBy")
    void findPage_orders_limits_offsets() {
        Long u1 = insertUser(Role.USER, UserStatus.ACTIVE);
        Long u2 = insertUser(Role.USER, UserStatus.ACTIVE);
        Long floorId = insertFloor();
        Long r1 = insertRoom(floorId, u1, HelperUtil.generateRoomName(1, 11), RoomStatus.SOLD);
        Long r2 = insertRoom(floorId, u2, HelperUtil.generateRoomName(1, 12), RoomStatus.SOLD);

        // create 3 requests
        floorRequestRepository.insert(FloorRequest.builder()
                .requesterId(u1).requesterRoomId(r1).counterpartId(u2).counterpartRoomId(r2)
                .status(RequestStatus.PENDING).build());
        floorRequestRepository.insert(FloorRequest.builder()
                .requesterId(u1).requesterRoomId(r1).counterpartId(u2).counterpartRoomId(r2)
                .status(RequestStatus.ACCEPTED).build());
        floorRequestRepository.insert(FloorRequest.builder()
                .requesterId(u2).requesterRoomId(r2).counterpartId(u1).counterpartRoomId(r1)
                .status(RequestStatus.CANCELLED).build());

        List<FloorRequest> page = floorRequestRepository.findPage("id ASC", 2, 1);
        assertThat(page).hasSize(2);
        assertThat(page.get(0).getId()).isLessThan(page.get(1).getId());
    }

    @Test
    @DisplayName("countAll: increases after inserts")
    void countAll_increases() {
        long before = floorRequestRepository.countAll();

        Long u1 = insertUser(Role.USER, UserStatus.ACTIVE);
        Long u2 = insertUser(Role.USER, UserStatus.ACTIVE);
        Long floorId = insertFloor();
        Long r1 = insertRoom(floorId, u1, HelperUtil.generateRoomName(1, 13), RoomStatus.SOLD);
        Long r2 = insertRoom(floorId, u2, HelperUtil.generateRoomName(1, 14), RoomStatus.SOLD);

        floorRequestRepository.insert(FloorRequest.builder()
                .requesterId(u1).requesterRoomId(r1).counterpartId(u2).counterpartRoomId(r2)
                .status(RequestStatus.PENDING).build());
        floorRequestRepository.insert(FloorRequest.builder()
                .requesterId(u2).requesterRoomId(r2).counterpartId(u1).counterpartRoomId(r1)
                .status(RequestStatus.PENDING).build());

        long after = floorRequestRepository.countAll();
        assertThat(after - before).isEqualTo(2L);
    }
}
