package vn.io.nghlong3004.apartment_management.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import vn.io.nghlong3004.apartment_management.constants.ApplicationConstants;

@Component
@RequiredArgsConstructor
public class JWTTokenProvider {

	private final Environment environment;

	public String generateToken(String email, Object role) {

		return getJWT(email, role);
	}

	public Claims getClaims(String JWT) {
		if (environment != null) {
			SecretKey secretKey = getSecretKey(environment);
			if (secretKey != null) {
				Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(JWT).getPayload();
				return claims;
			}
		}
		return null;
	}

	private String getJWT(String email, Object role) {
		String JWT = ApplicationConstants.JWT_TOKEN_PREFIX;
		if (environment != null) {
			Date now = new Date();
			Date expired = new Date(now.getTime() + ApplicationConstants.EXPIRY_DATE_ACCESS_TOKEN_MS);
			SecretKey secretKey = getSecretKey(environment);
			JWT = Jwts.builder().issuer(ApplicationConstants.JWT_ISSUER).subject(email).claim("email", email)
					.claim("role", role).issuedAt(now).expiration(expired).signWith(secretKey).compact();
		}
		return JWT;
	}

	private SecretKey getSecretKey(Environment environment) {
		String secret = environment.getProperty(ApplicationConstants.JWT_SECRET_KEY);
		SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		return secretKey;
	}
}
