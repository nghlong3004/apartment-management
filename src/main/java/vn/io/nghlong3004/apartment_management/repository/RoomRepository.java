package vn.io.nghlong3004.apartment_management.repository;

import java.util.List;
import java.util.Optional;

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

}
