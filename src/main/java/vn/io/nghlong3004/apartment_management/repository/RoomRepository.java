package vn.io.nghlong3004.apartment_management.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import vn.io.nghlong3004.apartment_management.model.Room;

@Mapper
public interface RoomRepository {

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
	public Optional<Room> findRoomByFloorIdAndRoomId(Long floorId, Long roomId);

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
			SELECT *
			 FROM  room
			 WHERE floor_id = #{floorId}
			""")
	public List<Room> findAllRoomsByFloorId(Long floorId);

	@Insert("""
			    INSERT INTO room (floor_id, user_id, name, status, created, updated)
			    VALUES (#{floorId}, #{userId}, #{name},
			            #{status}::room_status, NOW(), NOW())
			""")
	void insert(Room room);

	@Select("""
			    SELECT 1
			    FROM room
			    WHERE floor_id = #{floorId} AND LOWER(name) = LOWER(#{name})
			""")
	Optional<Boolean> existsByFloorIdAndName(Long floorId, String name);

	@Select("""
			    SELECT 1
			      FROM room
			     WHERE floor_id = #{floorId}
			       AND LOWER(name) = LOWER(#{name})
			       AND id <> #{roomId}
			""")
	Optional<Boolean> existsByFloorIdAndNameExcludingId(Long floorId, String name, Long roomId);

	@Delete("""
			    DELETE FROM room
			    WHERE id = #{roomId} AND floor_id = #{floorId}
			""")
	void deleteByIdAndFloorId(Long roomId, Long floorId);

	@Select("""
			    SELECT *
			      FROM room
			     WHERE floor_id = #{floorId}
			       AND LOWER(name) = LOWER(#{name})
			""")
	Optional<Room> findByFloorIdAndName(Long floorId, String name);

	@Select("""
			    SELECT COUNT(id) FROM room WHERE floor_id = #{floorId}
			""")
	long countByFloorId(Long floorId);

	@Select("""
			    SELECT *
			      FROM room
			     WHERE floor_id = #{floorId}
			     ORDER BY ${orderBy}
			     LIMIT #{limit} OFFSET #{offset}
			""")
	List<Room> findPageByFloorId(Long floorId, String orderBy, int limit, int offset);

}
