package com.stc.inspireu.repositories;

import com.stc.inspireu.models.DueDiligenceTemplate2021;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface DueDiligenceTemplate2021Repository extends PagingAndSortingRepository<DueDiligenceTemplate2021, Long>,
		JpaSpecificationExecutor<DueDiligenceTemplate2021> {

	List<DueDiligenceTemplate2021> findByIntakeProgram_IdAndStatus(long intakeNumber, String string);

	List<DueDiligenceTemplate2021> findByIntakeProgram_IdAndName(long intakeNumber, String string, Sort by);

	DueDiligenceTemplate2021 findByIdAndStartup_Id(Long dueDiligenceId, Long id);

	List<DueDiligenceTemplate2021> findByIntakeProgram_IdAndStatusAndIsActiveTrueAndStartupIdIsNull(Long intakeNumber,
			String string);

	@Query(value = "select o from DueDiligenceTemplate2021 o where o.intakeProgram.id = :intakeNumber and o.status = :status and o.startup is null")
	List<DueDiligenceTemplate2021> getTemplate(@Param("intakeNumber") Long intakeNumber,
			@Param("status") String status);

	Page<DueDiligenceTemplate2021> findAllByIntakeProgram_ProgramNameAndIsArchiveFalseAndStartupIsNull(
			String intakeProgramName, Pageable pageable);

	Page<DueDiligenceTemplate2021> findAllByRefDueDiligenceTemplate2021_IdAndStartupIsNotNull(Long dueDiligenceId,
			Pageable pageable);

	@Query("select u from DueDiligenceTemplate2021 u where u.refDueDiligenceTemplate2021.id = :dueDiligenceId and u.startup is not null and u.startup.startupName like %:filterKeyword%")
	Page<DueDiligenceTemplate2021> findByStartupName(String filterKeyword, Long dueDiligenceId, Pageable pageable);

	@Query("select u from DueDiligenceTemplate2021 u where u.refDueDiligenceTemplate2021.id = :id and u.submittedUser is not null and lower(u.submittedUser.alias) like %:filterKeyword%")
	public Page<DueDiligenceTemplate2021> getDueDiligenceByKeyword(String filterKeyword, Long id, Pageable paging);

	DueDiligenceTemplate2021 findByRefDueDiligenceTemplate2021_IdAndStartup_Id(Long refDueDiligenceId, Long id);

	DueDiligenceTemplate2021 findByIdAndIntakeProgram_Id(Long refDueDiligenceId, Long intakeNumber);

	Optional<DueDiligenceTemplate2021> findByIdAndStartupIdIsNull(Long dueDiligenceId);

	@Modifying
	@Query("UPDATE DueDiligenceTemplate2021 c SET c.status = :status WHERE c.intakeProgram.id = :intakeProgramId")
	void updateStatus(@Param("intakeProgramId") Long intakeProgramId, @Param("status") String status);

	List<DueDiligenceTemplate2021> findByIntakeProgram_IdAndStatusAndStartupIdIsNull(long intakeNumber, String string,
			Sort by);

	DueDiligenceTemplate2021 findByIdAndIntakeProgram_IdAndStartupIdIsNull(Long dueDiligenceId, Long intakeNumber);

	List<DueDiligenceTemplate2021> findByIntakeProgram_IdAndStartupIdIsNull(Long intakeProgramId);
}
