package vn.io.nghlong3004.apartment_management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;
import vn.io.nghlong3004.apartment_management.model.dto.FloorSummary;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.service.FloorService;

@RestController
@RequestMapping("/api/v1/floor")
@RequiredArgsConstructor
public class FloorController {

	private final FloorService floorService;

	@GetMapping(value = "/{floorId}")
	@ResponseStatus(code = HttpStatus.OK)
	public FloorResponse getFloor(@Min(1) @PathVariable("floorId") Long floorId) {
		return floorService.getFloorWithRooms(floorId);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public PagedResponse<FloorSummary> floors(
			@RequestParam(required = false) @Size(max = 10, message = "Floor name must be at most 10 characters") String name,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestParam(defaultValue = "id,asc") String sort) {
		return floorService.getFloors(name, page, size, sort);
	}

	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	public void createFloor() {
		floorService.createFloor();
	}

	@DeleteMapping("/{floorId}")
	@ResponseStatus(code = HttpStatus.OK)
	public void deleteFloor(@PathVariable @Min(1) Long floorId) {
		floorService.deleteFloor(floorId);
	}

}
