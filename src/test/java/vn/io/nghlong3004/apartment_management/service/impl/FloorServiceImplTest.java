package vn.io.nghlong3004.apartment_management.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.Floor;
import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;
import vn.io.nghlong3004.apartment_management.model.dto.FloorSummary;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.repository.RoomRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FloorServiceImplTest {

    @Mock
    FloorRepository floorRepository;
    @Mock
    RoomRepository roomRepository;

    @InjectMocks
    FloorServiceImpl service;

    @Captor
    ArgumentCaptor<Long> longCaptor;
    @Captor
    ArgumentCaptor<String> stringCaptor;
    @Captor
    ArgumentCaptor<Integer> intCaptor1;
    @Captor
    ArgumentCaptor<Integer> intCaptor2;

    private Floor f(long id, String name, Long managerId, int roomCount) {
        return Floor.builder().id(id).name(name).managerId(managerId).roomCount(roomCount).build();
    }

    @Test
    @DisplayName("getFloorWithRooms: returns response and queries both repositories")
    void getFloorWithRooms_success() {
        Long floorId = 101L;
        when(floorRepository.findById(floorId)).thenReturn(Optional.of(f(floorId, "Floor 101", 10L, 5)));
        when(roomRepository.findAllRoomsByFloorId(floorId)).thenReturn(List.of());

        FloorResponse res = service.getFloorWithRooms(floorId);

        verify(floorRepository).findById(longCaptor.capture());
        assertThat(longCaptor.getValue()).isEqualTo(floorId);
        verify(roomRepository).findAllRoomsByFloorId(floorId);
        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("getFloorWithRooms: throws NOT_FOUND when floor is missing")
    void getFloorWithRooms_notFound() {
        Long floorId = 404L;
        when(floorRepository.findById(floorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFloorWithRooms(floorId))
                .isInstanceOf(ResourceException.class)
                .satisfies(ex -> {
                    ResourceException re = (ResourceException) ex;
                    assertThat(re.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(re.getMessage()).isEqualTo(ErrorMessageConstant.FLOOR_NOT_FOUND);
                });

        verify(roomRepository, never()).findAllRoomsByFloorId(anyLong());
    }

    @Test
    @DisplayName("deleteFloor: deletes when floor exists")
    void deleteFloor_success() {
        Long floorId = 7L;
        when(floorRepository.floorExists(floorId)).thenReturn(Optional.of(true));

        service.deleteFloor(floorId);

        verify(floorRepository).floorExists(longCaptor.capture());
        assertThat(longCaptor.getValue()).isEqualTo(floorId);
        verify(floorRepository).deleteById(floorId);
    }

    @Test
    @DisplayName("deleteFloor: throws BAD_REQUEST when floor does not exist")
    void deleteFloor_notExists() {
        Long floorId = 8L;
        when(floorRepository.floorExists(floorId)).thenReturn(Optional.of(false));

        assertThatThrownBy(() -> service.deleteFloor(floorId))
                .isInstanceOf(ResourceException.class)
                .satisfies(ex -> {
                    ResourceException re = (ResourceException) ex;
                    assertThat(re.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(re.getMessage()).isEqualTo(ErrorMessageConstant.FLOOR_NOT_FOUND);
                });

        verify(floorRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("createFloor: inserts next floor with generated name and roomCount=0")
    void createFloor_success() {
        ReflectionTestUtils.setField(service, "maxFloorNumber", 100L);
        when(floorRepository.countAll()).thenReturn(5L);

        service.createFloor();

        ArgumentCaptor<Floor> floorCaptor = ArgumentCaptor.forClass(Floor.class);
        verify(floorRepository).insert(floorCaptor.capture());
        Floor saved = floorCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("Floor 6");
        assertThat(saved.getRoomCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("createFloor: throws when next floorNumber >= maxFloorNumber")
    void createFloor_exceedsMax() {
        ReflectionTestUtils.setField(service, "maxFloorNumber", 6L);
        when(floorRepository.countAll()).thenReturn(5L);

        assertThatThrownBy(service::createFloor)
                .isInstanceOf(ResourceException.class)
                .satisfies(ex -> {
                    ResourceException re = (ResourceException) ex;
                    assertThat(re.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(re.getMessage()).isEqualTo(ErrorMessageConstant.FLOOR_NUMBER_EXCEEDS_LIMIT);
                });

        verify(floorRepository, never()).insert(any());
    }

    @Test
    @DisplayName("getFloors(name): trims name, finds by name and returns single-item page")
    void getFloors_byName_success() {
        String raw = "  Floor 1  ";
        String trimmed = "Floor 1";
        when(floorRepository.findByName(trimmed)).thenReturn(Optional.of(f(1L, trimmed, 10L, 5)));

        PagedResponse<FloorSummary> res = service.getFloors(raw, 0, 20, "id,asc");

        verify(floorRepository).findByName(trimmed);
        assertThat(res.getPage()).isEqualTo(0);
        assertThat(res.getSize()).isEqualTo(1);
        assertThat(res.getTotalElements()).isEqualTo(1);
        assertThat(res.getTotalPages()).isEqualTo(1);
        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getContent().get(0).getName()).isEqualTo("Floor 1");
    }

    @Test
    @DisplayName("getFloors(name): throws NOT_FOUND when floor not found")
    void getFloors_byName_notFound() {
        when(floorRepository.findByName("Floor 404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFloors("Floor 404", 0, 20, "id,asc"))
                .isInstanceOf(ResourceException.class)
                .satisfies(ex -> {
                    ResourceException re = (ResourceException) ex;
                    assertThat(re.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(re.getMessage()).isEqualTo(ErrorMessageConstant.FLOOR_NOT_FOUND);
                });

        verify(floorRepository, never()).findPage(anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("getFloors(paging): total=0 -> empty content, totalPages=1, findPage not invoked")
    void getFloors_paging_totalZero() {
        when(floorRepository.countAll()).thenReturn(0L);

        PagedResponse<FloorSummary> res = service.getFloors(null, 3, 50, "name,desc");

        verify(floorRepository).countAll();
        verify(floorRepository, never()).findPage(anyString(), anyInt(), anyInt());
        assertThat(res.getContent()).isEmpty();
        assertThat(res.getPage()).isEqualTo(3);
        assertThat(res.getSize()).isEqualTo(50);
        assertThat(res.getTotalElements()).isEqualTo(0);
        assertThat(res.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("getFloors(paging): normalizes negative page and zero size; computes orderBy and offset; maps content")
    void getFloors_paging_positiveTotal_normalization() {
        when(floorRepository.countAll()).thenReturn(5L);
        when(floorRepository.findPage(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(
                        f(1L, "Floor 1", 10L, 5),
                        f(2L, "Floor 2", 11L, 7),
                        f(3L, "Floor 3", 12L, 2)
                ));

        PagedResponse<FloorSummary> res = service.getFloors(null, -1, 0, "id,desc");

        ArgumentCaptor<String> orderBy = ArgumentCaptor.forClass(String.class);
        verify(floorRepository).findPage(orderBy.capture(), intCaptor1.capture(), intCaptor2.capture());

        assertThat(orderBy.getValue()).isEqualTo("id DESC");
        assertThat(intCaptor1.getValue()).isEqualTo(1);
        assertThat(intCaptor2.getValue()).isEqualTo(0);

        assertThat(res.getPage()).isEqualTo(0);
        assertThat(res.getSize()).isEqualTo(1);
        assertThat(res.getTotalElements()).isEqualTo(5);
        assertThat(res.getTotalPages()).isEqualTo(5);
        assertThat(res.getContent()).hasSize(3);
        assertThat(res.getContent().get(0).getName()).isEqualTo("Floor 1");
    }

    @Test
    @DisplayName("getFloors(paging): computes offset=page*size and totalPages=ceil(total/size)")
    void getFloors_paging_offset_and_totalPages() {
        when(floorRepository.countAll()).thenReturn(9L);
        when(floorRepository.findPage(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(f(4L, "Floor 4", 13L, 3), f(5L, "Floor 5", 14L, 1)));

        PagedResponse<FloorSummary> res = service.getFloors(null, 2, 2, "name,asc");

        ArgumentCaptor<String> orderBy = ArgumentCaptor.forClass(String.class);
        verify(floorRepository).findPage(orderBy.capture(), intCaptor1.capture(), intCaptor2.capture());

        assertThat(orderBy.getValue()).isEqualTo("name ASC");
        assertThat(intCaptor1.getValue()).isEqualTo(2);
        assertThat(intCaptor2.getValue()).isEqualTo(4);

        assertThat(res.getPage()).isEqualTo(2);
        assertThat(res.getSize()).isEqualTo(2);
        assertThat(res.getTotalElements()).isEqualTo(9);
        assertThat(res.getTotalPages()).isEqualTo(5);
        assertThat(res.getContent()).hasSize(2);
    }
}
