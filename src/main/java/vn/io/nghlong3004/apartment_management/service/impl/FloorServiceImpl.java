package vn.io.nghlong3004.apartment_management.service.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.Floor;
import vn.io.nghlong3004.apartment_management.model.dto.FloorRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;
import vn.io.nghlong3004.apartment_management.model.dto.FloorSummary;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.service.FloorService;
import vn.io.nghlong3004.apartment_management.service.RoomService;
import vn.io.nghlong3004.apartment_management.util.HelperUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloorServiceImpl implements FloorService {

	private final FloorRepository floorRepository;
	private final RoomService roomService;

	@Override
	@Transactional(readOnly = true)
	public FloorResponse getFloorWithRooms(Long floorId) {
		log.info("Retrieving floor details for floorId={}", floorId);

		Floor floor = floorRepository.findById(floorId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.FLOOR_NOT_FOUND));

		return FloorResponse.from(floor, roomService.getAllRooms(floorId));
	}

	@Override
	@Transactional
	public void deleteFloor(Long floorId) {
		log.info("Deleting floor floorId={}", floorId);

		floorRepository.deleteById(floorId);

		log.debug("Floor deleted: floorId={}", floorId);
	}

	@Override
	@Transactional
	public void updateFloor(Long floorId, FloorRequest floorUpdateRequest) {
		log.info("Updating floor with floorId={}", floorId);

		Floor existingFloor = floorRepository.findById(floorId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.FLOOR_NOT_FOUND));

		existingFloor.setName(floorUpdateRequest.name());
		existingFloor.setManagerId(floorUpdateRequest.managerId());

		floorRepository.updateFloor(existingFloor);

		log.debug("Floor updated successfully: floorId={}", floorId);

	}

	@Override
	@Transactional
	public void addFloor(FloorRequest floorRequest) {
		log.info("Creating floor name = {}", floorRequest.name());

		if (floorRepository.existsByName(floorRequest.name()).orElse(false)) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.FLOOR_NAME_ALREADY_EXISTS);
		}

		Floor floor = Floor.builder().name(floorRequest.name()).roomCount(0).build();

		floorRepository.insert(floor);

		log.debug("Floor created successfully id={}", floor.getId());
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<FloorSummary> getFloors(String name, int page, int size, String sort) {
		log.info("Floors query: name='{}', page={}, size={}, sort={}", name, page, size, sort);

		if (name != null && !name.isBlank()) {
			return getFloorByName(name);
		}
		return listFloors(page, size, sort);
	}

	private PagedResponse<FloorSummary> getFloorByName(String currentName) {
		long t0 = System.nanoTime();
		final String name = currentName.trim();
		log.info("Start retrieving floor by name: {}", name);
		Floor floor = floorRepository.findByName(name)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.FLOOR_NOT_FOUND));

		FloorSummary one = FloorSummary.from(floor);

		log.debug("Floors(byName) -> 1 item, timeMs={}", (System.nanoTime() - t0) / 1_000_000.0);
		return PagedResponse.<FloorSummary>builder().content(List.of(one)).page(0).size(1).totalElements(1)
				.totalPages(1).build();
	}

	private PagedResponse<FloorSummary> listFloors(int page, int size, String sort) {
		log.info("Listing floors page={}, size={}, sort={}", page, size, sort);
		long t0 = System.nanoTime();
		int safePage = Math.max(page, 0);
		int safeSize = Math.max(size, 1);
		int offset = safePage * safeSize;
		String orderBy = HelperUtil.normalizeSort(sort);

		long total = floorRepository.countAll();
		List<Floor> rows = total == 0 ? List.of() : floorRepository.findPage(orderBy, safeSize, offset);

		List<FloorSummary> content = rows.stream().map(FloorSummary::from).toList();

		int totalPages = (int) Math.ceil(total / (double) safeSize);

		log.debug("Floors(list) -> fetched={}, total={}, timeMs={}", content.size(), total,
				(System.nanoTime() - t0) / 1_000_000.0);

		return PagedResponse.<FloorSummary>builder().content(content).page(safePage).size(safeSize).totalElements(total)
				.totalPages(Math.max(totalPages, 1)).build();
	}
}
