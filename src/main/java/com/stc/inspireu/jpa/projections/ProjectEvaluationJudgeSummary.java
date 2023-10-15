package com.stc.inspireu.jpa.projections;

import org.springframework.beans.factory.annotation.Value;

public interface ProjectEvaluationJudgeSummary {

	@Value("#{target.intakeProgramSubmission.id}")
	Long getIntakeProgramSubmissionId();

	String getJsonForm();

	String getJsonFormBootcamp();
}
