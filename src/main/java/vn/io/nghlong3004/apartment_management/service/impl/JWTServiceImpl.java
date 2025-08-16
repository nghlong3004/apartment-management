package vn.io.nghlong3004.apartment_management.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
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

	private SecretKey secretKey;
	private JwtParser parser;

	@PostConstruct
	void init() {
		byte[] keyBytes = jwtSecretKey.getBytes(StandardCharsets.UTF_8);
		if (keyBytes.length < 32) {
			throw new IllegalStateException("jwt.secret.key must be at least 256 bits (32 bytes) for HS256");
		}
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
		this.parser = Jwts.parser().requireIssuer(issuer).clockSkewSeconds(clockSkewSeconds).verifyWith(secretKey)
				.build();
		log.info("JWTService initialized. issuer={}, clockSkewSeconds={}", issuer, clockSkewSeconds);
	}

	@Override
	public String generateAccessToken(Long userId, Object role) {
		List<String> roles = normalizeRoles(role);
		Date now = new Date();
		Date exp = new Date(now.getTime() + accessTokenExpirationMs);

		String token = Jwts.builder().issuer(issuer).subject(String.valueOf(userId)).issuedAt(now).expiration(exp)
				.claim("roles", roles).signWith(secretKey).compact();

		log.debug("Generated access token for userId={}, roles={}", userId, roles);
		return token;
	}

	@Override
	public boolean isValid(String token) {
		try {
			validateOrThrow(token);
			return true;
		} catch (JwtException e) {
			log.warn("Invalid JWT: {}", e.getMessage());
			return false;
		}
	}

	@Override
	public void validateOrThrow(String token) throws JwtException {
		parser.parseSignedClaims(token);
	}

	@Override
	public Long getUserId(String token) {
		Claims c = parseClaimsOrThrow(token);
		String sub = c.getSubject();
		if (sub == null) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_ACCESS_TOKEN);
		}
		try {
			return Long.valueOf(sub);
		} catch (NumberFormatException e) {
			throw new ResourceException(HttpStatus.BAD_REQUEST, ErrorMessageConstant.INVALID_ACCESS_TOKEN);
		}
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities(String token) {
		Claims c = parseClaimsOrThrow(token);
		List<String> roles = extractRoles(c);
		return roles.stream().map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r).map(SimpleGrantedAuthority::new)
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public Date getExpiration(String token) {
		return parseClaimsOrThrow(token).getExpiration();
	}

	@Override
	public Claims getClaims(String token) {
		try {
			return parseClaimsOrThrow(token);
		} catch (JwtException e) {
			return null;
		}
	}

	private Claims parseClaimsOrThrow(String token) throws JwtException {
		Jws<Claims> jws = parser.parseSignedClaims(token);
		return jws.getPayload();
	}

	private List<String> normalizeRoles(Object role) {
		if (role == null)
			return List.of();
		if (role instanceof String s)
			return List.of(s);
		if (role instanceof Enum<?> e)
			return List.of(e.name());
		if (role instanceof Collection<?> col) {
			return col.stream().map(String::valueOf).toList();
		}
		return List.of(String.valueOf(role));
	}

	private List<String> extractRoles(Claims claims) {
		Object raw = claims.get("roles");
		if (raw == null)
			return List.of();

		if (raw instanceof Collection<?> col) {
			return col.stream().map(String::valueOf).toList();
		}
		if (raw instanceof String s) {
			String[] parts = s.split("[,\\s]+");
			return Arrays.stream(parts).filter(p -> !p.isBlank()).toList();
		}
		return List.of(String.valueOf(raw));
	}
}
