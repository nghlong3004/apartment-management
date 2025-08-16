package vn.io.nghlong3004.apartment_management.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.dto.UserDto;
import vn.io.nghlong3004.apartment_management.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	@Mock
	private UserService mockUserService;

	@InjectMocks
	private UserController userController;

	@Captor
	private ArgumentCaptor<UserDto> userDtoCaptor;

	private UserDto sampleUserDto() {
		return UserDto.builder().email("user+" + UUID.randomUUID() + "@example.com").firstName("John").lastName("Doe")
				.phoneNumber("0909123456").build();
	}

	@Test
	@DisplayName("GET /api/v1/user/{id} -> should return UserDto when found")
	void getProfile_ShouldReturnUserDto() {
		Long id = 10L;
		UserDto expected = sampleUserDto();

		when(mockUserService.getUser(id)).thenReturn(expected);

		UserDto response = userController.getProfile(id);

		verify(mockUserService).getUser(id);
		Assertions.assertEquals(expected, response);
	}

	@Test
	@DisplayName("GET /api/v1/user/{id} -> should throw when user not found")
	void getProfile_ShouldThrowWhenNotFound() {
		Long id = 999L;
		when(mockUserService.getUser(id))
				.thenThrow(new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ENDPOINT_NOT_FOUND));

		Assertions.assertThrows(ResourceException.class, () -> userController.getProfile(id));
	}

	@Test
	@DisplayName("PUT /api/v1/user/{id} -> should delegate to service with correct params")
	void updateProfile_ShouldDelegateToService() {
		Long id = 7L;
		UserDto dto = sampleUserDto();

		userController.updateProfile(id, dto);

		verify(mockUserService).updateUser(org.mockito.Mockito.eq(id), userDtoCaptor.capture());
		UserDto captured = userDtoCaptor.getValue();
		Assertions.assertEquals(dto, captured);
	}

	@Test
	@DisplayName("PUT /api/v1/user/{id} -> should propagate service exception")
	void updateProfile_ShouldPropagateException() {
		Long id = 8L;
		UserDto dto = sampleUserDto();

		org.mockito.Mockito.doThrow(new ResourceException(HttpStatus.FORBIDDEN, ErrorMessageConstant.PROFILE_UPDATE_FORBIDDEN))
				.when(mockUserService).updateUser(org.mockito.Mockito.eq(id), org.mockito.Mockito.any(UserDto.class));

		Assertions.assertThrows(ResourceException.class, () -> userController.updateProfile(id, dto));
	}

	@Test
	@DisplayName("PUT /api/v1/user/{id} -> should allow partial updates (some fields null)")
	void updateProfile_ShouldAllowPartialFields() {
		Long id = 15L;
		UserDto partialDto = UserDto.builder().email(null).firstName("OnlyFirstName").lastName(null).phoneNumber(null)
				.build();

		userController.updateProfile(id, partialDto);

		verify(mockUserService).updateUser(org.mockito.Mockito.eq(id), userDtoCaptor.capture());
		UserDto captured = userDtoCaptor.getValue();
		Assertions.assertEquals("OnlyFirstName", captured.getFirstName());
		Assertions.assertNull(captured.getEmail());
	}

	@Test
	@DisplayName("PUT /api/v1/user/{id} -> should throw when target user not found")
	void updateProfile_ShouldThrowWhenTargetNotFound() {
		Long id = 1234L;
		UserDto dto = sampleUserDto();

		org.mockito.Mockito.doThrow(new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ENDPOINT_NOT_FOUND))
				.when(mockUserService).updateUser(org.mockito.Mockito.eq(id), org.mockito.Mockito.any(UserDto.class));

		Assertions.assertThrows(ResourceException.class, () -> userController.updateProfile(id, dto));
	}

	@Test
	@DisplayName("GET /api/v1/user/{id} -> should return empty UserDto when service returns empty object")
	void getProfile_ShouldReturnEmptyDto() {
		Long id = 55L;
		UserDto emptyDto = UserDto.builder().build();

		when(mockUserService.getUser(id)).thenReturn(emptyDto);

		UserDto result = userController.getProfile(id);

		Assertions.assertNotNull(result);
		Assertions.assertNull(result.getEmail());
		Assertions.assertNull(result.getFirstName());
	}

}
