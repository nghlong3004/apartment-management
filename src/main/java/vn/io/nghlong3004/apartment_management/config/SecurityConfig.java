package vn.io.nghlong3004.apartment_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import lombok.RequiredArgsConstructor;
import vn.io.nghlong3004.apartment_management.security.CustomAccessDeniedHandler;
import vn.io.nghlong3004.apartment_management.security.JWTAuthenticationFilter;
import vn.io.nghlong3004.apartment_management.security.JwtAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JWTAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint authenticationEntryPoint;
	private final CustomAccessDeniedHandler accessDeniedHandler;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

		CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();

		httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.csrf(csrf -> csrf.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
						.ignoringRequestMatchers("/api/v1/**")
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/v1/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/user/{id}").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/floor/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/floor/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/v1/floor/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/v1/floor/**").hasRole("ADMIN").anyRequest()
						.authenticated())
				.exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return httpSecurity.build();
	}

}
