package com.stc.inspireu.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectDueDiligenceFile2021;
import com.stc.inspireu.models.DueDiligenceFile2021;

@Transactional
public interface DueDiligenceFile2021Repository extends PagingAndSortingRepository<DueDiligenceFile2021, Long> {

	List<DueDiligenceFile2021> findDistinctByStartup_IdAndFieldIdIn(Long id, Set<String> fieldIds);

	List<ProjectDueDiligenceFile2021> findByStartup_IdAndFieldId(Long id, String fieldId);

	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM DueDiligenceFile2021 e WHERE e.id = :documentId AND e.startup.id = :startupId AND e.fieldId = :fieldId")
	int removeByIdAndStartupIdAndFieldId(@Param("documentId") Long documentId, @Param("startupId") Long startupId,
			String fieldId);

	List<DueDiligenceFile2021> findByStartup_IdAndFieldIdIn(Long id, Set<String> fieldIds);

	List<ProjectDueDiligenceFile2021> findByIdAndStartup_IdAndFieldId(Long dueDiligenceId, Long id, String fieldId);

	List<ProjectDueDiligenceFile2021> findByStartup_IdAndFieldIdAndDueDiligenceTemplate2021_Id(Long id, String fieldId,
			Long dueDiligenceId);

	List<ProjectDueDiligenceFile2021> findByStartup_IdAndFieldIdAndDueDiligenceTemplate2021_RefDueDiligenceTemplate2021_Id(
			Long id, String fieldId, Long refDueDiligenceId);

	List<DueDiligenceFile2021> findDistinctByDueDiligenceTemplate2021_IdAndStartup_IdAndFieldIdIn(
			Long dueDiligenceTemplateId, Long startupId, Set<String> fieldIds);

	DueDiligenceFile2021 findByIdAndStartup_IdAndFieldIdAndDueDiligenceTemplate2021_Id(Long id, Long startupId,
			String fieldId, Long dueDiligenceId);

	List<DueDiligenceFile2021> findByStartup_IdAndFieldIdInAndAndDueDiligenceTemplate2021_RefDueDiligenceTemplate2021_Id(
			Long id, Set<String> fieldIds, Long dueDiligenceId);

	DueDiligenceFile2021 findByIdAndStartup_IdAndFieldIdAndDueDiligenceTemplate2021_RefDueDiligenceTemplate2021_Id(
			Long id, Long startupId, String fieldId, Long refDueDiligenceId);

}
