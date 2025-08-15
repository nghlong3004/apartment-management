package vn.io.nghlong3004.apartment_management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import vn.io.nghlong3004.apartment_management.model.dto.FloorCreateRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;
import vn.io.nghlong3004.apartment_management.model.dto.FloorUpdateRequest;
import vn.io.nghlong3004.apartment_management.model.dto.JoinRoomRequest;
import vn.io.nghlong3004.apartment_management.service.FloorService;

@RestController
@RequestMapping("/api/v1/floor")
@RequiredArgsConstructor
public class FloorController {

	private final FloorService floorService;

	@PostMapping(value = "/{floorId}", consumes = "application/json")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void joinRoom(@Min(1) @PathVariable("floorId") Long floorId,
			@RequestBody @Valid JoinRoomRequest joinRoomRequest) {
		floorService.createJoinRequest(floorId, joinRoomRequest.roomId());
	}

	@PutMapping(value = "/{floorId}/room/move", consumes = "application/json")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void moveRoom(@Min(1) @PathVariable("floorId") Long floorId,
			@RequestBody @Valid JoinRoomRequest joinRoomRequest) {
		floorService.createMoveRequest(floorId, joinRoomRequest.roomId());
	}

	@GetMapping(value = "/{floorId}", consumes = "application/json")
	@ResponseStatus(code = HttpStatus.OK)
	public FloorResponse getFloor(@Min(1) @PathVariable("floorId") Long floorId) {
		return floorService.getFloorWithRooms(floorId);
	}

	@PutMapping(value = "/{floorId}", consumes = "application/json")
	@ResponseStatus(code = HttpStatus.OK)
	public void updateFloor(@Min(1) @PathVariable("floorId") Long floorId,
			@RequestBody @Valid FloorUpdateRequest request) {
		floorService.updateFloor(floorId, request);
	}

	@PostMapping(consumes = "application/json")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void addFloor(@RequestBody @Valid FloorCreateRequest request) {
		floorService.addFloor(request);
	}

	@DeleteMapping("/{floorId}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void deleteFloor(@PathVariable @Min(1) Long floorId) {
		floorService.deleteFloor(floorId);
	}

}
