package vn.io.nghlong3004.apartment_management.security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.service.JWTService;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

	private final JWTService jwtService;
	private final UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String path = request.getRequestURI();
		if (isPublic(path)) {
			filterChain.doFilter(request, response);
			return;
		}
		log.info("Incoming request path: {}", path);

		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			log.debug("No Authorization header found or it does not start with 'Bearer '");
			filterChain.doFilter(request, response);
			return;
		}
		String token = authHeader.substring(7);
		log.info("Extracted JWT token: {}", token);

		if (jwtService.isValid(token)) {
			Long userId = jwtService.getUserId(token);
			log.info("JWT validated successfully for userId={}", userId);

			UserDetails user = userDetailsService.loadUserByUsername(String.valueOf(userId));
			Collection<? extends GrantedAuthority> authorities = jwtService.getAuthorities(token);
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
					(authorities == null || authorities.isEmpty()) ? user.getAuthorities() : authorities);
			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(auth);
		}
		filterChain.doFilter(request, response);
	}

	private boolean isPublic(String path) {
		return path.startsWith("/api/v1/auth");
	}
}
