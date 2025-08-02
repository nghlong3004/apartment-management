package vn.io.nghlong3004.apartment_management.util;

import java.util.regex.Pattern;

public class ValidationUtil {
	private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

	private ValidationUtil() {

	}

	public static boolean isBlank(String input) {
		return input == null || input.trim().isEmpty();
	}

	public static boolean hasValidLength(String input, int min, int max) {
		if (isBlank(input)) {
			return false;
		}
		int length = input.length();
		return length >= min && length <= max;
	}

	public static boolean isValidEmail(String email) {
		if (isBlank(email)) {
			return false;
		}
		return EMAIL_PATTERN.matcher(email).matches();
	}

	public static boolean isPasswordStrong(String password) {
		if (isBlank(password) || password.length() < 8) {
			return false;
		}
		boolean hasUpper = false;
		boolean hasLower = false;
		boolean hasDigit = false;

		for (char c : password.toCharArray()) {
			if (Character.isUpperCase(c))
				hasUpper = true;
			else if (Character.isLowerCase(c))
				hasLower = true;
			else if (Character.isDigit(c))
				hasDigit = true;
		}
		return hasUpper && hasLower && hasDigit;
	}
}
