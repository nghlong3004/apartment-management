package vn.io.nghlong3004.apartment_management.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HelperUtil {
	public static final Map<String, String> SORT_WHITELIST = Map.of("id", "id", "name", "name", "managerId",
			"manager_id", "roomCount", "room_count", "created", "created", "updated", "updated");

	public static String normalizeSort(String sort) {
		if (sort == null || sort.isBlank())
			return "id ASC";
		String[] parts = sort.split(",");
		String field = parts[0].trim();
		String dir = (parts.length > 1 ? parts[1].trim() : "asc");

		String column = SORT_WHITELIST.getOrDefault(field, "id");
		String direction = switch (dir.toLowerCase()) {
		case "desc" -> "DESC";
		default -> "ASC";
		};
		return column + " " + direction;
	}

	public static String generateRoomName(int floorNumber, int roomNumber) {
		return String.format("Room %d-%02d", floorNumber, roomNumber);
	}

	public static String generateFloorName(long floorNumber) {
		return "Floor " + floorNumber;
	}

	public static int parseFloorNumber(String floorName) {
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(floorName);

		if (matcher.find()) {
			int number = Integer.parseInt(matcher.group());
			return number;
		}
		return 0;
	}
}
