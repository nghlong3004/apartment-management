package vn.io.nghlong3004.apartment_management.repository;

import java.time.Instant;
import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import vn.io.nghlong3004.apartment_management.model.RefreshToken;

@Mapper
public interface RefreshTokenRepository {
	@Select("""
			SELECT id, user_id, token, expiry_date AS expiryDate FROM refresh_token WHERE token = #{token}
			""")
	public Optional<RefreshToken> findByToken(String token);

	@Delete("""
			DELETE FROM refresh_token WHERE user_id = #{userId}
			""")
	public void deleteByUserId(Long userId);

	@Insert("""
			INSERT INTO refresh_token(user_id, token, expiry_date) VALUES(#{userId}, #{token}, #{expiryDate})
			""")
	public void save(Long userId, String token, Instant expiryDate);

}
