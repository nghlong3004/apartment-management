package vn.io.nghlong3004.apartment_management.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.Floor;
import vn.io.nghlong3004.apartment_management.model.Room;
import vn.io.nghlong3004.apartment_management.model.RoomStatus;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.RoomRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomResponse;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.repository.RoomRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    RoomRepository roomRepository;
    @Mock
    FloorRepository floorRepository;

    @InjectMocks
    RoomServiceImpl service;

    @Captor
    ArgumentCaptor<Room> roomCaptor;
    @Captor
    ArgumentCaptor<Long> longCaptor1;
    @Captor
    ArgumentCaptor<Long> longCaptor2;
    @Captor
    ArgumentCaptor<String> stringCaptor;
    @Captor
    ArgumentCaptor<Integer> intCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "maxRoomNumber", 100L);
    }

    @Test
    @DisplayName("getRoom: returns room when found by floorId & roomId")
    void getRoom_found() {
        Long floorId = 10L, roomId = 100L;
        Room room = Room.builder().id(roomId).floorId(floorId).name("Room 1-01").status(RoomStatus.AVAILABLE).build();

        when(roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)).thenReturn(Optional.of(room));

        Room got = service.getRoom(floorId, roomId);

        assertThat(got).isSameAs(room);
    }

    @Test
    @DisplayName("getRoom: throws NOT_FOUND when room does not exist")
    void getRoom_notFound() {
        Long floorId = 10L, roomId = 999L;
        when(roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRoom(floorId, roomId))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("getRoomResponse: maps Room to RoomResponse")
    void getRoomResponse_maps() {
        Long floorId = 10L, roomId = 101L;
        Room room = Room.builder().id(roomId).floorId(floorId).name("Room 1-02").status(RoomStatus.RESERVED).build();
        when(roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)).thenReturn(Optional.of(room));

        RoomResponse resp = service.getRoomResponse(floorId, roomId);

        assertThat(resp.getId()).isEqualTo(roomId);
        assertThat(resp.getFloorId()).isEqualTo(floorId);
        assertThat(resp.getName()).isEqualTo("Room 1-02");
        assertThat(resp.getStatus()).isEqualTo(RoomStatus.RESERVED);
    }

    @Test
    @DisplayName("createRoom: inserts AVAILABLE room with generated name and increments floor roomCount")
    void createRoom_success() {
        Long floorId = 20L;
        Floor floor = Floor.builder().id(floorId).name("Floor 3").roomCount(0).build();
        when(floorRepository.findById(floorId)).thenReturn(Optional.of(floor));

        service.createRoom(floorId);

        verify(roomRepository).insert(roomCaptor.capture());
        Room inserted = roomCaptor.getValue();
        assertThat(inserted.getFloorId()).isEqualTo(floorId);
        assertThat(inserted.getStatus()).isEqualTo(RoomStatus.AVAILABLE);
        assertThat(inserted.getName()).isEqualTo("Room 3-01");

        verify(floorRepository).incrementRoomCount(floorId);
    }

    @Test
    @DisplayName("createRoom: throws FLOOR_NOT_FOUND when floor does not exist")
    void createRoom_floorNotFound() {
        Long floorId = 21L;
        when(floorRepository.findById(floorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createRoom(floorId))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.FLOOR_NOT_FOUND);

        verify(roomRepository, never()).insert(any());
        verify(floorRepository, never()).incrementRoomCount(any());
    }

    @Test
    @DisplayName("createRoom: throws INVALID_FLOOR_NUMBER when floor name has no digits")
    void createRoom_invalidFloorNumber() {
        Long floorId = 22L;
        Floor floor = Floor.builder().id(floorId).name("NoNumber").roomCount(0).build();
        when(floorRepository.findById(floorId)).thenReturn(Optional.of(floor));

        assertThatThrownBy(() -> service.createRoom(floorId))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.INVALID_FLOOR_NUMBER);

        verify(roomRepository, never()).insert(any());
        verify(floorRepository, never()).incrementRoomCount(any());
    }

    @Test
    @DisplayName("createRoom: throws ROOM_NUMBER_EXCEEDS_LIMIT when next roomNumber >= maxRoomNumber")
    void createRoom_exceedsLimit() {
        Long floorId = 23L;
        Floor floor = Floor.builder().id(floorId).name("Floor 1").roomCount(99).build();
        when(floorRepository.findById(floorId)).thenReturn(Optional.of(floor));

        assertThatThrownBy(() -> service.createRoom(floorId))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.ROOM_NUMBER_EXCEEDS_LIMIT);

        verify(roomRepository, never()).insert(any());
        verify(floorRepository, never()).incrementRoomCount(any());
    }

    @Test
    @DisplayName("updateRoom: updates status to RESERVED (from AVAILABLE) and persists")
    void updateRoom_success_changeToReserved() {
        Long floorId = 30L, roomId = 300L;
        Room existing =
                Room.builder().id(roomId).floorId(floorId).name("Room 2-01").status(RoomStatus.AVAILABLE).build();
        when(roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)).thenReturn(Optional.of(existing));

        RoomRequest req = new RoomRequest(RoomStatus.RESERVED);

        service.updateRoom(floorId, roomId, req);

        verify(roomRepository).updateRoom(roomCaptor.capture());
        Room updated = roomCaptor.getValue();
        assertThat(updated.getStatus()).isEqualTo(RoomStatus.RESERVED);
    }

    @Test
    @DisplayName("updateRoom: no status provided -> keeps current status")
    void updateRoom_noStatus_keepsCurrent() {
        Long floorId = 31L, roomId = 301L;
        Room existing = Room.builder().id(roomId).floorId(floorId).status(RoomStatus.AVAILABLE).build();
        when(roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)).thenReturn(Optional.of(existing));

        RoomRequest req = new RoomRequest(null); // resolveStatus -> AVAILABLE

        service.updateRoom(floorId, roomId, req);

        verify(roomRepository).updateRoom(roomCaptor.capture());
        assertThat(roomCaptor.getValue().getStatus()).isEqualTo(RoomStatus.AVAILABLE);
    }

    @Test
    @DisplayName("updateRoom: throws INVALID_ROOM_STATUS when resolved status is null")
    void updateRoom_invalidStatus_null() {
        Long floorId = 32L, roomId = 302L;
        Room existing = Room.builder().id(roomId).floorId(floorId).status(null).build();
        when(roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)).thenReturn(Optional.of(existing));

        RoomRequest req = new RoomRequest(null);

        assertThatThrownBy(() -> service.updateRoom(floorId, roomId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.INVALID_ROOM_STATUS);

        verify(roomRepository, never()).updateRoom(any());
    }

    @Test
    @DisplayName("updateRoom: throws INCONSISTENT_ROOM_STATE when status is SOLD")
    void updateRoom_inconsistent_whenSold() {
        Long floorId = 33L, roomId = 303L;
        Room existing = Room.builder().id(roomId).floorId(floorId).status(RoomStatus.AVAILABLE).build();
        when(roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)).thenReturn(Optional.of(existing));

        RoomRequest req = new RoomRequest(RoomStatus.SOLD);

        assertThatThrownBy(() -> service.updateRoom(floorId, roomId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.INCONSISTENT_ROOM_STATE);

        verify(roomRepository, never()).updateRoom(any());
    }

    @Test
    @DisplayName("updateRoom: throws ROOM_NOT_FOUND when room missing")
    void updateRoom_roomNotFound() {
        Long floorId = 34L, roomId = 304L;
        when(roomRepository.findRoomByFloorIdAndRoomId(floorId, roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRoom(floorId, roomId, new RoomRequest(RoomStatus.RESERVED)))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteRoom: deletes and decrements count when exists")
    void deleteRoom_success() {
        Long floorId = 40L, roomId = 400L;
        when(roomRepository.existsByFloorIdAndRoomId(floorId, roomId)).thenReturn(Optional.of(true));

        service.deleteRoom(floorId, roomId);

        verify(roomRepository).deleteByIdAndFloorId(roomId, floorId);
        verify(floorRepository).decrementRoomCount(floorId);
    }

    @Test
    @DisplayName("deleteRoom: throws ROOM_NOT_FOUND when exists=false")
    void deleteRoom_existsFalse() {
        Long floorId = 41L, roomId = 401L;
        when(roomRepository.existsByFloorIdAndRoomId(floorId, roomId)).thenReturn(Optional.of(false));

        assertThatThrownBy(() -> service.deleteRoom(floorId, roomId))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.ROOM_NOT_FOUND);

        verify(roomRepository, never()).deleteByIdAndFloorId(any(), any());
        verify(floorRepository, never()).decrementRoomCount(any());
    }

    @Test
    @DisplayName("deleteRoom: throws ROOM_NOT_FOUND when exists Optional is empty")
    void deleteRoom_existsEmpty() {
        Long floorId = 42L, roomId = 402L;
        when(roomRepository.existsByFloorIdAndRoomId(floorId, roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteRoom(floorId, roomId))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.ROOM_NOT_FOUND);

        verify(roomRepository, never()).deleteByIdAndFloorId(any(), any());
        verify(floorRepository, never()).decrementRoomCount(any());
    }

    @Test
    @DisplayName("getRooms (by name): trims name and returns single-element page")
    void getRooms_byName_success() {
        Long floorId = 50L;
        when(floorRepository.floorExists(floorId)).thenReturn(Optional.of(true));

        Room r = Room.builder().id(1L).floorId(floorId).name("R-01").status(RoomStatus.AVAILABLE).build();
        when(roomRepository.findByFloorIdAndName(floorId, "R-01")).thenReturn(Optional.of(r));

        PagedResponse<RoomResponse> resp = service.getRooms(floorId, "  R-01  ", 0, 20, "id,asc");

        assertThat(resp.getContent()).hasSize(1);
        assertThat(resp.getPage()).isEqualTo(0);
        assertThat(resp.getSize()).isEqualTo(1);
        assertThat(resp.getTotalElements()).isEqualTo(1);
        assertThat(resp.getTotalPages()).isEqualTo(1);
        assertThat(resp.getContent().get(0).getName()).isEqualTo("R-01");
    }

    @Test
    @DisplayName("getRooms (by name): throws ROOM_NOT_FOUND when not matched")
    void getRooms_byName_notFound() {
        Long floorId = 51L;
        when(floorRepository.floorExists(floorId)).thenReturn(Optional.of(true));
        when(roomRepository.findByFloorIdAndName(floorId, "X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRooms(floorId, "X", 0, 20, "id,asc"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("getRooms (list): normalizes paging/sort, maps to DTOs and computes totalPages")
    void getRooms_list_paged() {
        Long floorId = 52L;
        when(floorRepository.floorExists(floorId)).thenReturn(Optional.of(true));
        long total = 3L;
        when(roomRepository.countByFloorId(floorId)).thenReturn(total);

        Room r1 = Room.builder().id(1L).floorId(floorId).name("A").status(RoomStatus.AVAILABLE).build();
        Room r2 = Room.builder().id(2L).floorId(floorId).name("B").status(RoomStatus.RESERVED).build();
        when(roomRepository.findPageByFloorId(eq(floorId), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(r1, r2));
        PagedResponse<RoomResponse> resp = service.getRooms(floorId, null, -1, 2, "name,desc");

        assertThat(resp.getPage()).isEqualTo(0);
        assertThat(resp.getSize()).isEqualTo(2);
        assertThat(resp.getTotalElements()).isEqualTo(3L);
        assertThat(resp.getTotalPages()).isEqualTo(2); // ceil(3/2)=2
        assertThat(resp.getContent()).extracting(RoomResponse::getId).containsExactly(1L, 2L);
        
        verify(roomRepository).findPageByFloorId(longCaptor1.capture(), stringCaptor.capture(), intCaptor.capture(),
                intCaptor.capture());
        assertThat(longCaptor1.getValue()).isEqualTo(floorId);
        assertThat(stringCaptor.getValue()).isEqualTo("name DESC");
    }

    @Test
    @DisplayName("getRooms: throws FLOOR_NOT_FOUND when floor id does not exist")
    void getRooms_floorNotFound() {
        Long floorId = 53L;
        when(floorRepository.floorExists(floorId)).thenReturn(Optional.of(false));

        assertThatThrownBy(() -> service.getRooms(floorId, null, 0, 20, "id,asc"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.FLOOR_NOT_FOUND);
    }
}
