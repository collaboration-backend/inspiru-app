package com.stc.inspireu.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.AcademyRoom;

@Transactional
public interface AcademyRoomRepository
		extends PagingAndSortingRepository<AcademyRoom, Long>, JpaSpecificationExecutor<AcademyRoom> {
	List<AcademyRoom> findByStartup_IdAndStatus(Long id, String academyRoomStatus);

	@Query(value = "select o from AcademyRoom o where o.status = :academyRoomStatus AND o.id NOT IN (select c.refAcademyRoom from AcademyRoom c where c.startup.id = :startupId)")
	List<AcademyRoom> getByStatusNew(@Param("startupId") Long startupId,
			@Param("academyRoomStatus") String academyRoomStatus);

	@Query(value = "select o from AcademyRoom o where o.startup.id = :startupId and o.refAcademyRoom.id = :academyRoomId")
	List<AcademyRoom> getByStartupIdAndRefAcademyRoom(@Param("startupId") Long startupId,
			@Param("academyRoomId") Long academyRoomId);
	Page<AcademyRoom> findAll(Specification<AcademyRoom> spec, Pageable pageable);

	List<AcademyRoom> findByRefAcademyRoom_Id(Long academyRoomId);

    boolean existsByRefAcademyRoom_Id(Long academyRoomId);

	List<AcademyRoom> findByNameIgnoreCase(String academyRoomName);

	List<AcademyRoom> findByNameIgnoreCaseAndIntakeProgram_Id(String academyRoomName, Long intakeProgramId);

	Optional<AcademyRoom> findByIdAndStartup_Id(Long academyRoomId, Long id);

	Optional<AcademyRoom> findByStartup_IdAndRefAcademyRoom_Id(Long id, Long academyRoomId);

	Optional<AcademyRoom> findByIdAndStatusPublish(Long academyRoomId, String string);

	Page<AcademyRoom> findByIntakeProgramId(Long id, Pageable pageable);

	Optional<AcademyRoom> findByIdAndStartupIdIsNull(Long academyRoomId);

	// mark cards
	List<AcademyRoom> findByIntakeProgram_IdAndStartupIdIsNull(Long intakeProgramId, Pageable pageable);

	Optional<AcademyRoom> findByIdAndIntakeProgram_IdAndStartupIdIsNull(Long academyRoomId, Long intakeProgramId);

	List<AcademyRoom> findByIntakeProgram_IdAndStartupIdIsNullAndIdNotIn(Long intakeProgramId,
			List<Long> academyRoomIds, Pageable pageable);

	long countByIntakeProgram_IdAndStartupIdIsNullAndIdNotIn(Long intakeProgramId, List<Long> academyRoomIds);

	long countByIntakeProgram_IdAndStartupIdIsNull(Long intakeProgramId);

	List<AcademyRoom> findByIntakeProgram_IdAndStartupIdIsNull(Long intakeProgramId);

	Page<AcademyRoom> findBySessionEndLessThan(Date date, Pageable paging);

	Page<AcademyRoom> findByIdInAndSessionEndLessThan(Set<Long> academyRoomIds, Date date, Pageable paging);

	Page<AcademyRoom> findByStatusNotAndSessionEndBefore(String string, Date date, Pageable paging);

	Page<AcademyRoom> findByIntakeProgram_IdAndStatusNotAndSessionEndBefore(Long intakeProgramId, String string,
			Date date, Pageable paging);

	Page<AcademyRoom> findByIntakeProgram_IdAndStatusNot(Long intakeProgramId, String string, Pageable paging);

	Page<AcademyRoom> findByIntakeProgram_IdAndStatusNotAndSessionEndAfter(Long intakeProgramId, String string,
			Date date, Pageable paging);

	@Query(value = "select o from AcademyRoom o where o.intakeProgram.id = :intakeId and o.status = :academyRoomStatus AND o.sessionEnd > :now  AND o.id NOT IN (select c.refAcademyRoom from AcademyRoom c where c.startup.id = :startupId)")
	List<AcademyRoom> getByStatusNewAfterNow(@Param("intakeId") Long intakeId, @Param("startupId") Long startupId,
			@Param("academyRoomStatus") String academyRoomStatus, @Param("now") Date now);

	List<AcademyRoom> findByStartup_IdAndStatusAndSessionEndGreaterThan(Long id, String academyRoomStatus, Date date);

	List<AcademyRoom> findByStartup_IdAndSessionEndLessThan(Long id, Date date);

	Page<AcademyRoom> findByStartupIsNullAndRefAcademyRoomIsNullAndSessionEndLessThan(Date date, Pageable paging);

	@Query("select i FROM AcademyRoom i WHERE i.status IN (:s1, :s2) AND i.sessionEnd > :now AND i.startup is null AND i.refAcademyRoom is null AND i.id NOT IN (select c.refAcademyRoom from AcademyRoom c where c.sessionEnd > :now AND c.startup is not null AND c.refAcademyRoom is not null)")
	Page<AcademyRoom> superAdminAcademyroomsNew(@Param("s1") String s1, @Param("s2") String s2, @Param("now") Date now,
			Pageable paging);

	@Query("select i FROM AcademyRoom i WHERE i.sessionEnd > :now AND i.intakeProgram is not null AND i.id IN (select c.refAcademyRoom from AcademyRoom c where c.sessionEnd > :now AND c.startup is not null AND c.refAcademyRoom is not null)")
	Page<AcademyRoom> superAdminAcademyroomsInprogress(@Param("now") Date now, Pageable paging);

	Page<AcademyRoom> findByIdInAndStartupIsNullAndRefAcademyRoomIsNullAndSessionEndLessThan(Set<Long> academyRoomIds,
			Date date, Pageable paging);

	@Query("select i FROM AcademyRoom i WHERE i.id in :academyRoomIds and i.status IN (:s1, :s2) AND i.sessionEnd > :now AND i.startup is null AND i.refAcademyRoom is null AND i.id NOT IN (select c.refAcademyRoom from AcademyRoom c where c.sessionEnd > :now AND c.startup is not null AND c.refAcademyRoom is not null)")
	Page<AcademyRoom> superAdminAcademyroomsNewIdIn(@Param("s1") String s1, @Param("s2") String s2,
			@Param("now") Date now, @Param("academyRoomIds") Set<Long> academyRoomIds, Pageable paging);

	@Query("select i FROM AcademyRoom i WHERE i.id in :academyRoomIds and i.sessionEnd > :now AND i.intakeProgram is not null AND i.id IN (select c.refAcademyRoom from AcademyRoom c where c.sessionEnd > :now AND c.startup is not null AND c.refAcademyRoom is not null)")
	Page<AcademyRoom> superAdminAcademyroomsInprogressIdIn(@Param("now") Date now,
			@Param("academyRoomIds") Set<Long> academyRoomIds, Pageable paging);

	long countByStartup_IdAndSessionEndLessThan(Long id, Date date);

	List<AcademyRoom> findByIntakeProgram_IdAndName(Long intakeProgramId, String name);

    boolean existsByIntakeProgram_IdAndName(Long intakeProgramId, String name);

	List<AcademyRoom> findByStartupId(Long startUpId);

	Page<AcademyRoom> findByIntakeProgramIdAndStatusPublish(Long intakeProgramId, String string, Pageable paging);

	@Query("select u.id from AcademyRoom u where u.refAcademyRoom.id = :academyRoomId and u.startup.id = :startupId")
	Set<Long> getAllAcademyRoomIdsByRefAcademyRoomIdAndStartup(Long academyRoomId, Long startupId);

}
