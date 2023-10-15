package com.stc.inspireu.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectIntake;
import com.stc.inspireu.models.IntakeProgram;

@Transactional
public interface IntakeProgramRepository extends PagingAndSortingRepository<IntakeProgram, Long> {

	List<IntakeProgram> findByProgramName(String programName);

	Optional<IntakeProgram> findByIdAndProgramName(Long intakeProgramId, String programName);

	IntakeProgram findFirstByOrderByCreatedOnDesc();

	@Query("SELECT COUNT(u) FROM IntakeProgram u WHERE u.status = :status and u.periodEnd < :date")
	Long countIntakeByDate(@Param("status") String status, @Param("date") Date date);

	Page<IntakeProgram> findByStatus(String string, Pageable paging);

	Page<IntakeProgram> findByStatusAndPeriodEndBefore(String string, Date date, Pageable paging);

	Iterable<ProjectIntake> findAllByOrderByCreatedOnAsc();

	Set<ProjectIntake> findByIdIn(Set<Long> ipIds);

	Page<IntakeProgram> findByStatusNot(String string, Pageable paging);

	Page<IntakeProgram> findByStatusNotIn(List<String> asList, Pageable paging);

    @Query(value = "select p.* from  public.intake_programs p join registration_forms rf on rf.id=p.registrationformid where p.status='PUBLISHED' and rf.duedate>=CURRENT_TIMESTAMP",nativeQuery = true)
    List<IntakeProgram> findOngoingIntakes();
}
