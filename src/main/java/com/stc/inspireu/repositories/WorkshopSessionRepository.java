package com.stc.inspireu.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectId;
import com.stc.inspireu.models.WorkshopSession;

@Transactional
public interface WorkshopSessionRepository
		extends PagingAndSortingRepository<WorkshopSession, Long>, JpaSpecificationExecutor<WorkshopSession> {

	@Query(value = "select o from WorkshopSession o where o.academyRoom.id = :academyRoomId AND (o.startup is null OR o.startup.id = :startupId) AND o.id NOT IN (select c.refWorkshopSession from WorkshopSession c where o.academyRoom.id = :academyRoomId and c.startup.id = :startupId)")
	List<WorkshopSession> getByStartup_IdAndAcademyRoom_Id(@Param("startupId") Long startupId,
			@Param("academyRoomId") Long academyRoomId);

	List<WorkshopSession> findByStartup_IdAndAcademyRoom_IdAndRefWorkshopSession_Id(Long startupId, Long academyRoomId,
			Long workshopSessionId);

	WorkshopSession findByIdAndAcademyRoom_IdAndStartupIdIsNull(Long id, Long academyRoomId);

	WorkshopSession findByIdAndAcademyRoom_IdAndStartup_Id(Long workshopSessionId, Long academyRoomId, Long id);

	Optional<WorkshopSession> findByIdAndStartup_Id(Long workshopSessionId, Long startupId);

	List<WorkshopSession> findByNameIgnoreCaseAndAcademyRoom_Id(String workShopName, Long academyRoomId);

    boolean existsByNameIgnoreCaseAndAcademyRoom_Id(String workShopName, Long academyRoomId);

	List<WorkshopSession> findByRefWorkshopSession_Id(Long workshopSessionId);

	List<WorkshopSession> findByAcademyRoom_Id(Long academyRoomId);

	Optional<WorkshopSession> findByIdAndAndStartupIdIsNull(Long workshopSessionId);

	Optional<WorkshopSession> findByIdAndAndStartup_Id(Long workshopSessionId, Long id);

	@Query(value = "select o from WorkshopSession o where ((o.academyRoom.id = :refAcademyRoomId and o.startup is null and statusPublish = :status) or (o.academyRoom.id = :academyRoomId and o.startup.id = :startupId)) and o.id NOT IN (select c.refWorkshopSession from WorkshopSession c where c.academyRoom.id = :academyRoomId and c.startup.id = :startupId)")
	List<WorkshopSession> getByStartupAcademyRoomId(@Param("startupId") Long startupId,
			@Param("refAcademyRoomId") Long refAcademyRoomId, @Param("academyRoomId") Long academyRoomId,
			@Param("status") String status);

	Page<WorkshopSession> findByIdInAndAcademyRoom_Id(Set<Long> workshopSessionIds, Long academyRoomId,
			Pageable paging);

	@Query(value = "select o from WorkshopSession o where ((o.academyRoom.id = :academyRoomId and o.startup is null))")
	Page<WorkshopSession> getWorkshpSessions(@Param("academyRoomId") Long academyRoomId, Pageable paging);

//    mark card
	List<WorkshopSession> findByStartup_IdAndAcademyRoom_Id(Long startupId, Long academyRoomId);

	List<WorkshopSession> findByAcademyRoom_IdAndAndStartupIdIsNull(Long academyRoomId);

	Page<WorkshopSession> findByAcademyRoom_IdAndStatusNot(Long academyRoomId, String string, Pageable paging);

	@Query("select u.id from WorkshopSession u where u.academyRoom.id = :academyRoomId and u.status = :status and u.statusPublish = :statusPublish and u.startup is null")
	Set<Long> getAllWorkshopSessionIdsByAcademyRoomId(Long academyRoomId, String status, String statusPublish);

	Set<ProjectId> findByAcademyRoom_IdAndStatusPublishAndStartupIsNull(Long academyRoomId, String string);

	WorkshopSession findByIdAndStartupIdIsNull(Long workshopSessionId);

	List<WorkshopSession> findByAcademyRoom_IdAndStartupIdIsNull(Long academyRoomId);

	List<WorkshopSession> findByAcademyRoom_IdAndSessionStartGreaterThanEqualAndSessionEndLessThanEqualAndStartupIdIsNull(
			Long academyRoomId, Date start, Date end);

}
