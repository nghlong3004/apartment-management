package vn.io.nghlong3004.apartment_management.repository;

import java.util.List;
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

	@Select("""
			SELECT *
			FROM floor
			WHERE LOWER(name) = LOWER(#{name})
			""")
	Optional<Floor> findByName(String name);

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

	@Select("""
			    SELECT 1
			    FROM floor
			    WHERE id = #{floorId}
			""")
	Optional<Boolean> floorExists(Long floorId);

	@Update("""
			    UPDATE floor
			       SET room_count = room_count + 1,
			           updated = NOW()
			     WHERE id = #{floorId}
			""")
	int incrementRoomCount(Long floorId);

	@Update("""
			    UPDATE floor
			       SET room_count = GREATEST(room_count - 1, 0),
			           updated = NOW()
			     WHERE id = #{floorId}
			""")
	int decrementRoomCount(Long floorId);

	@Select("""
			    SELECT 1
			    FROM floor
			    WHERE LOWER(name) = LOWER(#{name})
			""")
	Optional<Boolean> existsByName(String name);

	@Select("SELECT COUNT(*) FROM floor")
	long countAll();

	@Select("""
			    SELECT *
			      FROM floor
			     ORDER BY ${orderBy}
			     LIMIT #{limit} OFFSET #{offset}
			""")
	List<Floor> findPage(String orderBy, int limit, int offset);

}
