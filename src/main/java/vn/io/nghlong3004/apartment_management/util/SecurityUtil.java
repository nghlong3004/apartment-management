package vn.io.nghlong3004.apartment_management.util;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.experimental.UtilityClass;
import vn.io.nghlong3004.apartment_management.model.UserPrincipal;

@UtilityClass
public class SecurityUtil {

	public static Optional<Long> getCurrentUserId() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext == null)
			return Optional.empty();
		Authentication authentication = securityContext.getAuthentication();
		if (authentication == null || !authentication.isAuthenticated())
			return Optional.empty();
		Object p = authentication.getPrincipal();
		if (p instanceof UserPrincipal up && up.getId() != null)
			return Optional.of(up.getId());
		return Optional.empty();
	}

	public static boolean hasRole(String role) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext == null)
			return false;
		Authentication authentication = securityContext.getAuthentication();
		if (authentication == null)
			return false;
		String need = role.startsWith("ROLE_") ? role : "ROLE_" + role;
		return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(need));
	}

}
