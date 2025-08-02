package vn.io.nghlong3004.apartment_management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;
import vn.io.nghlong3004.apartment_management.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	@ResponseStatus(code = HttpStatus.CREATED, reason = "User registered successfully!")
	public String registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
		userService.register(registerRequest);
		return "User registered successfully!";
	}

}
