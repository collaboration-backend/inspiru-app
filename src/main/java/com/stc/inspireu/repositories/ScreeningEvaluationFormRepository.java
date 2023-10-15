package com.stc.inspireu.repositories;
import com.stc.inspireu.models.ScreeningEvaluationForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface ScreeningEvaluationFormRepository extends PagingAndSortingRepository<ScreeningEvaluationForm, Long> {

	ScreeningEvaluationForm findByIntakeProgram_Id(Long id);

	Page<ScreeningEvaluationForm> findByFormNameContainingIgnoreCase(String name, Pageable paging);

	Page<ScreeningEvaluationForm> findByIntakeProgramIsNull(Pageable paging);

	Page<ScreeningEvaluationForm> findByFormNameContainingIgnoreCaseAndIntakeProgramIsNull(String name,
                                                                                           Pageable paging);

    ScreeningEvaluationForm findByIntakeProgramIsNullAndFormNameIgnoreCase(String evaluationTemplateName);

    ScreeningEvaluationForm findByIntakeProgram_IdAndFormNameIgnoreCase(Long intakePgmId,
                                                                        String evaluationTemplateName);
    Boolean existsByIntakeProgram_IdAndFormNameIgnoreCase(Long intakePgmId,
                                                        String evaluationTemplateName);

    Optional<ScreeningEvaluationForm> findByFormNameIgnoreCase(String formName);

}
