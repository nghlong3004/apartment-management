package vn.io.nghlong3004.apartment_management.constant;

public final class ErrorMessageConstant {

	// Server errors
	public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred. Please try again later.";
	public static final String TOKEN_GENERATION_FAILED = "Failed to generate token.";

	// Authentication
	public static final String INVALID_CREDENTIALS = "Invalid email or password.";
	public static final String INVALID_ACCESS_TOKEN = "Invalid or malformed access token.";
	public static final String INVALID_REFRESH_TOKEN = "Invalid or malformed refresh token.";
	public static final String ACCESS_TOKEN_EXPIRED = "Access token has expired. Please sign in again.";
	public static final String REFRESH_TOKEN_EXPIRED = "Refresh token has expired. Please sign in again.";
	public static final String ACCOUNT_INACTIVE = "Account is inactive.";
	public static final String PROFILE_UPDATE_FORBIDDEN = "You do not have permission to change this profile.";
	public static final String FORBIDDEN = "You do not have permission to perform this action.";
	public static final String SELF_DELETE_FORBIDDEN = "You cannot delete your own account.";
	public static final String USER_NOT_FOUND_WITH_ID = "User not found.";

	// Room
	public static final String ROOM_NOT_FOUND = "Room does not exist.";
	public static final String ROOM_ALREADY_RESERVED = "Room is already reserved or sold.";
	public static final String ROOM_MOVE_NOT_ALLOWED = "Cannot move to a reserved room.";
	public static final String MOVE_TO_OWN_ROOM_NOT_ALLOWED = "Cannot move to your own room.";
	public static final String ROOM_ALREADY_NAME = "Room name already exists in this floor.";

	// Floor
	public static final String FLOOR_NOT_FOUND = "Floor not found.";
	public static final String UNABLE_TO_CREATE_FLOOR = "Unale to create floor.";
	public static final String FLOOR_STILL_HAS_ROOMS = "Floor still has rooms.";
	public static final String FLOOR_NAME_ALREADY_EXISTS = "Floor name already exists.";

	// Request
	public static final String PENDING_REQUEST_EXISTS = "You already have a pending request.";
	public static final String USER_PENDING_REQUEST = "You already has a pending request.";
	public static final String PERSON_PENDING_REQUEST = "That person already has a pending request.";

	// Resource
	public static final String ENDPOINT_NOT_FOUND = "Endpoint not found.";
	public static final String EMAIL_ALREADY_EXISTS = "Email is already in use.";
	public static final String DATA_CONFLICT = "Data conflict or constraint violation.";
	public static final String ID_NOT_FOUND = "ID not found.";

	// HTTP
	public static final String UNSUPPORTED_MEDIA_TYPE = "Unsupported Content-Type.";
	public static final String MALFORMED_REQUEST_BODY_MESSAGE = "Invalid request body format. Please check the structure and data types.";

	// Admin
	public static final String ALREADY_A_MANAGER = "This user is already a manager.";
	public static final String USER_NOT_IN_FLOOR = "User does not belong to this floor.";
	public static final String NOT_MANAGER = "This user is not a manager.";
	public static final String USER_BANNED = "This user has been banned.";
	public static final String OCCUPIED_ROOM = "The room was occupied.";
	public static final String MANAGER_DIFFERENT_ROOM = "User is a manager of a different floor.";
	public static final String NOT_OWNER_ROOM = "This user is not the owner of the room.";
	public static final String MANAGER_ROOM = "User is a manager of floor.";
	public static final String MANAGER_FLOOR = "This floor has a manager.";
	public static final String NOT_MANAGER_FLOOR = "This user is not the manager of this floor.";

	private ErrorMessageConstant() {

	}
}
