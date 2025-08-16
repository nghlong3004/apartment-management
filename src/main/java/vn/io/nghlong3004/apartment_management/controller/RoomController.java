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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.RoomRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomResponse;
import vn.io.nghlong3004.apartment_management.service.RoomService;

@RestController
@RequestMapping("/api/v1/floor")
@RequiredArgsConstructor
public class RoomController {

	private final RoomService roomService;

	@PostMapping(value = "/{floorId}/room", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.CREATED)
	public void createRoom(@PathVariable @Min(1) Long floorId, @RequestBody @Valid RoomRequest request) {
		roomService.createRoom(floorId, request);
	}

	@GetMapping("/{floorId}/room/{roomId}")
	@ResponseStatus(code = HttpStatus.OK)
	public RoomResponse getRoom(@PathVariable @Min(1) Long floorId, @PathVariable @Min(1) Long roomId) {
		return roomService.getRoomResponse(floorId, roomId);
	}

	@GetMapping(value = "/{floorId}/room")
	@ResponseStatus(HttpStatus.OK)
	public PagedResponse<RoomResponse> rooms(@PathVariable @Min(1) Long floorId,
			@RequestParam(required = false) @Size(max = 20, message = "Room name must be at most 20 characters") String name,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestParam(defaultValue = "id,asc") String sort) {
		return roomService.getRooms(floorId, name, page, size, sort);
	}

	@PutMapping(value = "/{floorId}/room/{roomId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	public void updateRoom(@PathVariable @Min(1) Long floorId, @PathVariable @Min(1) Long roomId,
			@RequestBody @Valid RoomRequest request) {
		roomService.updateRoom(floorId, roomId, request);
	}

	@DeleteMapping("/{floorId}/room/{roomId}")
	@ResponseStatus(code = HttpStatus.OK)
	public void deleteRoom(@PathVariable @Min(1) Long floorId, @PathVariable @Min(1) Long roomId) {
		roomService.deleteRoom(floorId, roomId);
	}

}
