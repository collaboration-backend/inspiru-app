package com.stc.inspireu.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectId;
import com.stc.inspireu.models.Assignment;

@Transactional
public interface AssignmentRepository
		extends PagingAndSortingRepository<Assignment, Long>, JpaSpecificationExecutor<Assignment> {

	@Query(value = "select o from Assignment o where o.workshopSession.id = :workshopSessionId AND (o.submittedStartup is null OR o.submittedStartup.id = :startupId) AND o.id NOT IN (select c.refAssignment from Assignment c where o.workshopSession.id = :workshopSessionId and c.submittedStartup.id = :startupId)")
	List<Assignment> getByAssignmentsByWorkshopSessionIdAndStartupId(@Param("workshopSessionId") Long workshopSessionId,
			@Param("startupId") Long startupId);

	Optional<Assignment> findByIdAndWorkshopSession_Id(Long assignmentId, Long workshopSessionId);

	@Query(value = "select o from Assignment o where ((o.workshopSession.id = :refWorkshopSessionId and o.submittedStartup is null and status = 'PUBLISHED') or (o.workshopSession.id = :workshopSessionId and o.submittedStartup.id = :startupId)) and o.id NOT IN (select c.refAssignment from Assignment c where c.workshopSession.id = :workshopSessionId and c.submittedStartup.id = :startupId)")
	List<Assignment> getStartupWorkshopSessionAssignments(@Param("startupId") Long startupId,
			@Param("refWorkshopSessionId") Long refWorkshopSessionId,
			@Param("workshopSessionId") Long workshopSessionId);

	Assignment findByRefAssignment_IdAndWorkshopSession_IdAndSubmittedStartup_Id(Long assignmentId,
			Long workshopSessionId, Long id);

	Assignment findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNull(
			Long assignmentId, Long workshopSessionId, Long academyRoomId);

	long countByRefAssignment_IdAndRefAssignment_WorkshopSession_IdAndRefAssignment_WorkshopSession_AcademyRoom_Id(
			Long refAssignmentId, Long workshopSessionId, Long academyRoomId);

	Optional<Assignment> findByIdAndRefAssignment_WorkshopSession_IdAndRefAssignment_WorkshopSession_AcademyRoom_Id(
			Long assignmentId, Long workshopSessionId, Long academyRoomId);

	Assignment findByIdAndWorkshopSession_IdAndSubmittedStartup_Id(Long assignmentId, Long workshopSessionId, Long id);

	Page<Assignment> findAll(Specification<Assignment> spec, Pageable pageable);

	Assignment findByIdAndRefAssignment_WorkshopSession_IdAndRefAssignment_WorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNotNull(
			Long assignmentId, Long workshopSessionId, Long academyRoomId);

	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM Assignment e WHERE e.id = :assignmentId")
	void removeById(@Param("assignmentId") Long assignmentId);

	// mark card
	List<Assignment> findByWorkshopSession_IdAndSubmittedStartupIsNullAndStatus(Long workshopSessionId, String status);

	List<Assignment> findByWorkshopSession_IdAndSubmittedStartup_IdAndStatusIn(Long workshopSessionId, Long startupId,
			List<String> status);

	long countBySubmittedStartupId(long submittedStartupId);

	@Query("SELECT COUNT(o) FROM Assignment o where o.submittedStartup is null and o.workshopSession.academyRoom.id = :academyRoomId")
	long countByTotalAssignments(Long academyRoomId);

	@Query("SELECT COUNT(o) FROM Assignment o where o.submittedStartup.id = :startupId and o.workshopSession.academyRoom.id = :academyRoomId")
	long countByTotalAssignmentsAndStartupId(Long startupId, Long academyRoomId);

	long countByWorkshopSessionIdInAndStatusAndSubmittedStartupIsNull(Set<Long> wsIds, String string);

	Set<ProjectId> findByWorkshopSessionIdInAndStatusAndSubmittedStartupIsNull(Set<Long> wsIds, String string);

	long countByRefAssignmentIdInAndReview1StatusAndReview2StatusAndSubmittedStartup_Id(Set<Long> collect,
			String string, String string2, Long id);

}
