package vn.io.nghlong3004.apartment_management.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.nghlong3004.apartment_management.model.dto.FloorManagerRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomOwnerRequest;
import vn.io.nghlong3004.apartment_management.service.AdminService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController controller;

    @Captor
    private ArgumentCaptor<Long> longCaptor;

    @Captor
    private ArgumentCaptor<FloorManagerRequest> floorReqCaptor;

    @Captor
    private ArgumentCaptor<RoomOwnerRequest> roomReqCaptor;

    @Test
    @DisplayName("PUT /floor/{floorId}/manager -> delegates to service.setManager with id and body")
    void setManager_delegates() {
        Long floorId = 10L;
        FloorManagerRequest req = org.mockito.Mockito.mock(FloorManagerRequest.class);

        controller.setManager(floorId, req);

        verify(adminService).setManager(longCaptor.capture(), floorReqCaptor.capture());
        assertThat(longCaptor.getValue()).isEqualTo(floorId);
        assertThat(floorReqCaptor.getValue()).isSameAs(req);
    }

    @Test
    @DisplayName("DELETE /floor/{floorId}/manager -> delegates to service.deleteManager with id and body")
    void deleteManager_delegates() {
        Long floorId = 11L;
        FloorManagerRequest req = org.mockito.Mockito.mock(FloorManagerRequest.class);

        controller.deleteManager(floorId, req);

        verify(adminService).deleteManager(longCaptor.capture(), floorReqCaptor.capture());
        assertThat(longCaptor.getValue()).isEqualTo(floorId);
        assertThat(floorReqCaptor.getValue()).isSameAs(req);
    }

    @Test
    @DisplayName("PUT /room/{roomId}/owner -> delegates to service.setOwner with id and body")
    void setOwnerRoom_delegates() {
        Long roomId = 20L;
        RoomOwnerRequest req = org.mockito.Mockito.mock(RoomOwnerRequest.class);

        controller.setOwnerRoom(roomId, req);

        verify(adminService).setOwner(longCaptor.capture(), roomReqCaptor.capture());
        assertThat(longCaptor.getValue()).isEqualTo(roomId);
        assertThat(roomReqCaptor.getValue()).isSameAs(req);
    }

    @Test
    @DisplayName("DELETE /room/{roomId}/owner -> delegates to service.deleteOwner with id and body")
    void deleteOwner_delegates() {
        Long roomId = 21L;
        RoomOwnerRequest req = org.mockito.Mockito.mock(RoomOwnerRequest.class);

        controller.deleteOwner(roomId, req);

        verify(adminService).deleteOwner(longCaptor.capture(), roomReqCaptor.capture());
        assertThat(longCaptor.getValue()).isEqualTo(roomId);
        assertThat(roomReqCaptor.getValue()).isSameAs(req);
    }
}
