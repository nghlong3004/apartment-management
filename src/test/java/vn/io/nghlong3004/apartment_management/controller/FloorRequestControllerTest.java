package vn.io.nghlong3004.apartment_management.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.nghlong3004.apartment_management.model.dto.CreateRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorRequestSummary;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.UpdateRequest;
import vn.io.nghlong3004.apartment_management.service.FloorRequestService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FloorRequestControllerTest {

    @Mock
    private FloorRequestService floorRequestService;

    @InjectMocks
    private FloorRequestController controller;

    @Captor
    private ArgumentCaptor<CreateRequest> createCaptor;
    @Captor
    private ArgumentCaptor<Long> idCaptor;
    @Captor
    private ArgumentCaptor<UpdateRequest> updateCaptor;
    @Captor
    private ArgumentCaptor<Integer> pageCaptor;
    @Captor
    private ArgumentCaptor<Integer> sizeCaptor;
    @Captor
    private ArgumentCaptor<String> sortCaptor;

    @Test
    @DisplayName("POST / -> delegates to service.create with request body")
    void createRequest_delegates() {
        CreateRequest req = org.mockito.Mockito.mock(CreateRequest.class);

        controller.createRequest(req);

        verify(floorRequestService).create(createCaptor.capture());
        assertThat(createCaptor.getValue()).isSameAs(req);
    }

    @Test
    @DisplayName("PUT /{requestId} -> delegates to service.update with id and body")
    void updateRequest_delegates() {
        Long requestId = 123L;
        UpdateRequest req = org.mockito.Mockito.mock(UpdateRequest.class);

        controller.updateRequest(requestId, req);

        verify(floorRequestService).update(idCaptor.capture(), updateCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(requestId);
        assertThat(updateCaptor.getValue()).isSameAs(req);
    }

    @Test
    @DisplayName("GET / -> returns paged list and delegates with paging/sort")
    void requests_paged() {
        int page = 2;
        int size = 50;
        String sort = "created,desc";

        FloorRequestSummary s1 = org.mockito.Mockito.mock(FloorRequestSummary.class);
        FloorRequestSummary s2 = org.mockito.Mockito.mock(FloorRequestSummary.class);
        PagedResponse<FloorRequestSummary> expected = PagedResponse.<FloorRequestSummary>builder()
                .content(List.of(s1, s2))
                .page(2).size(50).totalElements(7).totalPages(4)
                .build();

        when(floorRequestService.getRequests(page, size, sort)).thenReturn(expected);

        PagedResponse<FloorRequestSummary> got = controller.requests(page, size, sort);

        verify(floorRequestService).getRequests(pageCaptor.capture(), sizeCaptor.capture(), sortCaptor.capture());
        assertThat(pageCaptor.getValue()).isEqualTo(page);
        assertThat(sizeCaptor.getValue()).isEqualTo(size);
        assertThat(sortCaptor.getValue()).isEqualTo(sort);

        assertThat(got).isEqualTo(expected);
        assertThat(got.getContent()).hasSize(2);
        assertThat(got.getTotalElements()).isEqualTo(7);
        assertThat(got.getTotalPages()).isEqualTo(4);
    }
}
