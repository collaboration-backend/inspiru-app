package com.stc.inspireu.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectIdNamePathAssignment;
import com.stc.inspireu.models.AssignmentFile;

@Transactional
public interface AssignmentFileRepository extends PagingAndSortingRepository<AssignmentFile, Long> {

	List<AssignmentFile> findByAssignment_Id(Long assignmentId);

	List<ProjectIdNamePathAssignment> findByAssignment_IdAndWillManagementTrue(Long assignmentId);

	List<ProjectIdNamePathAssignment> findByAssignment_IdAndWillManagementFalse(Long id);

	void deleteByAssignment_Id(Long assignmentId);

	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM AssignmentFile e WHERE e.assignment.id = :assignmentId")
	void deleteByAssignment(@Param("assignmentId") Long assignmentId);

	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM AssignmentFile e WHERE e.assignment.id = :assignmentId and e.id = :documentId")
	void deleteWorksopSessionAssignment(Long assignmentId, Long documentId);

}
