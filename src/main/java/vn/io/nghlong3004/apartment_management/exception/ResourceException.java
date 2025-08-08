package vn.io.nghlong3004.apartment_management.exception;

import lombok.Getter;

@Getter
public class ResourceException extends RuntimeException {
	private final ErrorState errorState;

	public ResourceException(ErrorState errorState) {
		super(errorState.getMessage());
		this.errorState = errorState;
	}

}
