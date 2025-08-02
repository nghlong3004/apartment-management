package vn.io.nghlong3004.apartment_management.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import vn.io.nghlong3004.apartment_management.model.User;

@Mapper
public interface UserRepository {

	@Select("""
			SELECT COUNT(email) FROM floor_user WHERE email = #{email}
			""")
	public int existsByEmail(String email);

	@Insert("""
			INSERT INTO floor_user(first_name, last_name, email, password_hash, phone_number, role, status)
			VALUES(#{firstName}, #{lastName}, #{email}, #{passwordHash}, #{phoneNumber}, #{role}::user_role, #{status}::user_status)
			""")
	public void save(User user);

}
