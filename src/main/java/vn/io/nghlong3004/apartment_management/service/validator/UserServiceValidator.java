package vn.io.nghlong3004.apartment_management.service.validator;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.RefreshToken;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserStatus;
import vn.io.nghlong3004.apartment_management.repository.UserRepository;
import vn.io.nghlong3004.apartment_management.service.RefreshTokenService;
import vn.io.nghlong3004.apartment_management.util.SecurityUtil;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceValidator {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenService refreshTokenService;

	public void ensureCanUpdateUser(Long targetUserId) {
		Long actorId = SecurityUtil.getCurrentUserId()
				.orElseThrow(() -> new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.ID_NOT_FOUND));
		boolean isAdmin = SecurityUtil.hasRole("ADMIN");
		boolean isSelf = actorId != null && actorId.equals(targetUserId);

		if (!(isAdmin || isSelf)) {
			log.warn("Update user forbidden: actorId={}, targetUserId={}, isAdmin={}, isSelf={}", actorId, targetUserId,
					isAdmin, isSelf);
			throw new ResourceException(HttpStatus.FORBIDDEN, ErrorMessageConstant.PROFILE_UPDATE_FORBIDDEN);
		}
	}

	public void ensureEmailNotExists(String email) {
		boolean exists = userRepository.existsByEmail(email).orElse(false);
		if (exists) {
			log.warn("Register failed: email already exists {}", email);
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.EMAIL_ALREADY_EXISTS);
		}
	}

	public void validateCredentials(String rawPassword, User user) {
		if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
			log.warn("Login failed: invalid password email={}", user.getEmail());
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_CREDENTIALS);
		}
		if (user.getStatus() != UserStatus.ACTIVE) {
			log.warn("Login failed: account inactive email={}", user.getEmail());
			throw new ResourceException(HttpStatus.FORBIDDEN, ErrorMessageConstant.ACCOUNT_INACTIVE);
		}
	}

	public RefreshToken findAndVerifyRefreshToken(String requestRefreshToken) {
		RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken).orElseThrow(() -> {
			log.warn("Refresh failed: token not found");
			return new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_REFRESH_TOKEN);
		});
		refreshTokenService.verifyExpiration(refreshToken);
		return refreshToken;
	}

}
