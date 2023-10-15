package com.stc.inspireu.services;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.EvaluationSubmissionDto;

public interface EvaluationSummaryService {

    Object screeningEvaluationSubmission(CurrentUserObject currentUserObject,
                                          EvaluationSubmissionDto evaluationSubmissionDto);
	Object assessmentEvaluationSubmission(CurrentUserObject currentUserObject,
			EvaluationSubmissionDto evaluationSubmissionDto);

	Object bootcampEvaluationSubmission(CurrentUserObject currentUserObject,
			EvaluationSubmissionDto evaluationSubmissionDto);

	Object assessmentEvaluationExit(CurrentUserObject currentUserObject,
			EvaluationSubmissionDto evaluationSubmissionDto);

	Object bootcampEvaluationExit(CurrentUserObject currentUserObject, EvaluationSubmissionDto evaluationSubmissionDto);

	Object createSummary(CurrentUserObject currentUserObject, Long submissionId, String phase);

	Object doLater(CurrentUserObject currentUserObject, Long submissionId, String phase,
			EvaluationSubmissionDto evaluationSubmissionDto);

	Object exitEval(CurrentUserObject currentUserObject, Long submissionId, String phase,
			EvaluationSubmissionDto evaluationSubmissionDto);

}
