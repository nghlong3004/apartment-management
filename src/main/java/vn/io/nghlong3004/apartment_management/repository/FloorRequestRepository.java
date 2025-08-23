package vn.io.nghlong3004.apartment_management.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import vn.io.nghlong3004.apartment_management.model.FloorRequest;

@Mapper
public interface FloorRequestRepository {

	@Insert("""
				INSERT INTO floor_request (requester_id, requester_room_id, counterpart_id,
							counterpart_room_id, approver_id, status,
							closed_reason, created, updated)
				VALUES (#{requesterId}, #{requesterRoomId}, #{counterpartId},
				 		#{counterpartRoomId}, #{approverId}, #{status}::request_status,
				 		#{closedReason}, NOW(), NOW())
			""")
	void insert(FloorRequest floorRequest);

	@Select("""
				SELECT id, requester_id AS requesterId,
					requester_room_id AS requesterRoomId, counterpart_id AS counterpartId,
					counterpart_room_id AS counterpartRoomId, approver_id AS approverId,
					status, closed_reason, created, updated
				FROM floor_request
				WHERE id = #{id}
			""")
	Optional<FloorRequest> findById(Long id);

	@Select("""
			SELECT EXISTS(
			  SELECT 1
			  FROM floor_request
			  WHERE requester_id = #{id}
			    AND status IN ('PENDING','ACCEPTED')
			  )
			""")
	boolean existsActiveByRequesterId(Long id);

	@Select("""
			SELECT EXISTS(
			  SELECT 1
			  FROM floor_request
			  WHERE counterpart_id = #{id}
			    AND status IN ('PENDING','ACCEPTED')
			  )
			""")
	boolean existsActiveByCounterpartId(Long id);

	@Update("""
				UPDATE floor_request
				SET
					requester_id = #{requesterId},
					requester_room_id = #{requesterRoomId},
					counterpart_id = #{counterpartId},
					counterpart_room_id = #{counterpartRoomId},
					approver_id = #{approverId},
					status = #{status}::request_status,
					closed_reason = #{closedReason},
					updated = NOW()
				WHERE id = #{id}
			""")
	void update(FloorRequest floorRequest);

	@Select("""
				SELECT id, requester_id AS requesterId,
					requester_room_id AS requesterRoomId, counterpart_id AS counterpartId,
					counterpart_room_id AS counterpartRoomId, approver_id AS approverId,
					status, closed_reason, created, updated
				FROM floor_request
			""")
	List<FloorRequest> find();

	@Select("""
			    SELECT id, requester_id AS requesterId,
						requester_room_id AS requesterRoomId, counterpart_id AS counterpartId,
						counterpart_room_id AS counterpartRoomId, approver_id AS approverId,
						status, closed_reason, created, updated
				FROM floor_request
			    ORDER BY ${orderBy}
			    LIMIT #{limit} OFFSET #{offset}
			""")
	List<FloorRequest> findPage(String orderBy, int limit, int offset);

	@Select("""
				SELECT COUNT(*)
				FROM floor_request
			""")
	long countAll();

}
