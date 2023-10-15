package com.stc.inspireu.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.AssessmentEvaluationForm;

@Transactional
public interface AssessmentEvaluationFormRepository extends PagingAndSortingRepository<AssessmentEvaluationForm, Long> {

	AssessmentEvaluationForm findByIntakeProgram_Id(Long id);

	Page<AssessmentEvaluationForm> findByFormNameContainingIgnoreCase(String name, Pageable paging);

	Page<AssessmentEvaluationForm> findByIntakeProgramIsNull(Pageable paging);

	Page<AssessmentEvaluationForm> findByFormNameContainingIgnoreCaseAndIntakeProgramIsNull(String name,
			Pageable paging);

	AssessmentEvaluationForm findByIntakeProgramIsNullAndFormNameIgnoreCase(String evaluationTemplateName);

	AssessmentEvaluationForm findByIntakeProgram_IdAndFormNameIgnoreCase(Long intakePgmId,
			String evaluationTemplateName);

}
