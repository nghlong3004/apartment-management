package vn.io.nghlong3004.apartment_management.constants;

public final class ApplicationConstants {

	public static final String JWT_SECRET_KEY = "jwt.secret";
	public static final String JWT_SECRET_DEFAULT_VALUE = "jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4";
	public static final String JWT_HEADER = "Authorization";
	public static final String JWT_ISSUER = "nghlong3004";
	public static final String JWT_TOKEN_PREFIX = "Bearer ";

	public static final Long EXPIRY_DATE_REFRESH_TOKEN_MS = 604800000L;
	public static final Long EXPIRY_DATE_ACCESS_TOKEN_MS = 900000L;

}
