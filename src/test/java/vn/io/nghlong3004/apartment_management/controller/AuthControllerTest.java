package vn.io.nghlong3004.apartment_management.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.io.nghlong3004.apartment_management.exception.AppException;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;
import vn.io.nghlong3004.apartment_management.service.UserService;
import vn.io.nghlong3004.apartment_management.util.MessageConstants;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	private RegisterRequest createSampleRegisterRequest() {
		return RegisterRequest.builder().firstName("Long").lastName("Nguyen").email("nghlong3004@example.com")
				.password("matkhaune!A@1234").phoneNumber("0123456789").build();
	}

	@Test
	@DisplayName("Method: registerUser - Should call service and return 201 CREATED when request is valid")
	void AuthController_RegisterUser_WhenRequestIsValidShouldReturnCreateds()
			throws JsonProcessingException, Exception {
		RegisterRequest registerRequest = createSampleRegisterRequest();
		Mockito.doNothing().when(userService).register(any(RegisterRequest.class));

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registerRequest))).andExpect(status().isCreated());
	}

	@Test
	@DisplayName("Method: registerUser - Should return 400 BAD REQUEST when request is invalid")
	void registerUser_WhenRequestIsInvalid_ShouldReturnBadRequest() throws Exception {
		RegisterRequest invalidRequest = RegisterRequest.builder().build();

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Method: registerUser - Should return 400 BAD REQUEST when email already exists")
	void registerUser_WhenServiceThrowsAppException_ShouldReturnBadRequest() throws Exception {

		RegisterRequest validRegisterRequest = createSampleRegisterRequest();

		doThrow(new AppException(HttpStatus.BAD_REQUEST, MessageConstants.EXISTS_EMAIL)).when(userService)
				.register(any(RegisterRequest.class));

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRegisterRequest))).andExpect(status().isBadRequest());

		verify(userService).register(any(RegisterRequest.class));
	}

}
