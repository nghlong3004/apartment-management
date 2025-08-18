package vn.io.nghlong3004.apartment_management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.io.nghlong3004.apartment_management.model.dto.FloorManagerRequest;
import vn.io.nghlong3004.apartment_management.model.dto.RoomOwnerRequest;
import vn.io.nghlong3004.apartment_management.service.AdminService;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

	private final AdminService adminService;

	@PutMapping(value = "/floor/{floorId}/manager", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	public void setManager(@PathVariable Long floorId, @RequestBody @Valid FloorManagerRequest request) {
		adminService.setManager(floorId, request);
	}

	@DeleteMapping(value = "/floor/{floorId}/manager", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	public void deleteManager(@PathVariable Long floorId, @RequestBody @Valid FloorManagerRequest request) {
		adminService.deleteManager(floorId, request);
	}

	@PutMapping(value = "/room/{roomId}/owner", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	public void setOwnerRoom(@PathVariable Long roomId, @RequestBody @Valid RoomOwnerRequest request) {
		adminService.setOwner(roomId, request);
	}

	@DeleteMapping(value = "/room/{roomId}/owner", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	public void deleteOwner(@PathVariable Long roomId, @RequestBody @Valid RoomOwnerRequest request) {
		adminService.deleteOwner(roomId, request);
	}

}
