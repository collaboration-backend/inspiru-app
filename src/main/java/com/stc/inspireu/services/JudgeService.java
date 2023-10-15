package com.stc.inspireu.services;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.GetIntakeProgramDto;

public interface JudgeService {

	List<GetIntakeProgramDto> judgesIntakePrograms(CurrentUserObject currentUserObject, Pageable paging);

	Map<String, Object> submissionDetails(CurrentUserObject currentUserObject, Long intakeProgramId, Long assessmentId,
			String phase);

    Map<String, Object> evaluationNextItemInQueue(CurrentUserObject currentUserObject);

	Map<String, Object> currentEvaluationStatus(CurrentUserObject currentUserObject);

	ResponseEntity<?> getAssessments(CurrentUserObject currentUserObject, String filterBy, String filterKeyword,
			Long filterDate, Pageable paging);

	ResponseEntity<?> getBootcamps(CurrentUserObject currentUserObject, String filterBy, String filterKeyword,
			Long filterDate, Pageable paging);

	ResponseEntity<?> getAssessmentSummaryStatus(CurrentUserObject currentUserObject, Long judgeId,
			Long intakeProgramId);

	ResponseEntity<?> getBootcampSummaryStatus(CurrentUserObject currentUserObject, Long judgeId, Long intakeProgramId);

	ResponseEntity<?> getEvaluationsSummaryStatus(CurrentUserObject currentUserObject, Long judgeId,
			Long intakeProgramId);

	ResponseEntity<?> judgeAssessementEvaluations(CurrentUserObject currentUserObject, Long intakeProgramId,
			Pageable paging, String filterKeyword, String filterBy);

	ResponseEntity<?> judgeBootcampEvaluations(CurrentUserObject currentUserObject, Long intakeProgramId,
			Pageable paging, String filterKeyword, String filterBy);

	ResponseEntity<?> getProceedToSummayStatus(CurrentUserObject currentUserObject, Long intakeProgramId, String phase);

	ResponseEntity<?> judgeBootcampApps(CurrentUserObject currentUserObject, Long intakeProgramId, Pageable paging,
			String filterKeyword, String filterBy);

	ResponseEntity<?> judgeAssessementApps(CurrentUserObject currentUserObject, Long intakeProgramId, Pageable paging,
			String filterKeyword, String filterBy);

	ResponseEntity<Object> generateSummaryAssessmentBootcamp(CurrentUserObject currentUserObject, Long intakeProgramId,
			String phase);

	ResponseEntity<?> getFinalSummayStatus(CurrentUserObject currentUserObject, Long intakeProgramId, String phase);

	ResponseEntity<?> getAllFinalSummayStatus(CurrentUserObject currentUserObject, Long intakeProgramId, String phase);

	ResponseEntity<?> judgeAssessmentAllSummary(CurrentUserObject currentUserObject, Long intakeProgramId,
			Pageable paging, String filterKeyword, String filterBy);

	ResponseEntity<?> judgeBootcampAllSummary(CurrentUserObject currentUserObject, Long intakeProgramId,
			Pageable paging, String filterKeyword, String filterBy);

}
