package vn.io.nghlong3004.apartment_management.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.constant.ErrorMessageConstant;
import vn.io.nghlong3004.apartment_management.util.JsonErrorWriter;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		log.warn("Access denied: {}", accessDeniedException.getMessage());
		JsonErrorWriter.write(response, HttpServletResponse.SC_FORBIDDEN, ErrorMessageConstant.PROFILE_UPDATE_FORBIDDEN);
	}
}
