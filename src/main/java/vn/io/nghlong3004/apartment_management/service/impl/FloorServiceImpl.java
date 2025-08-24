package vn.io.nghlong3004.apartment_management.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.Floor;
import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;
import vn.io.nghlong3004.apartment_management.model.dto.FloorSummary;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.repository.FloorRepository;
import vn.io.nghlong3004.apartment_management.repository.RoomRepository;
import vn.io.nghlong3004.apartment_management.service.FloorService;
import vn.io.nghlong3004.apartment_management.util.HelperUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloorServiceImpl implements FloorService {

	private final FloorRepository floorRepository;
	private final RoomRepository roomRepository;

	@Value("${apartment.management.floor.max-number}")
	private long maxFloorNumber;

	@Override
	@Transactional(readOnly = true)
	public FloorResponse getFloorWithRooms(Long floorId) {
		log.info("Retrieving floor details for floorId={}", floorId);

		Floor floor = floorRepository.findById(floorId)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.FLOOR_NOT_FOUND));

		return FloorResponse.from(floor, roomRepository.findAllRoomsByFloorId(floorId));
	}

	@Override
	@Transactional
	public void deleteFloor(Long floorId) {
		log.info("Deleting floor floorId={}", floorId);
		if (!floorRepository.floorExists(floorId).orElse(false)) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.FLOOR_NOT_FOUND);
		}
		floorRepository.deleteById(floorId);

		log.debug("Floor deleted: floorId={}", floorId);
	}

	@Override
	@Transactional
	public void createFloor() {
		log.info("Creating floor");
		long floorNumber = floorRepository.countAll() + 1;
		validateFloorNumber(floorNumber);

		Floor floor = Floor.builder().name(HelperUtil.generateFloorName(floorNumber)).roomCount(0).build();
		floorRepository.insert(floor);

		log.info("Floor created successfully id={}", floor.getId());
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
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		final String name = currentName.trim();
		log.info("Start retrieving floor by name: {}", name);
		Floor floor = floorRepository.findByName(name)
				.orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, ErrorMessageConstant.FLOOR_NOT_FOUND));

		FloorSummary one = FloorSummary.from(floor);

		stopWatch.stop();
		log.info("Successfully fetched floor by name='{}'  elapsedMs={}", name, stopWatch.getTotalTimeMillis());
		return PagedResponse.<FloorSummary>builder().content(List.of(one)).page(0).size(1).totalElements(1)
				.totalPages(1).build();
	}

	private PagedResponse<FloorSummary> listFloors(int page, int size, String sort) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		log.info("Listing floors page={}, size={}, sort={}", page, size, sort);
		int safePage = Math.max(page, 0);
		int safeSize = Math.max(size, 1);
		int offset = safePage * safeSize;
		String orderBy = HelperUtil.normalizeSort(sort);

		long total = floorRepository.countAll();
		List<Floor> rows = total == 0 ? List.of() : floorRepository.findPage(orderBy, safeSize, offset);

		List<FloorSummary> content = rows.stream().map(FloorSummary::from).toList();

		int totalPages = (int) Math.ceil(total / (double) safeSize);

		stopWatch.stop();
		log.info("Successfully fetched {} floors (of total={}) for totalPages={}, Elapsed time={} ms", content.size(),
				total, totalPages, stopWatch.getTotalTimeMillis());
		return PagedResponse.<FloorSummary>builder().content(content).page(safePage).size(safeSize).totalElements(total)
				.totalPages(Math.max(totalPages, 1)).build();
	}

	private void validateFloorNumber(long floorNumber) {
		if (floorNumber >= maxFloorNumber) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.FLOOR_NUMBER_EXCEEDS_LIMIT);
		}
	}
}
