package com.stc.inspireu.jpa.projections;

import org.springframework.beans.factory.annotation.Value;

public interface ProjectEvaluationIdAndJsonForm {

	String getJsonForm();

	@Value("#{target.evaluationSummary.id}")
	String getEvaluationSummaryId();
}
