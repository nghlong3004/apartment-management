package vn.io.nghlong3004.apartment_management.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import vn.io.nghlong3004.apartment_management.model.dto.ErrorResponse;

@UtilityClass
public class JsonErrorWriter {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static void write(HttpServletResponse resp, int status, String message) throws IOException {
		resp.setStatus(status);
		resp.setContentType("application/json;charset=UTF-8");
		var body = ErrorResponse.builder().code(status).message(message).build();
		resp.getWriter().write(MAPPER.writeValueAsString(body));
	}
}
