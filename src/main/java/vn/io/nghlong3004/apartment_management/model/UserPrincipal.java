package vn.io.nghlong3004.apartment_management.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

	private final Long id;
	private final String password;
	private final Collection<? extends GrantedAuthority> authorities;

	public static UserPrincipal from(User user) {
		String roleName = user.getRole() == null ? "USER" : user.getRole().name();
		String springRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

		return new UserPrincipal(user.getId(), user.getPassword(), List.of(new SimpleGrantedAuthority(springRole)));
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return null;
	}

}
