package vn.io.nghlong3004.apartment_management.service;

import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;

import io.jsonwebtoken.Claims;

public interface JWTService {

	String generateAccessToken(Long userId, Object roles);

	boolean isValid(String token);

	void validateOrThrow(String token);

	Long getUserId(String token);

	Collection<? extends GrantedAuthority> getAuthorities(String token);

	Date getExpiration(String token);

	Claims getClaims(String token);
}
