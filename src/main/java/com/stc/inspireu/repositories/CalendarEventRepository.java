package com.stc.inspireu.repositories;

import com.stc.inspireu.models.CalendarEvent;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Transactional
public interface CalendarEventRepository extends PagingAndSortingRepository<CalendarEvent, Long> {

	@Query("select t from CalendarEvent t where t.startup.id = :startupId and month(t.sessionStart) = :month")
	List<CalendarEvent> getCalendarEventsByStartupIdAndMonthAndYear(@Param("startupId") Long startupId,
			@Param("month") int month);

	@Query("select t from CalendarEvent t where t.startup.id = :startupId and month(t.sessionStart) = :month and day(t.sessionStart) = :dt")
	List<CalendarEvent> getCalendarEventsByStartupIdAndDateAndMonthAndYear(@Param("startupId") Long startupId,
			@Param("month") int month, @Param("dt") int dt);

	@Query(value = "select t from CalendarEvent t where t.startup.id = :startupId and t.sessionStart BETWEEN :atStartOfDay AND :atEndOfDay")
	List<CalendarEvent> getCalendarEventsByStartupIdAndDay(@Param("startupId") Long startupId,
			@Param("atStartOfDay") Date atStartOfDay, @Param("atEndOfDay") Date atEndOfDay);

	@Query(value = "select t from CalendarEvent t where t.startup.id = :startupId and t.sessionStart BETWEEN :atStartOfMonth AND :atEndOfMonth")
	List<CalendarEvent> getCalendarEventsByStartupIdAndMonth(@Param("startupId") Long startupId,
			@Param("atStartOfMonth") Date atStartOfMonth, @Param("atEndOfMonth") Date atEndOfMonth);

	@Query("select t from CalendarEvent t where year(t.sessionStart) = :year and month(t.sessionStart) = :month and (t.oneToOneMeeting = NULL or  t.id IN (select c.id from CalendarEvent c inner join c.oneToOneMeeting o inner join o.trainer tr  where c.id = t.id and c.oneToOneMeeting IS NOT NULL and tr.id=:trainerId ))")
	List<CalendarEvent> getCalendarEventsByMonthAndYearForManagement(@Param("month") int month, @Param("year") int year,
			@Param("trainerId") Long trainerId);

	@Query("select t from CalendarEvent t where t.sessionStart BETWEEN :atStartOfDay AND :atEndOfDay and (t.oneToOneMeeting = NULL or  t.id IN (select c.id from CalendarEvent c inner join c.oneToOneMeeting o inner join o.trainer tr  where c.id = t.id and c.oneToOneMeeting IS NOT NULL and tr.id=:trainerId ))")
	List<CalendarEvent> getCalendarEventsByDayForManagement(@Param("atStartOfDay") Date atStartOfDay,
			@Param("atEndOfDay") Date atEndOfDay, @Param("trainerId") Long trainerId);

	Optional<CalendarEvent> findByStartup_IdAndOneToOneMeeting_IdAndSessionStartAndSessionEnd(Long startupId,
			Long oneToOneMeetingId, Date start, Date end);

	@Query("select t from CalendarEvent t where t.sessionStart >= :atStartOfDay and (t.id IN (select c.id from CalendarEvent c inner join c.oneToOneMeeting o inner join o.trainer tr  where c.id = t.id and c.oneToOneMeeting IS NOT NULL and tr.id=:trainerId ))")
	List<CalendarEvent> getTrainerUpcomingCalendarEventsForManagement(@Param("atStartOfDay") Date atStartOfDay,
			@Param("trainerId") Long trainerId);

	long countBySessionStartGreaterThanEqualAndStartup_IdAndOneToOneMeetingIsNotNullAndOneToOneMeeting_Trainer_Id(
			Date atStartOfDay, Long startupId, Long trainerId);

	long countBySessionStartLessThanAndStartup_IdAndOneToOneMeetingIsNotNullAndOneToOneMeeting_Trainer_Id(
			Date atStartOfDay, Long startupId, Long trainerId);

	long countBySessionStartGreaterThanEqualAndOneToOneMeetingIsNotNullAndOneToOneMeeting_Trainer_Id(Date atStartOfDay,
			Long trainerId);

	long countBySessionStartLessThanAndOneToOneMeetingIsNotNullAndOneToOneMeeting_Trainer_Id(Date atStartOfDay,
			Long trainerId);

	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM CalendarEvent e WHERE e.slot.id = :slotId and e.startup.id = :startupId")
	void removeBySlotIdAndStartup(@Param("slotId") Long slotId, @Param("startupId") Long startupId);

	@Query("select t from CalendarEvent t where year(t.sessionStart) = :year and month(t.sessionStart) = :month")
	List<CalendarEvent> getCalendarEventsByMonthAndYearForSuperAdmin(@Param("month") int month,
			@Param("year") int year);

	@Query("select t from CalendarEvent t where t.sessionStart BETWEEN :atStartOfDay AND :atEndOfDay")
	List<CalendarEvent> getCalendarEventsByDayForSuperAdmin(@Param("atStartOfDay") Date atStartOfDay,
			@Param("atEndOfDay") Date atEndOfDay);

	long countBySessionStartGreaterThanEqualAndOneToOneMeetingIsNotNull(Date atStartOfDay);

	long countBySessionStartLessThanAndOneToOneMeetingIsNotNull(Date atStartOfDay);

	@Query("select t from CalendarEvent t where t.sessionStart >= :atStartOfDay and (t.id IN (select c.id from CalendarEvent c inner join c.oneToOneMeeting o where c.id = t.id and c.oneToOneMeeting IS NOT NULL))")
	List<CalendarEvent> getSuperAdminUpcomingCalendarEventsForManagement(@Param("atStartOfDay") Date atStartOfDay);

	long countBySessionStartGreaterThanEqualAndStartup_IdAndOneToOneMeetingIsNotNull(Date atStartOfDay, Long startupId);

	long countBySessionStartLessThanAndStartup_IdAndOneToOneMeetingIsNotNull(Date atStartOfDay, Long startupId);

	@Query(value = "select t from CalendarEvent t where t.sessionStart BETWEEN :atStartOfMonth AND :atEndOfMonth")
	List<CalendarEvent> getCalendarEventsByMonthForSuperAdmin(@Param("atStartOfMonth") Date atStartOfMonth,
			@Param("atEndOfMonth") Date atEndOfMonth);

	@Query(value = "select t from CalendarEvent t where t.sessionStart BETWEEN :atStartOfMonth AND :atEndOfMonth and (t.oneToOneMeeting = NULL or  t.id IN (select c.id from CalendarEvent c inner join c.oneToOneMeeting o inner join o.trainer tr  where c.id = t.id and c.oneToOneMeeting IS NOT NULL and tr.id=:trainerId ))")
	List<CalendarEvent> getCalendarEventsByMonthForManagement(@Param("atStartOfMonth") Date atStartOfMonth,
			@Param("atEndOfMonth") Date atEndOfMonth, @Param("trainerId") Long trainerId);

	List<CalendarEvent> findByStartup_IdAndSessionStartBetween(Long startupId, Date start, Date end);

	List<CalendarEvent> findBySessionStartBetween(Date start, Date end);

	@Query(value = "select t from CalendarEvent t where (t.trainingSession is not null and t.trainingSession.intakeProgram.id = :intakeId and t.sessionStart BETWEEN :atStartOfMonth AND :atEndOfMonth) or (t.trainingSession is not null and t.trainingSession.intakeProgram.id = :intakeId and t.sessionEnd BETWEEN :atStartOfMonth AND :atEndOfMonth) ")
	List<CalendarEvent> getAllCalendarEventsByStartupIdAndMonth(@Param("intakeId") Long intakeId,
			@Param("atStartOfMonth") Date atStartOfMonth, @Param("atEndOfMonth") Date atEndOfMonth);

	@Query(value = "select t from CalendarEvent t where t.trainingSession is not null and t.trainingSession.intakeProgram.id = :intakeId and t.sessionStart <= :atStartOfDay and t.sessionEnd >= :atStartOfDay ")
	List<CalendarEvent> getDayCalendarEventsByStartupIdAndMonth(@Param("intakeId") Long intakeId,
			@Param("atStartOfDay") Date atStartOfDay);

	@Query(value = "select t from CalendarEvent t where t.trainingSession is not null and t.sessionStart <= :atStartOfDay and t.sessionEnd >= :atStartOfDay ")
	List<CalendarEvent> getDayCalendarEvents(@Param("atStartOfDay") Date atStartOfDay);
}
