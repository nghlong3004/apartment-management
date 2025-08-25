package vn.io.nghlong3004.apartment_management.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.*;
import vn.io.nghlong3004.apartment_management.model.dto.CreateRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorRequestSummary;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.UpdateRequest;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.repository.FloorRequestRepository;
import vn.io.nghlong3004.apartment_management.repository.RoomRepository;
import vn.io.nghlong3004.apartment_management.repository.UserRepository;
import vn.io.nghlong3004.apartment_management.util.SecurityUtil;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FloorRequestServiceImplTest {

    @Mock
    FloorRequestRepository floorRequestRepository;
    @Mock
    FloorRepository floorRepository;
    @Mock
    RoomRepository roomRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    FloorRequestServiceImpl service;

    private CreateRequest cr(Long requesterId, Long requesterRoomId, Long counterpartId, Long counterpartRoomId) {
        return new CreateRequest(requesterId, requesterRoomId, counterpartId, counterpartRoomId);
    }

    private UpdateRequest ur(RequestStatus status, String reason) {
        return new UpdateRequest(status, reason);
    }

    private FloorRequest frPending(Long id, Long reqId, Long reqRoomId, Long cpId, Long cpRoomId) {
        return FloorRequest.builder()
                .id(id)
                .requesterId(reqId)
                .requesterRoomId(reqRoomId)
                .counterpartId(cpId)
                .counterpartRoomId(cpRoomId)
                .status(RequestStatus.PENDING)
                .build();
    }


    @Test
    @DisplayName("create: success when moving into an AVAILABLE empty room (counterpartId=null)")
    void create_success_availableEmptyRoom() {
        Long actorId = 11L;
        Long requesterId = actorId;
        Long requesterRoomId = 101L;
        Long counterpartId = null;
        Long counterpartRoomId = 202L;

        Room cpRoom = Room.builder()
                .id(counterpartRoomId)
                .floorId(1L)
                .userId(null)
                .status(RoomStatus.AVAILABLE)
                .build();

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(actorId));
            sec.when(() -> SecurityUtil.hasRole(anyString())).thenReturn(false);

            when(roomRepository.findById(counterpartRoomId)).thenReturn(Optional.of(cpRoom));

            when(roomRepository.findById(requesterId)).thenReturn(Optional.empty());

            when(floorRequestRepository.existsActiveByRequesterId(any())).thenReturn(false);
            when(floorRequestRepository.existsActiveByCounterpartId(any())).thenReturn(false);

            service.create(cr(requesterId, requesterRoomId, counterpartId, counterpartRoomId));

            verify(floorRequestRepository).insert(any(FloorRequest.class));
        }
    }
    

    @Test
    @DisplayName("create: room owner mismatch when counterpartId != room.userId")
    void create_counterpart_ownerMismatch() {
        Long actorId = 2L;
        Long counterpartId = 20L;
        Long counterpartRoomId = 200L;

        Room cpRoom = Room.builder()
                .id(counterpartRoomId)
                .floorId(1L)
                .userId(999L)
                .status(RoomStatus.SOLD)
                .build();

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(actorId));
            sec.when(() -> SecurityUtil.hasRole(anyString())).thenReturn(false);

            when(roomRepository.findById(counterpartRoomId)).thenReturn(Optional.of(cpRoom));
            when(roomRepository.findById(actorId)).thenReturn(
                    Optional.empty());

            assertThatThrownBy(() ->
                    service.create(cr(actorId, 101L, counterpartId, counterpartRoomId))
            ).isInstanceOf(ResourceException.class)
                    .hasMessageContaining(ErrorMessageConstant.ROOM_OWNER_MISMATCH);

            verify(floorRequestRepository, never()).insert(any());
        }
    }


    @Test
    @DisplayName("update: ACCEPTED by counterpart self")
    void update_accept_byCounterpart() {
        Long counterpartId = 44L;
        FloorRequest fr = frPending(1L, 33L, 333L, counterpartId, 444L);

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(counterpartId));
            sec.when(() -> SecurityUtil.hasRole(anyString())).thenReturn(false);
            when(floorRequestRepository.findById(1L)).thenReturn(Optional.of(fr));

            service.update(1L, ur(RequestStatus.ACCEPTED, null));

            assertThat(fr.getStatus()).isEqualTo(RequestStatus.ACCEPTED);
            assertThat(fr.getApproverId()).isNull();
            verify(floorRequestRepository).update(fr);
        }
    }

    @Test
    @DisplayName("update: DECLINED by counterpart self with reason")
    void update_decline_byCounterpart() {
        Long counterpartId = 55L;
        FloorRequest fr = frPending(2L, 66L, 666L, counterpartId, 777L);

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(counterpartId));
            sec.when(() -> SecurityUtil.hasRole(anyString())).thenReturn(false);
            when(floorRequestRepository.findById(2L)).thenReturn(Optional.of(fr));

            service.update(2L, ur(RequestStatus.DECLINED, "No thanks"));

            assertThat(fr.getStatus()).isEqualTo(RequestStatus.DECLINED);
            assertThat(fr.getClosedReason()).isEqualTo("No thanks");
            verify(floorRequestRepository).update(fr);
        }
    }

    @Test
    @DisplayName("update: CANCELLED by requester self")
    void update_cancel_byRequester() {
        Long requesterId = 77L;
        FloorRequest fr = frPending(3L, requesterId, 700L, 88L, 800L);

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(requesterId));
            sec.when(() -> SecurityUtil.hasRole(anyString())).thenReturn(false);
            when(floorRequestRepository.findById(3L)).thenReturn(Optional.of(fr));

            service.update(3L, ur(RequestStatus.CANCELLED, null));

            assertThat(fr.getStatus()).isEqualTo(RequestStatus.CANCELLED);
            verify(floorRequestRepository).update(fr);
        }
    }

    @Test
    @DisplayName("update: REJECTED by manager on same floor")
    void update_reject_byManager() {
        Long actorId = 200L;
        FloorRequest fr = frPending(4L, 10L, 101L, 20L, 202L);

        Room cpRoom = Room.builder().id(fr.getCounterpartRoomId()).floorId(999L).build();
        Floor floor = Floor.builder().id(999L).managerId(actorId).build();

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(actorId));
            sec.when(() -> SecurityUtil.hasRole("MANAGER")).thenReturn(true);
            sec.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            when(floorRequestRepository.findById(4L)).thenReturn(Optional.of(fr));
            when(roomRepository.findById(fr.getCounterpartRoomId())).thenReturn(Optional.of(cpRoom));
            when(floorRepository.findById(cpRoom.getFloorId())).thenReturn(Optional.of(floor));

            service.update(4L, ur(RequestStatus.REJECTED, "Policy"));

            assertThat(fr.getStatus()).isEqualTo(RequestStatus.REJECTED);
            assertThat(fr.getApproverId()).isEqualTo(actorId);
            assertThat(fr.getClosedReason()).isEqualTo("Policy");
            verify(floorRequestRepository).update(fr);
        }
    }

    @Test
    @DisplayName("update: APPROVED performs domain actions and sets APPROVED")
    void update_approve_domainActions() {
        Long actorId = 300L;
        Long reqId = 1000L;
        Long cpId = 2000L;
        Long reqRoomId = 111L;
        Long cpRoomId = 222L;

        FloorRequest fr = frPending(5L, reqId, reqRoomId, cpId, cpRoomId);
        fr.setStatus(RequestStatus.ACCEPTED);

        Room r1 = Room.builder().id(reqRoomId).floorId(1L).userId(reqId).status(RoomStatus.SOLD).build();
        Room r2 = Room.builder().id(cpRoomId).floorId(2L).userId(cpId).status(RoomStatus.SOLD).build();

        User u1 = User.builder().id(cpId).build();
        User u2 = User.builder().id(reqId).build();

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(actorId));
            sec.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(true);
            sec.when(() -> SecurityUtil.hasRole("MANAGER")).thenReturn(false);

            when(floorRequestRepository.findById(5L)).thenReturn(Optional.of(fr));
            when(roomRepository.findById(reqRoomId)).thenReturn(Optional.of(r1));
            when(roomRepository.findById(cpRoomId)).thenReturn(Optional.of(r2));
            when(userRepository.findById(cpId)).thenReturn(Optional.of(u1));
            when(userRepository.findById(reqId)).thenReturn(Optional.of(u2));

            service.update(5L, ur(RequestStatus.APPROVED, null));

            assertThat(fr.getStatus()).isEqualTo(RequestStatus.APPROVED);
            assertThat(fr.getApproverId()).isEqualTo(actorId);

            verify(roomRepository, times(2)).updateRoom(any(Room.class));
            verify(userRepository, times(2)).update(any(User.class));
            verify(floorRequestRepository).update(fr);
        }
    }

    @Test
    @DisplayName("update: REQUEST_ALREADY_IN_STATUS when new status == current status")
    void update_alreadyInStatus() {
        FloorRequest fr = FloorRequest.builder()
                .id(9L).status(RequestStatus.PENDING)
                .requesterId(1L).requesterRoomId(10L).counterpartId(2L).counterpartRoomId(20L)
                .build();

        when(floorRequestRepository.findById(9L)).thenReturn(Optional.of(fr));

        assertThatThrownBy(() ->
                service.update(9L, ur(RequestStatus.PENDING, null))
        ).isInstanceOf(ResourceException.class)
                .hasMessageContaining(ErrorMessageConstant.REQUEST_ALREADY_IN_STATUS);

        verify(floorRequestRepository, never()).update(any());
    }

    @Test
    @DisplayName("update: ACCEPTED forbidden when actor is neither admin nor counterpart self")
    void update_accept_forbidden() {
        FloorRequest fr = frPending(10L, 1L, 10L, 2L, 20L);

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(999L));
            sec.when(() -> SecurityUtil.hasRole(anyString())).thenReturn(false);
            when(floorRequestRepository.findById(10L)).thenReturn(Optional.of(fr));

            assertThatThrownBy(() ->
                    service.update(10L, ur(RequestStatus.ACCEPTED, null))
            ).isInstanceOf(ResourceException.class)
                    .hasMessageContaining(ErrorMessageConstant.ACTION_FORBIDDEN);
        }
    }

    @Test
    @DisplayName("update: APPROVED invalid state when current != ACCEPTED and counterpartId != null")
    void update_approve_invalidState() {
        FloorRequest fr = frPending(11L, 1L, 10L, 2L, 20L); // PENDING, có counterpartId

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(1L));
            sec.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(true);
            when(floorRequestRepository.findById(11L)).thenReturn(Optional.of(fr));

            assertThatThrownBy(() ->
                    service.update(11L, ur(RequestStatus.APPROVED, null))
            ).isInstanceOf(ResourceException.class)
                    .hasMessageContaining(ErrorMessageConstant.INVALID_STATE);
        }
    }


    @Test
    @DisplayName("getRequests: returns paged data and delegates to repository")
    void getRequests_paging() {
        FloorRequest r = FloorRequest.builder().id(1L).build();
        when(floorRequestRepository.countAll()).thenReturn(3L);
        when(floorRequestRepository.findPage(eq("id DESC"), eq(2), eq(2)))
                .thenReturn(List.of(r));

        PagedResponse<FloorRequestSummary> res = service.getRequests(1, 2, "id,desc");

        verify(floorRequestRepository).countAll();
        verify(floorRequestRepository).findPage("id DESC", 2, 2);
        assertThat(res.getPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(2);
        assertThat(res.getTotalElements()).isEqualTo(3);
        assertThat(res.getTotalPages()).isEqualTo(2);
        assertThat(res.getContent()).hasSize(1);
    }
}
