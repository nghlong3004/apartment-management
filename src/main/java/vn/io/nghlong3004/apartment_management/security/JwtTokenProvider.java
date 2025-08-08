package vn.io.nghlong3004.apartment_management.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTTokenProvider {

	@Value("${jwt.secret.key}")
	private String JWT_SECRET_KEY;

	@Value("${jwt.access-token-expiration-ms}")
	private String ACCESS_TOKEN_EXPIRATION_MS;

	public String generateToken(String email, Object role) {

		return getJWT(email, role);
	}

	public Claims getClaims(String JWT) {
		SecretKey secretKey = getSecretKey();
		if (secretKey != null) {
			Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(JWT).getPayload();
			return claims;
		}
		return null;
	}

	private String getJWT(String email, Object role) {
		String JWT = "Bearer ";
		Date now = new Date();
		Date expired = new Date(now.getTime() + Long.parseLong(ACCESS_TOKEN_EXPIRATION_MS));
		SecretKey secretKey = getSecretKey();
		JWT = Jwts.builder().issuer("nghlong3004").subject(email).claim("email", email).claim("role", role)
				.issuedAt(now).expiration(expired).signWith(secretKey).compact();
		return JWT;
	}

	private SecretKey getSecretKey() {
		String secret = JWT_SECRET_KEY;
		SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		return secretKey;
	}
}
