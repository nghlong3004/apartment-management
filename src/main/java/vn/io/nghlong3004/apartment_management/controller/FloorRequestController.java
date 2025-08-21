package vn.io.nghlong3004.apartment_management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import lombok.RequiredArgsConstructor;
import vn.io.nghlong3004.apartment_management.model.dto.CreateRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorRequestSummary;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.UpdateRequest;
import vn.io.nghlong3004.apartment_management.service.FloorRequestService;

@RestController
@RequestMapping("/api/v1/request")
@RequiredArgsConstructor
public class FloorRequestController {

	private final FloorRequestService floorRequestService;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	public void createRequest(@RequestBody @Valid CreateRequest request) {
		floorRequestService.create(request);
	}

	@PutMapping(value = "/{requestId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	public void updateRequest(@Min(1) @PathVariable Long requestId, @RequestBody @Valid UpdateRequest request) {
		floorRequestService.update(requestId, request);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public PagedResponse<FloorRequestSummary> requests(@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestParam(defaultValue = "id,asc") String sort) {
		return floorRequestService.getRequests(page, size, sort);
	}

}
