package vn.io.nghlong3004.apartment_management.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record JoinRoomRequest(@NotNull @Min(1) Long roomId) {

}
