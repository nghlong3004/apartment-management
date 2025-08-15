package vn.io.nghlong3004.apartment_management.controller;

import java.util.List;

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
import vn.io.nghlong3004.apartment_management.model.dto.RoomRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomResponse;
import vn.io.nghlong3004.apartment_management.service.RoomService;

@RestController
@RequestMapping("/api/v1/floor")
@RequiredArgsConstructor
public class RoomController {

	private final RoomService roomService;

	@PostMapping(value = "/{floorId}/room", consumes = "application/json")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void createRoom(@PathVariable @Min(1) Long floorId, @RequestBody @Valid RoomRequest request) {
		roomService.createRoom(floorId, request);
	}

	@GetMapping("/{floorId}/room")
	@ResponseStatus(code = HttpStatus.OK)
	public List<RoomResponse> listRooms(@PathVariable @Min(1) Long floorId) {
		return roomService.getRoomsByFloor(floorId);
	}

	@GetMapping("/{floorId}/room/{roomId}")
	@ResponseStatus(code = HttpStatus.OK)
	public RoomResponse getRoom(@PathVariable @Min(1) Long floorId, @PathVariable @Min(1) Long roomId) {
		return roomService.getRoomResponse(floorId, roomId);
	}

	@PutMapping(value = "/{floorId}/room/{roomId}", consumes = "application/json")
	@ResponseStatus(code = HttpStatus.OK)
	public void updateRoom(@PathVariable @Min(1) Long floorId, @PathVariable @Min(1) Long roomId,
			@RequestBody @Valid RoomRequest request) {
		roomService.updateRoom(floorId, roomId, request);
	}

	@DeleteMapping("/{floorId}/room/{roomId}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void deleteRoom(@PathVariable @Min(1) Long floorId, @PathVariable @Min(1) Long roomId) {
		roomService.deleteRoom(floorId, roomId);
	}

}
