package vn.io.nghlong3004.apartment_management.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;
import vn.io.nghlong3004.apartment_management.model.dto.FloorSummary;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.service.FloorService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FloorControllerTest {

    @Mock
    private FloorService floorService;

    @InjectMocks
    private FloorController controller;

    @Captor
    private ArgumentCaptor<Long> longCaptor;
    @Captor
    private ArgumentCaptor<String> stringCaptor;
    @Captor
    private ArgumentCaptor<Integer> intCaptor1;
    @Captor
    private ArgumentCaptor<Integer> intCaptor2;
    @Captor
    private ArgumentCaptor<String> sortCaptor;

    private FloorSummary sum(long id, String name, Long managerId, int roomCount) {
        return FloorSummary.builder()
                .id(id)
                .name(name)
                .managerId(managerId)
                .roomCount(roomCount)
                .build();
    }

    private PagedResponse<FloorSummary> samplePage() {
        return PagedResponse.<FloorSummary>builder()
                .content(List.of(
                        sum(1L, "Floor 1", 10L, 5),
                        sum(2L, "Floor 2", 11L, 7)))
                .page(0).size(2).totalElements(8).totalPages(4)
                .build();
    }

    @Test
    @DisplayName("GET /{floorId} -> returns FloorResponse and delegates correctly")
    void getFloor_returns_and_delegates() {
        Long floorId = 123L;
        FloorResponse expected = org.mockito.Mockito.mock(FloorResponse.class);

        when(floorService.getFloorWithRooms(floorId)).thenReturn(expected);

        FloorResponse got = controller.getFloor(floorId);

        verify(floorService).getFloorWithRooms(longCaptor.capture());
        assertThat(longCaptor.getValue()).isEqualTo(floorId);
        assertThat(got).isSameAs(expected);
    }

    @Test
    @DisplayName("GET / (no name) -> returns paged list and delegates with paging/sort")
    void listFloors_paged_noName() {
        String name = null;
        int page = 2;
        int size = 50;
        String sort = "id,desc";

        PagedResponse<FloorSummary> expected = samplePage();
        when(floorService.getFloors(name, page, size, sort)).thenReturn(expected);

        PagedResponse<FloorSummary> got = controller.floors(name, page, size, sort);

        verify(floorService).getFloors(
                isNull(),
                intCaptor1.capture(),
                intCaptor2.capture(),
                sortCaptor.capture());

        assertThat(intCaptor1.getValue()).isEqualTo(page);
        assertThat(intCaptor2.getValue()).isEqualTo(size);
        assertThat(sortCaptor.getValue()).isEqualTo(sort);

        assertThat(got).isEqualTo(expected);
        assertThat(got.getContent()).hasSize(2);
        assertThat(got.getPage()).isEqualTo(0);
        assertThat(got.getTotalElements()).isEqualTo(8);
    }

    @Test
    @DisplayName("GET /?name=... -> returns filtered floors by name")
    void listFloors_withName() {
        String name = "Floor 1";
        int page = 0;
        int size = 20;
        String sort = "id,asc";

        PagedResponse<FloorSummary> expected = PagedResponse.<FloorSummary>builder()
                .content(List.of(sum(1L, "Floor 1", 10L, 5)))
                .page(0).size(1).totalElements(1).totalPages(1).build();

        when(floorService.getFloors(name, page, size, sort)).thenReturn(expected);

        PagedResponse<FloorSummary> got = controller.floors(name, page, size, sort);

        verify(floorService).getFloors(
                stringCaptor.capture(),
                intCaptor1.capture(),
                intCaptor2.capture(),
                sortCaptor.capture());

        assertThat(stringCaptor.getValue()).isEqualTo(name);
        assertThat(intCaptor1.getValue()).isEqualTo(page);
        assertThat(intCaptor2.getValue()).isEqualTo(size);
        assertThat(sortCaptor.getValue()).isEqualTo(sort);

        assertThat(got).isEqualTo(expected);
        assertThat(got.getContent()).hasSize(1);
        assertThat(got.getContent().get(0).getName()).isEqualTo("Floor 1");
    }

    @Test
    @DisplayName("POST / -> delegates to service.createFloor")
    void createFloor_delegates() {
        controller.createFloor();
        verify(floorService).createFloor();
    }

    @Test
    @DisplayName("DELETE /{floorId} -> delegates to service.deleteFloor")
    void deleteFloor_delegates() {
        Long floorId = 999L;

        controller.deleteFloor(floorId);

        verify(floorService).deleteFloor(longCaptor.capture());
        assertThat(longCaptor.getValue()).isEqualTo(floorId);
    }
}
