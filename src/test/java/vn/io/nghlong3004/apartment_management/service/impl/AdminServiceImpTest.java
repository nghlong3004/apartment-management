package vn.io.nghlong3004.apartment_management.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.*;
import vn.io.nghlong3004.apartment_management.model.dto.FloorManagerRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomOwnerRequest;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.repository.RoomRepository;
import vn.io.nghlong3004.apartment_management.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    FloorRepository floorRepository;
    @Mock
    RoomRepository roomRepository;

    @InjectMocks
    AdminServiceImpl service;

    @Captor
    ArgumentCaptor<User> userCaptor;
    @Captor
    ArgumentCaptor<Room> roomCaptor;
    @Captor
    ArgumentCaptor<Long> longCaptor1;
    @Captor
    ArgumentCaptor<Long> longCaptor2;

    // -------------------- setManager --------------------

    @Test
    @DisplayName("setManager: assigns manager role and updates floor when user is ACTIVE & floor has no manager")
    void setManager_success() {
        Long floorId = 10L;
        Long userId = 101L;

        FloorManagerRequest req = mock(FloorManagerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.ACTIVE).role(Role.USER).build();
        Floor floor = Floor.builder().id(floorId).managerId(null).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(floorRepository.findById(floorId)).thenReturn(Optional.of(floor));

        service.setManager(floorId, req);

        // user role -> MANAGER
        verify(userRepository).update(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.MANAGER);

        // floor manager -> userId
        verify(floorRepository).updateManager(longCaptor1.capture(), longCaptor2.capture());
        assertThat(longCaptor1.getValue()).isEqualTo(floorId);
        assertThat(longCaptor2.getValue()).isEqualTo(userId);
    }

    @Test
    @DisplayName("setManager: throws ALREADY_A_MANAGER when user role is already MANAGER")
    void setManager_userAlreadyManager() {
        Long floorId = 11L;
        Long userId = 102L;

        FloorManagerRequest req = mock(FloorManagerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.ACTIVE).role(Role.MANAGER).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.setManager(floorId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.ALREADY_A_MANAGER);

        verify(floorRepository, never()).updateManager(any(), any());
        verify(userRepository, never()).update(any());
    }

    @Test
    @DisplayName("setManager: throws FLOOR_NOT_FOUND when floor does not exist")
    void setManager_floorNotFound() {
        Long floorId = 12L;
        Long userId = 103L;

        FloorManagerRequest req = mock(FloorManagerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.ACTIVE).role(Role.USER).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(floorRepository.findById(floorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setManager(floorId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.FLOOR_NOT_FOUND);
    }

    @Test
    @DisplayName("setManager: throws MANAGER_FLOOR when floor already has a manager")
    void setManager_floorAlreadyHasManager() {
        Long floorId = 13L;
        Long userId = 104L;

        FloorManagerRequest req = mock(FloorManagerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.ACTIVE).role(Role.USER).build();
        Floor floor = Floor.builder().id(floorId).managerId(999L).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(floorRepository.findById(floorId)).thenReturn(Optional.of(floor));

        assertThatThrownBy(() -> service.setManager(floorId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.MANAGER_FLOOR);
    }

    @Test
    @DisplayName("setManager: throws USER_BANNED when user status is INACTIVE")
    void setManager_userInactive() {
        Long floorId = 14L;
        Long userId = 105L;

        FloorManagerRequest req = mock(FloorManagerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.INACTIVE).role(Role.USER).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.setManager(floorId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.USER_BANNED);
    }

    @Test
    @DisplayName("setManager: throws ID_NOT_FOUND when user not found")
    void setManager_userNotFound() {
        Long floorId = 15L;
        Long userId = 106L;

        FloorManagerRequest req = mock(FloorManagerRequest.class);
        when(req.userId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setManager(floorId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.ID_NOT_FOUND);
    }

    // -------------------- deleteManager --------------------

    @Test
    @DisplayName("deleteManager: demotes manager to USER and clears floor.managerId")
    void deleteManager_success() {
        Long floorId = 20L;
        Long userId = 201L;

        FloorManagerRequest req = mock(FloorManagerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.ACTIVE).role(Role.MANAGER).build();
        Floor floor = Floor.builder().id(floorId).managerId(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(floorRepository.findById(floorId)).thenReturn(Optional.of(floor));

        service.deleteManager(floorId, req);

        verify(userRepository).update(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.USER);

        verify(floorRepository).updateManager(longCaptor1.capture(), isNull());
        assertThat(longCaptor1.getValue()).isEqualTo(floorId);
    }

    @Test
    @DisplayName("deleteManager: throws NOT_MANAGER when user is not a manager")
    void deleteManager_userNotManager() {
        Long floorId = 21L;
        Long userId = 202L;

        FloorManagerRequest req = mock(FloorManagerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.ACTIVE).role(Role.USER).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.deleteManager(floorId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.NOT_MANAGER);
    }

    @Test
    @DisplayName("deleteManager: throws FLOOR_NOT_FOUND when floor does not exist")
    void deleteManager_floorNotFound() {
        Long floorId = 22L;
        Long userId = 203L;

        FloorManagerRequest req = mock(FloorManagerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.ACTIVE).role(Role.MANAGER).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(floorRepository.findById(floorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteManager(floorId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.FLOOR_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteManager: throws NOT_MANAGER_FLOOR when floor.managerId != user.id")
    void deleteManager_wrongManagerOfFloor() {
        Long floorId = 23L;
        Long userId = 204L;

        FloorManagerRequest req = mock(FloorManagerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.ACTIVE).role(Role.MANAGER).build();
        Floor floor = Floor.builder().id(floorId).managerId(999L).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(floorRepository.findById(floorId)).thenReturn(Optional.of(floor));

        assertThatThrownBy(() -> service.deleteManager(floorId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.NOT_MANAGER_FLOOR);
    }

    // -------------------- setOwner --------------------

    @Test
    @DisplayName("setOwner: assigns user to AVAILABLE room, clears old room if exists, updates user's floorId")
    void setOwner_success_withOldRoomCleared() {
        Long roomId = 30L;
        Long userId = 301L;

        RoomOwnerRequest req = mock(RoomOwnerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.ACTIVE).role(Role.USER).floorId(null).build();

        Room target = Room.builder()
                .id(roomId).floorId(500L).userId(null).status(RoomStatus.AVAILABLE).build();

        Room old = Room.builder()
                .id(777L).floorId(400L).userId(userId).status(RoomStatus.SOLD).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(target));
        when(roomRepository.findByUserId(userId)).thenReturn(old);

        service.setOwner(roomId, req);

        // old room cleared
        verify(roomRepository, times(2)).updateRoom(roomCaptor.capture());
        Room cleared = roomCaptor.getAllValues().get(0);
        Room updatedTarget = roomCaptor.getAllValues().get(1);

        assertThat(cleared.getId()).isEqualTo(old.getId());
        assertThat(cleared.getUserId()).isNull();
        assertThat(cleared.getStatus()).isEqualTo(RoomStatus.AVAILABLE);

        // target room assigned to user
        assertThat(updatedTarget.getId()).isEqualTo(roomId);
        assertThat(updatedTarget.getUserId()).isEqualTo(userId);
        assertThat(updatedTarget.getStatus()).isEqualTo(RoomStatus.SOLD);

        // user floorId updated to target.floorId
        verify(userRepository).update(userCaptor.capture());
        assertThat(userCaptor.getValue().getFloorId()).isEqualTo(500L);
    }

    @Test
    @DisplayName("setOwner: throws OCCUPIED_ROOM when target room has user or not AVAILABLE")
    void setOwner_roomOccupied() {
        Long roomId = 31L;
        Long userId = 302L;

        RoomOwnerRequest req = mock(RoomOwnerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.ACTIVE).role(Role.USER).build();
        // occupied by userId 999 OR status SOLD -> bất kỳ điều kiện nào cũng fail
        Room occupied = Room.builder().id(roomId).floorId(1L).userId(999L).status(RoomStatus.SOLD).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(occupied));

        assertThatThrownBy(() -> service.setOwner(roomId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.OCCUPIED_ROOM);

        verify(roomRepository, never()).updateRoom(any());
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("setOwner: throws ROOM_NOT_FOUND when room doesn't exist")
    void setOwner_roomNotFound() {
        Long roomId = 32L;
        Long userId = 303L;

        RoomOwnerRequest req = mock(RoomOwnerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.ACTIVE).role(Role.USER).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setOwner(roomId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("setOwner: throws USER_BANNED when user is INACTIVE")
    void setOwner_userInactive() {
        Long roomId = 33L;
        Long userId = 304L;

        RoomOwnerRequest req = mock(RoomOwnerRequest.class);
        when(req.userId()).thenReturn(userId);

        User user = User.builder().id(userId).status(UserStatus.INACTIVE).role(Role.USER).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.setOwner(roomId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.USER_BANNED);
    }

    @Test
    @DisplayName("setOwner: throws ID_NOT_FOUND when user not found")
    void setOwner_userNotFound() {
        Long roomId = 34L;
        Long userId = 305L;

        RoomOwnerRequest req = mock(RoomOwnerRequest.class);
        when(req.userId()).thenReturn(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setOwner(roomId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.ID_NOT_FOUND);
    }

    // -------------------- deleteOwner --------------------

    @Test
    @DisplayName("deleteOwner: clears userId and sets AVAILABLE when requester is the current owner")
    void deleteOwner_success() {
        Long roomId = 40L;
        Long userId = 401L;

        RoomOwnerRequest req = mock(RoomOwnerRequest.class);
        when(req.userId()).thenReturn(userId);

        Room room = Room.builder().id(roomId).floorId(1L).userId(userId).status(RoomStatus.SOLD).build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        service.deleteOwner(roomId, req);

        verify(roomRepository).updateRoom(roomCaptor.capture());
        Room updated = roomCaptor.getValue();
        assertThat(updated.getUserId()).isNull();
        assertThat(updated.getStatus()).isEqualTo(RoomStatus.AVAILABLE);
    }

    @Test
    @DisplayName("deleteOwner: throws NOT_OWNER_ROOM when requester is not the room owner")
    void deleteOwner_notOwner() {
        Long roomId = 41L;
        Long userId = 402L;

        RoomOwnerRequest req = mock(RoomOwnerRequest.class);
        when(req.userId()).thenReturn(userId);

        Room room = Room.builder().id(roomId).floorId(1L).userId(999L).status(RoomStatus.SOLD).build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> service.deleteOwner(roomId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.NOT_OWNER_ROOM);

        verify(roomRepository, never()).updateRoom(any());
    }

    @Test
    @DisplayName("deleteOwner: throws ROOM_NOT_FOUND when room doesn't exist")
    void deleteOwner_roomNotFound() {
        Long roomId = 42L;
        Long userId = 403L;

        RoomOwnerRequest req = mock(RoomOwnerRequest.class);
        when(req.userId()).thenReturn(userId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteOwner(roomId, req))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.ROOM_NOT_FOUND);
    }
}
