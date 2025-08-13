package vn.io.nghlong3004.apartment_management.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import vn.io.nghlong3004.apartment_management.model.RequestStatus;
import vn.io.nghlong3004.apartment_management.model.RequestType;
import vn.io.nghlong3004.apartment_management.model.Room;

@Mapper
public interface FloorRepository {

	@Select("""
			SELECT id,
				   floor_id    AS floorId,
			       user_id        AS userId,
			       name,
			       status,
			       created,
			       updated
			 FROM  room
			 WHERE floor_id = #{floorId}
			 AND id = #{roomId}
			""")
	public Optional<Room> findByFloorIdAndRoomId(Long floorId, Long roomId);

	@Insert("""
			INSERT INTO
				   floor_request (user_id, floor_id, room_id, type, status)
			VALUES
				   (#{userId}, #{floorId}, #{roomId}, #{type}::request_type, #{status}::request_status)
			""")
	public void createRequest(Long userId, Long floorId, Long roomId, RequestType type, RequestStatus status);

	@Update("""
			UPDATE room
			SET
				   floor_id = #{floorId},
				   user_id = #{userId},
				   name = #{name},
				   status = #{status}::room_status ,
				   updated = NOW()
			WHERE  id = #{id}
			""")
	public void updateRoom(Room room);

	@Select("""
			    SELECT EXISTS (
			        SELECT 1
			        FROM floor_request
			        WHERE user_id = #{userId}
			          AND type = #{requestType, jdbcType=OTHER}
			          AND status = 'PENDING'
			    )
			""")
	public Optional<Boolean> existsPendingRequest(Long userId, RequestType requestType);

}
