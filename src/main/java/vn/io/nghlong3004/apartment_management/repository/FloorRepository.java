package vn.io.nghlong3004.apartment_management.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import vn.io.nghlong3004.apartment_management.model.RequestStatus;
import vn.io.nghlong3004.apartment_management.model.RequestType;

@Mapper
public interface FloorRepository {

	@Insert("""
			INSERT INTO
				   floor_request (user_id, floor_id, room_id, type, status)
			VALUES
				   (#{userId}, #{floorId}, #{roomId}, #{type}::request_type, #{status}::request_status)
			""")
	public void createRequest(Long userId, Long floorId, Long roomId, RequestType type, RequestStatus status);

	@Select("""
			SELECT 1
			FROM floor_request
			     WHERE user_id = #{userId}
			     AND type = #{requestType, jdbcType=OTHER}
			     AND status = 'PENDING'
			""")
	public Optional<Boolean> existsPendingRequest(Long userId, RequestType requestType);

}
