package com.stc.inspireu.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.BootcampEvaluationForm;

@Transactional
public interface BootcampFormRepository extends PagingAndSortingRepository<BootcampEvaluationForm, Long> {

	Page<BootcampEvaluationForm> findByFormNameContainingIgnoreCase(String name, Pageable paging);

	BootcampEvaluationForm findByIntakeProgram_Id(Long id);

	Page<BootcampEvaluationForm> findByIntakeProgramIsNull(Pageable paging);

	Page<BootcampEvaluationForm> findByFormNameContainingIgnoreCaseAndIntakeProgramIsNull(String name, Pageable paging);

	BootcampEvaluationForm findByIntakeProgram_IdAndFormNameIgnoreCase(Long intakeProgramId, String formName);

	BootcampEvaluationForm findByIntakeProgramIsNullAndFormNameIgnoreCase(String formName);

}
