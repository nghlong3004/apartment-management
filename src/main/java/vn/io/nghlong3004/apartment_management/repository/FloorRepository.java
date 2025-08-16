package vn.io.nghlong3004.apartment_management.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import vn.io.nghlong3004.apartment_management.model.Floor;
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
	void createRequest(Long userId, Long floorId, Long roomId, RequestType type, RequestStatus status);

	@Select("""
			SELECT 1
			FROM floor_request
			     WHERE user_id = #{userId}
			     AND type = #{requestType, jdbcType=OTHER}
			     AND status = 'PENDING'
			""")
	Optional<Boolean> existsPendingRequest(Long userId, RequestType requestType);

	@Select("""
			SELECT *
			FROM floor
			WHERE id = #{floorId}
			""")
	Optional<Floor> findById(Long floorId);

	@Update("""
			    UPDATE floor
			    SET name = #{name},
			        manager_id = #{managerId},
			        room_count = #{roomCount},
			        updated = NOW()
			    WHERE id = #{id}
			""")
	void updateFloor(Floor existingFloor);

	@Insert("""
			    INSERT INTO floor (name, manager_id, room_count, created, updated)
			    VALUES (#{name}, #{managerId}, #{roomCount}, NOW(), NOW())
			""")
	void insert(Floor floor);

	@Delete("""
			    DELETE FROM floor
			    WHERE id = #{floorId}
			""")
	void deleteById(Long floorId);

	@Select("""
			    SELECT COUNT(*)
			    FROM room
			    WHERE floor_id = #{floorId}
			""")
	long countRoomsByFloorId(Long floorId);

}
