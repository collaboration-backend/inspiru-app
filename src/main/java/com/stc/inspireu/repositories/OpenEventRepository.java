package com.stc.inspireu.repositories;

import java.util.Date;
import java.util.List;

import com.stc.inspireu.models.IntakeProgram;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.OpenEvent;

@Transactional
public interface OpenEventRepository extends PagingAndSortingRepository<OpenEvent, Long> {

	List<OpenEvent> findByIntakeProgram_IdAndEventPhase(Long intakeProgramId, String string);

    boolean existsByIntakeProgram_IdAndEventPhase(Long intakeProgramId, String string);

	@Query(value = "select t from OpenEvent t where t.intakeProgram.id = :intakeProgramId and t.sessionStart >= :date order by t.createdOn desc")
	List<OpenEvent> findUpComingEvent(@Param("intakeProgramId") Long intakeProgramId, @Param("date") Date date);

	@Query(value = "select t from OpenEvent t where t.intakeProgram.id = :intakeProgramId")
	List<OpenEvent> findTop10(Long intakeProgramId, Pageable paging);

    Boolean existsByIntakeProgramAndEventPhase(IntakeProgram intakeProgram, String string);

}
