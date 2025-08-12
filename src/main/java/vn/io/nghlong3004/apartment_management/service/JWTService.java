package vn.io.nghlong3004.apartment_management.service;

import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;

import io.jsonwebtoken.Claims;

public interface JWTService {

	public String generateAccessToken(Long userId, Object roles);

	public boolean isValid(String token);

	public Long getUserId(String token);

	public Collection<? extends GrantedAuthority> getAuthorities(String token);

	public Date getExpiration(String token);

	public Claims getClaims(String token);
}
