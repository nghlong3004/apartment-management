package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.NotNull;
import vn.io.nghlong3004.apartment_management.model.RequestStatus;

public record UpdateRequest(@NotNull RequestStatus status, String reason) {

}
