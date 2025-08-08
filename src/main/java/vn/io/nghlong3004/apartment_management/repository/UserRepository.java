package vn.io.nghlong3004.apartment_management.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import vn.io.nghlong3004.apartment_management.model.User;

@Mapper
public interface UserRepository {

	@Select("""
			SELECT 1 FROM floor_user WHERE email = #{email}
			""")
	public Optional<Boolean> existsByEmail(String email);

	@Insert("""
			INSERT INTO floor_user(first_name, last_name, email, password_hash, phone_number, role, status)
			VALUES(#{firstName}, #{lastName}, #{email}, #{password}, #{phoneNumber}, #{role}::user_role, #{status}::user_status)
			""")
	public void save(User user);

	@Select("""
			SELECT id, first_name AS firstName, last_name AS lastName, email, password_hash AS password, phone_number AS phoneNumber,
			role, status, floor_id, created, updated FROM floor_user WHERE email = #{email}
			""")
	public Optional<User> findByEmail(String email);

	@Select("""
			SELECT password_hash AS password FROM floor_user WHERE email = #{email}
			""")
	public Optional<String> findPasswordByEmail(String email);

	@Select("""
			SELECT id, first_name AS firstName, last_name AS lastName, email, password_hash AS password, phone_number AS phoneNumber,
			role, status, floor_id, created, updated FROM floor_user WHERE id = #{userId}
			""")
	public Optional<User> findById(Long userId);

}
