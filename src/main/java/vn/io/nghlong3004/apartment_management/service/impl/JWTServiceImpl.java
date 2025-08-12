package vn.io.nghlong3004.apartment_management.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.service.JWTService;

@Service
@Slf4j
public class JWTServiceImpl implements JWTService {

	@Value("${jwt.secret.key}")
	private String jwtSecretKey;

	@Value("${jwt.access-token-expiration-ms}")
	private long accessTokenExpirationMs;

	@Value("${jwt.issuer:nghlong3004}")
	private String issuer;

	@Value("${jwt.clock-skew-seconds:60}")
	private long clockSkewSeconds;

	private SecretKey getSecretKey() {
		byte[] keyBytes = jwtSecretKey.getBytes(StandardCharsets.UTF_8);
		if (keyBytes.length < 32) {
			throw new IllegalStateException("jwt.secret.key must be at least 256 bits for HS256");
		}
		return Keys.hmacShaKeyFor(keyBytes);
	}

	@Override
	public String generateAccessToken(Long userId, Object role) {
		log.debug("Generating access token for userId={}, roles={}", userId, role);
		Date now = new Date();
		Date exp = new Date(now.getTime() + accessTokenExpirationMs);

		return Jwts.builder().issuer(issuer).subject(String.valueOf(userId)).issuedAt(now).expiration(exp)
				.claim("roles", role == null ? List.of() : role).signWith(getSecretKey()).compact();
	}

	@Override
	public boolean isValid(String token) {
		try {
			Claims c = parseAndValidate(token);
			log.trace("JWT token is valid");
			return c != null;
		} catch (Exception e) {
			log.warn("Invalid JWT token: {}", e.getMessage());
			return false;
		}
	}

	@Override
	public Long getUserId(String token) {
		Claims c = parseAndValidate(token);
		return (c == null) ? null : Long.valueOf(c.getSubject());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities(String token) {
		Claims c = parseAndValidate(token);
		if (c == null)
			return List.of();
		Object raw = c.get("roles");
		if (raw instanceof Collection<?> col) {
			List<String> roles = col.stream().map(String::valueOf).toList();
			return roles.stream().map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r).map(SimpleGrantedAuthority::new)
					.toList();
		}
		return List.of();
	}

	@Override
	public Date getExpiration(String token) {
		Claims c = parseAndValidate(token);
		return c == null ? null : c.getExpiration();
	}

	@Override
	public Claims getClaims(String token) {
		try {
			return parseAndValidate(token);
		} catch (Exception e) {
			return null;
		}
	}

	private Claims parseAndValidate(String token) {
		JwtParser parser = Jwts.parser().requireIssuer(issuer).clockSkewSeconds(clockSkewSeconds)
				.verifyWith(getSecretKey()).build();

		Jws<Claims> jws = parser.parseSignedClaims(token);
		Claims claims = jws.getPayload();

		return claims;
	}
}
