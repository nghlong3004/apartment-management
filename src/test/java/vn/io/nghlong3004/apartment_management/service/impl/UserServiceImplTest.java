package vn.io.nghlong3004.apartment_management.service.impl;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.io.nghlong3004.apartment_management.exception.AppException;
import vn.io.nghlong3004.apartment_management.model.Role;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserStatus;
import vn.io.nghlong3004.apartment_management.model.dto.RegisterRequest;
import vn.io.nghlong3004.apartment_management.repository.UserRepository;
import vn.io.nghlong3004.apartment_management.util.MessageConstants;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository mockUserRepository;
	@Mock
	private PasswordEncoder mockPasswordEncoder;

	@InjectMocks
	private UserServiceImpl userServiceImpl;

	@Captor
	private ArgumentCaptor<User> userArgumentCaptor;

	private RegisterRequest createSampleRegisterRequest() {
		return RegisterRequest.builder().firstName("Long").lastName("Nguyen").email("nghlong3004@example.com")
				.password("matkhaune!A@1234").phoneNumber("0123456789").build();
	}

	@Test
	@DisplayName("Method: Register -> Save user successfully when email does not exist")
	void userServiceImpl_Register_WhenEmailDoesNotExist_ShouldSaveUserSuccessfully() {
		RegisterRequest registerRequest = createSampleRegisterRequest();
		Mockito.when(mockUserRepository.existsByEmail(registerRequest.getEmail())).thenReturn(Optional.of(false));
		Mockito.when(mockPasswordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword123");

		userServiceImpl.register(registerRequest);

		Mockito.verify(mockUserRepository).save(userArgumentCaptor.capture());

		User savedUser = userArgumentCaptor.getValue();

		Assertions.assertNotNull(savedUser);
		Assertions.assertEquals(registerRequest.getFirstName(), savedUser.getFirstName());
		Assertions.assertEquals(registerRequest.getLastName(), savedUser.getLastName());
		Assertions.assertEquals(registerRequest.getEmail(), savedUser.getEmail());
		Assertions.assertEquals("encodedPassword123", savedUser.getPassword());
		Assertions.assertEquals(Role.USER, savedUser.getRole());
		Assertions.assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
	}

	@Test
	@DisplayName("Method: register -> Throw AppException when email already exists")
	void UserServiceImpl_Register_WhenEmailAlreadyExists_ShouldThrowAppException() {
		RegisterRequest request = createSampleRegisterRequest();

		Mockito.when(mockUserRepository.existsByEmail(request.getEmail())).thenReturn(Optional.of(true));

		AppException exception = Assertions.assertThrows(AppException.class, () -> {
			userServiceImpl.register(request);
		});

		Assertions.assertEquals(exception.getHttpStatus(), HttpStatus.BAD_REQUEST);
		Assertions.assertEquals(exception.getMessage(), MessageConstants.EXISTS_EMAIL);
	}
}