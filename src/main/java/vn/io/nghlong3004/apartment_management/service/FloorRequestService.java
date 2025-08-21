package vn.io.nghlong3004.apartment_management.service;

import vn.io.nghlong3004.apartment_management.model.dto.CreateRequest;
import vn.io.nghlong3004.apartment_management.model.dto.FloorRequestSummary;
import vn.io.nghlong3004.apartment_management.model.dto.PagedResponse;
import vn.io.nghlong3004.apartment_management.model.dto.UpdateRequest;

public interface FloorRequestService {
	void create(CreateRequest request);

	void update(Long requestId, UpdateRequest request);

	PagedResponse<FloorRequestSummary> getRequests(int page, int size, String sort);
}
