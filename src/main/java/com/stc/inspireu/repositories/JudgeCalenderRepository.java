package com.stc.inspireu.repositories;

import com.stc.inspireu.models.JudgeCalendar;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Transactional
public interface JudgeCalenderRepository extends PagingAndSortingRepository<JudgeCalendar, Long> {

	Optional<JudgeCalendar> findByJudgeIdAndPhaseAndIntakeProgramIdAndEmail(Long Judge, String phase,
			Long intakeProgram, String mail);

	@Query(value = "select t from JudgeCalendar t where t.sessionStart BETWEEN :atStartOfMonth AND :atEndOfMonth AND judgeId =:judgeId")
	List<JudgeCalendar> getCalendarEventsByMonthForJudge(@Param("atStartOfMonth") Date atStartOfMonth,
			@Param("atEndOfMonth") Date atEndOfMonth, @Param("judgeId") Long judgeId);

	@Query(value = "select t from JudgeCalendar t where t.sessionStart <= :atStartOfDay and t.sessionEnd >= :atStartOfDay and judgeId =:judgeId")
	List<JudgeCalendar> getDayCalendarEvents(@Param("atStartOfDay") Date atStartOfDay, @Param("judgeId") Long judgeId);

	List<JudgeCalendar> findByIntakeProgram_IdAndEmailAndPhaseContainingIgnoreCase(Long id, String email,
			String string);
    boolean existsByIntakeProgram_IdAndEmailAndPhaseContainingIgnoreCase(Long id, String email,
			String string);

}
