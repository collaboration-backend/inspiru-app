package com.stc.inspireu.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.AcademyRoomDto;
import com.stc.inspireu.dtos.AcademyRoomManagementDto;
import com.stc.inspireu.dtos.AssignmentManagementDto;
import com.stc.inspireu.dtos.FeedbackFormManagementDto;
import com.stc.inspireu.dtos.GetAssignmentDto;
import com.stc.inspireu.dtos.GetAssignmentFileDto;
import com.stc.inspireu.dtos.GetFeedbackDto;
import com.stc.inspireu.dtos.PostAcademyRoomDto;
import com.stc.inspireu.dtos.PostAcademyRoomWorkShopSessionDto;
import com.stc.inspireu.dtos.PostAssignmentManagementDto;
import com.stc.inspireu.dtos.PostFeedbackFormTemplateManagementDto;
import com.stc.inspireu.dtos.PostSurveyTemplateManagementDto;
import com.stc.inspireu.dtos.PostTrainingMaterialDto;
import com.stc.inspireu.dtos.PutAcademyRoomShareDto;
import com.stc.inspireu.dtos.PutAssignmentManagementDto;
import com.stc.inspireu.dtos.PutSurveyManagementDto;
import com.stc.inspireu.dtos.StartupFeedbackFormsDto;
import com.stc.inspireu.dtos.SubmitStartupSurveyDto;
import com.stc.inspireu.dtos.SurveyDto;
import com.stc.inspireu.dtos.SurveyManagementDto;
import com.stc.inspireu.dtos.TrainingMaterialManagementDto;
import com.stc.inspireu.dtos.WorkshopSessionManagementDto;
import com.stc.inspireu.dtos.WorkshopSessionSubmissionManagementDto;
import com.stc.inspireu.models.TrainingMaterial;

public interface AcademyRoomService {

	List<AcademyRoomDto> getStartupAcademyRoomsByAcademyRoomStatus(CurrentUserObject currentUserObject,
			String academyRoomStatus);

	Map<String, Object> getStartupAcademyRoomsById(CurrentUserObject currentUserObject, Long academyRoomId);

	Object getStartupAcademyRoomWorksopSession(CurrentUserObject currentUserObject, Long workshopSessionId,
			Long workshopSessionId2);

	Page<TrainingMaterial> getWorksopSessionTrainingMaterials(CurrentUserObject currentUserObject,
			Long workshopSessionId, String name, Pageable paging, String filterBy);

	List<SurveyDto> getWorksopSessionSurveys(CurrentUserObject currentUserObject, Long workshopSessionId);

	List<GetFeedbackDto> getWorksopSessionFeedbacks(CurrentUserObject currentUserObject, Long workshopSessionId);

	List<GetAssignmentDto> getWorksopSessionAssignments(CurrentUserObject currentUserObject, Long workshopSessionId);

	void submitWorksopSessionSurvey(CurrentUserObject currentUserObject, Long workshopSessionId, Long surveyId,
			SubmitStartupSurveyDto submitStartupSurveyDto);

	ResponseEntity<?> createAcademyRoom(CurrentUserObject currentUserObject, PostAcademyRoomDto academyRoomRequest);

	ResponseEntity<?> getAcademyRoomForStartups(CurrentUserObject currentUserObject, Long intakeProgramId,
			Long startupId);

	ResponseEntity<?> getWorkshopSessionForStartups(CurrentUserObject currentUserObject, Long academicRoomId,
			Long startupId);

	Object deleteAcademyRoom(CurrentUserObject currentUserObject, Long academyRoomId);

	Object publishAcademyRoom(CurrentUserObject currentUserObject, Long academyRoomId);

	Object getManagementAcademyRoom(CurrentUserObject currentUserObject, Long academyRoomId);

	ResponseEntity<?> shareAcademyRoom(CurrentUserObject currentUserObject, Long academyRoomId,
			List<PutAcademyRoomShareDto> putAcademyRoomShareDto);

	List<Object> dropdownManagementShareMembers(CurrentUserObject currentUserObject, Long academyRoomId,
			List<String> roles);

	List<AcademyRoomManagementDto> getManagementAcademyRoomsByAcademyRoomStatus(CurrentUserObject currentUserObject,
			String academyRoomStatus, Pageable paging, Set<Long> academyRoomIds);

	List<Object> dropdownManagementAcademyRoomIntakes(CurrentUserObject currentUserObject, String programName);

	// management- WorkShop sessions
	List<WorkshopSessionManagementDto> getManagementAcademyRoomWorkShopSessions(CurrentUserObject currentUserObject,
			Long academyRoomId, Set<Long> workshopSessionIds, Pageable paging);

	ResponseEntity<?> createManagementAcademyRoomWorkShopSession(CurrentUserObject currentUserObject,
			PostAcademyRoomWorkShopSessionDto academyRoomWorkShopSessionRequest, Long academyRoomId);

	Object getManagementAcademyRoomWorkShopSession(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId);

	Object deleteManagementAcademyRoomWorkShopSession(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId);

	ResponseEntity<?> publishManagementAcademyRoomWorkShopSession(CurrentUserObject currentUserObject,
			Long academyRoomId, Long workshopSessionId);

	List<WorkshopSessionSubmissionManagementDto> getManagementWorkShopSessionsAllSubmissions(
			CurrentUserObject currentUserObject, Long academyRoomId, Long workshopSessionId, Pageable pageable,
			String filterBy, String searchBy);

	ResponseEntity<?> shareAcademyRoomWorkShopSession(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, List<PutAcademyRoomShareDto> putAcademyRoomShareDto);

	List<Object> dropdownWorkshopSessionShareMembers(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, List<String> roles);

	// management- WorkShop sessions -Training Materials
	List<TrainingMaterialManagementDto> getManagementWorkShopSessionTrainingMaterials(
			CurrentUserObject currentUserObject, Long academyRoomId, Long workshopSessionId, Pageable paging,
			String filterBy, String searchBy);

	Object createManagementWorkShopSessionTrainingMaterials(CurrentUserObject currentUserObject,
			PostTrainingMaterialDto trainingMaterialRequest, Long workshopSessionId, Long academyRoomId);

	Object publishManagementWorkShopSessionTrainingMaterials(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, Long trainingMaterialId);

	// management- WorkShop sessions -assignments
	Object getManagementWorkShopSessionAssignments(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, Pageable paging, String filterBy, String searchBy);

	List<AssignmentManagementDto> getWorkShopSessionAssignmentsSubmitted(CurrentUserObject currentUserObject,
			Long academyRoomId, Long workshopSessionId, Long assignmentId, Pageable paging, String filterBy,
			String searchBy);

	AssignmentManagementDto getManagementWorkShopSessionAssignment(CurrentUserObject currentUserObject,
			Long academyRoomId, Long workshopSessionId, Long assignmentId);

	ResponseEntity<?> createManagementWorkShopSessionAssignment(CurrentUserObject currentUserObject,
			PostAssignmentManagementDto workShopSessionAssignmentRequest, Long academyRoomId, Long workshopSessionId,
			MultipartFile[] files);

	Object reviewManagementWorkShopSessionAssignment(CurrentUserObject currentUserObject,
			PutAssignmentManagementDto workShopSessionAssignmentRequest, Long academyRoomId, Long workshopSessionId,
			Long assignmentId);

	Object getManagementWorkShopSessionSubmittedAssignment(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, Long assignmentId);

	Object publishManagementWorkShopSessionAssignment(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, Long assignmentId);

	Object deleteManagementWorkShopSessionAssignment(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, Long assignmentId);

	Object getManagementWorkShopSessionFeedbackForms(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, Pageable paging, String filterBy, String searchBy);

	List<StartupFeedbackFormsDto> getWorkShopSessionFeedbacksSubmitted(CurrentUserObject currentUserObject,
			Long academyRoomId, Long workshopSessionId, Long surveyId, Pageable paging, String filterBy,
			String searchBy, String sortData);

	FeedbackFormManagementDto getManagementWorkShopSessionFeedback(CurrentUserObject currentUserObject,
			Long academyRoomId, Long workshopSessionId, Long feedbackId);

	Object createManagementWorkShopSessionFeedbackFormTemplates(CurrentUserObject currentUserObject,
			PostFeedbackFormTemplateManagementDto feedbackTemplateRequest, Long workshopSessionId, Long academyRoomId);

	Object publishManagementWorkShopSessionFeedbackFormTemplates(CurrentUserObject currentUserObject,
			Long workshopSessionId, Long academyRoomId, Long feedbackId);

	Object deleteManagementWorkShopSessionFeedbackFormTemplates(CurrentUserObject currentUserObject,
			Long workshopSessionId, Long academyRoomId, Long feedbackId);

	// management- WorkShop sessions -Surveys
	Object getManagementWorkShopSessionSurveys(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, Pageable paging, String filterBy, String searchBy);

	List<SurveyManagementDto> getWorkShopSessionSurveysSubmitted(CurrentUserObject currentUserObject,
			Long academyRoomId, Long workshopSessionId, Long surveyId, Pageable paging, String filterBy,
			String searchBy);

	SurveyManagementDto getManagementWorkShopSessionSurvey(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, Long surveyId);

	Object createManagementWorkShopSessionSurveyTemplates(CurrentUserObject currentUserObject,
			PostSurveyTemplateManagementDto surveyTemplateRequest, Long workshopSessionId, Long academyRoomId);

	Object publishManagementWorkShopSessionSurveyTemplates(CurrentUserObject currentUserObject, Long workshopSessionId,
			Long academyRoomId, Long surveyId);

	Object deleteManagementWorkShopSessionSurveyTemplates(CurrentUserObject currentUserObject, Long workshopSessionId,
			Long academyRoomId, Long surveyId);

	Object getManagementWorkShopSessionSubmittedSurvey(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, Long surveyId);

	Object reviewManagementWorkShopSessionSurvey(CurrentUserObject currentUserObject,
			PutSurveyManagementDto workShopSessionSurveyRequest, Long academyRoomId, Long workshopSessionId,
			Long surveyId);

	List<GetAssignmentFileDto> getWorksopSessionAssignmentDownloadableMaterials(CurrentUserObject currentUserObject,
			Long workshopSessionId, Long assignmentId);

	Map<String, Object> putStartupAcademyRoom(CurrentUserObject currentUserObject, Long academyRoomId);

	Object putStartupAcademyRoomWorksopSession(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId);

	Object putWorksopSessionAssignments(CurrentUserObject currentUserObject, Long workshopSessionId, Long assignmentId,
			MultipartFile[] documents);

	Object cloneWorksopSessionAssignment(CurrentUserObject currentUserObject, Long workshopSessionId,
			Long assignmentId);

	Map<String, Object> getWorksopSessionAssignment(CurrentUserObject currentUserObject, Long workshopSessionId,
			Long assignmentId);

	Object unShareAcademyRoom(CurrentUserObject currentUserObject, Long academyRoomId,
			PutAcademyRoomShareDto putAcademyRoomShareDto);

	Object unShareAcademyRoomWorkShopSession(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, PutAcademyRoomShareDto putAcademyRoomShareDto);

	ResponseEntity<?> deleteWorksopSessionAssignment(CurrentUserObject currentUserObject, Long workshopSessionId,
			Long assignmentId, Long documentId);

	ResponseEntity<?> listFeedbackCoaches(CurrentUserObject currentUserObject, Long academyRoomId,
			Long workshopSessionId, Pageable paging);

	List<StartupFeedbackFormsDto> getManagementWorkShopSessionFeedbackSubmissionsByCoach(
			CurrentUserObject currentUserObject, Long academyRoomId, Long workshopSessionId, Long feedbackId,
			Long coachId, Pageable paging, String filterBy, String searchBy, String sortData);
}
