package com.stc.inspireu.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.stc.inspireu.jpa.projections.ProjectEvaluationJudgeSummary;
import com.stc.inspireu.models.EvaluationJudgeSummary;

public interface EvaluationJudgeSummaryRepository extends PagingAndSortingRepository<EvaluationJudgeSummary, Long> {

	EvaluationJudgeSummary findBySubmittedJudge_IdAndEvaluationSummary_Id(Long id, Long id2);

    boolean existsBySubmittedJudge_IdAndEvaluationSummary_Id(Long evaluatorId, Long summaryId);

    List<EvaluationJudgeSummary> findAllByEvaluationSummary_Id(Long evaluationSummaryId);

	List<ProjectEvaluationJudgeSummary> findByIntakeProgramSubmission_IdIn(Set<Long> intakeProgramSubmissionIds);

	List<ProjectEvaluationJudgeSummary> findByIntakeProgramSubmission_IdInAndSubmittedJudge_Id(
			Set<Long> intakeProgramSubmissionIds, Long id);


}
