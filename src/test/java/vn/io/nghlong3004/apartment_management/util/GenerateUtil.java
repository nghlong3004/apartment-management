package vn.io.nghlong3004.apartment_management.util;

import java.util.Random;

public class GenerateUtil {

	public static String generateFirstName() {
		Random random = new Random();
		String firstName = "";
		// random first name
		for (int i = 0, n = Math.abs(random.nextInt()) % 15 + 4; i < n; ++i) {
			firstName += (char) (Math.abs(random.nextInt()) % 26 + 'a');
		}
		return firstName;
	}

	public static String generateLastName() {
		Random random = new Random();
		String lastName = "";
		// random first name
		for (int i = 0, n = Math.abs(random.nextInt()) % 15 + 4; i < n; ++i) {
			lastName += (char) (Math.abs(random.nextInt()) % 26 + 'a');
		}
		return lastName;
	}

	public static String generateEmail() {
		Random random = new Random();
		String email = "";
		// random email
		for (int i = 0, n = Math.abs(random.nextInt()) % 50 + 1; i < n; ++i) {
			email += (char) (Math.abs(random.nextInt()) % 26 + 'a');
		}
		email += "@example.com";
		return email;
	}

	public static String generatePassword() {
		Random random = new Random();

		String password = "";
		// random password
		for (int i = 0, n = Math.abs(random.nextInt()) % 50 + 1; i < n; ++i) {
			password += (char) (Math.abs(random.nextInt()) % 26 + 'a');
		}
		return password;
	}

	public static String generatePhoneNumber() {
		Random random = new Random();

		String phoneNumber = "";
		// random phone number
		for (int i = 0; i < 10; ++i) {
			phoneNumber += (char) (Math.abs(random.nextInt()) % 10 + '0');
		}
		return phoneNumber;
	}
}
