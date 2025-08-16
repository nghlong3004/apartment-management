package vn.io.nghlong3004.apartment_management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import vn.io.nghlong3004.apartment_management.model.dto.FloorRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorResponse;
import vn.io.nghlong3004.apartment_management.model.dto.JoinRoomRequest;
import vn.io.nghlong3004.apartment_management.service.FloorService;

@RestController
@RequestMapping("/api/v1/floor")
@RequiredArgsConstructor
public class FloorController {

	private final FloorService floorService;

	@PostMapping(value = "/{floorId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.CREATED)
	public void joinRoom(@Min(1) @PathVariable("floorId") Long floorId,
			@RequestBody @Valid JoinRoomRequest joinRoomRequest) {
		floorService.createJoinRequest(floorId, joinRoomRequest.roomId());
	}

	@PutMapping(value = "/{floorId}/room/move", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.CREATED)
	public void moveRoom(@Min(1) @PathVariable("floorId") Long floorId,
			@RequestBody @Valid JoinRoomRequest joinRoomRequest) {
		floorService.createMoveRequest(floorId, joinRoomRequest.roomId());
	}

	@GetMapping(value = "/{floorId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	public FloorResponse getFloor(@Min(1) @PathVariable("floorId") Long floorId) {
		return floorService.getFloorWithRooms(floorId);
	}

	@GetMapping(params = "name")
	@ResponseStatus(HttpStatus.OK)
	public FloorResponse getFloorByName(
			@RequestParam("name") @NotBlank @Size(max = 10, message = "Floor name must be at most 10 characters") String name) {
		return floorService.getFloorByName(name);
	}

	@PutMapping(value = "/{floorId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	public void updateFloor(@Min(1) @PathVariable("floorId") Long floorId, @RequestBody @Valid FloorRequest request) {
		floorService.updateFloor(floorId, request);
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.CREATED)
	public void addFloor(@RequestBody @Valid FloorRequest request) {
		floorService.addFloor(request);
	}

	@DeleteMapping("/{floorId}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void deleteFloor(@PathVariable @Min(1) Long floorId) {
		floorService.deleteFloor(floorId);
	}

}
