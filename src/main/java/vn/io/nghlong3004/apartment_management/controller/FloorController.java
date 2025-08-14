package vn.io.nghlong3004.apartment_management.controller;

import org.springframework.http.HttpStatus;
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

	@PutMapping(value = "/{floorId}", consumes = "application/json")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void moveRoom(@Min(1) @PathVariable("floorId") Long floorId,
			@RequestBody @Valid JoinRoomRequest joinRoomRequest) {
		floorService.createMoveRequest(floorId, joinRoomRequest.roomId());
	}

}
