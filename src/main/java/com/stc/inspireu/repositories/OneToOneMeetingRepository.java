package com.stc.inspireu.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectId;
import com.stc.inspireu.models.OneToOneMeeting;

@Transactional
public interface OneToOneMeetingRepository extends PagingAndSortingRepository<OneToOneMeeting, Long> {

	@Query(value = "select o from OneToOneMeeting o where (o.trainer.id = :trainerId or o.startup.id = :startupId) and ( (o.sessionStart BETWEEN :startDate AND :endDate) or (o.sessionEnd BETWEEN :startDate AND :endDate))")
	List<OneToOneMeeting> getByScheduledMeetings(@Param("startupId") Long startupId, @Param("trainerId") Long trainerId,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	List<ProjectId> findByStartup_IdAndSessionEndGreaterThanEqualAndTrainerIdAndWillOnlineTrue(Long id, Date date,
			Long id2);

	List<ProjectId> findByStartup_IdAndSessionEndGreaterThanEqualAndTrainerIdAndWillOnlineFalse(Long id, Date date,
			Long id2);

	@Query(value = "select o from OneToOneMeeting o where  o.startup.id = :startupId and ( (o.sessionStart <= :startDate AND o.sessionEnd >= :startDate) or (o.sessionStart <= :endDate AND o.sessionEnd >= :endDate))")
	List<OneToOneMeeting> getByStartupScheduledMeetings(@Param("startupId") Long startupId,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	@Query(value = "select o from OneToOneMeeting o where o.trainer.id = :trainerId and ( (o.sessionStart <= :startDate AND o.sessionEnd >= :startDate) or (o.sessionStart <= :endDate AND o.sessionEnd >= :endDate))")
	List<OneToOneMeeting> getByTrainerScheduledMeetings(@Param("trainerId") Long trainerId,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	@Query(value = "select o from OneToOneMeeting o where o.id <> :meetingId and o.startup.id = :startupId and ( (o.sessionStart <= :startDate AND o.sessionEnd >= :startDate) or (o.sessionStart <= :endDate AND o.sessionEnd >= :endDate))")
	List<OneToOneMeeting> getByStartupScheduledMeetingsForNewProposal(@Param("startupId") Long startupId,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("meetingId") Long meetingId);

	@Query(value = "select o from OneToOneMeeting o where o.id <> :meetingId and o.trainer.id = :trainerId and ( (o.sessionStart <= :startDate AND o.sessionEnd >= :startDate) or (o.sessionStart <= :endDate AND o.sessionEnd >= :endDate))")
	List<OneToOneMeeting> getByTrainerScheduledMeetingsForNewProposal(@Param("trainerId") Long trainerId,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("meetingId") Long meetingId);

	List<OneToOneMeeting> findAll();

	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM OneToOneMeeting e WHERE e.id = :assignmentId")
	void removeById(Long assignmentId);

}
