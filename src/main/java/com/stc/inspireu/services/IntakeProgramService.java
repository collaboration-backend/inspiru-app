package com.stc.inspireu.services;

import java.util.Date;
import java.util.List;

import com.stc.inspireu.dtos.*;
import com.stc.inspireu.dtos.validation.RegistrationFormValidationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.models.IntakeProgram;
import org.springframework.web.multipart.MultipartFile;

public interface IntakeProgramService {

    ResponseEntity<?> createStartupOnboarding(CurrentUserObject currentUserObject, Long intakeProgramId,
                                              PostStartupOnboardingDto postOnboardingDto);

    IntakeProgram saveIntakeProgram(CurrentUserObject currentUserObject, PostIntakeProgramDto postIntakeProgramDto);

    IntakeProgram updateIntakeProgram(Long intakeProgramId, PostIntakeProgramDto postIntakeProgramDto);

    Object shareIntake(CurrentUserObject currentUserObject, Long intakeProgramId, ShareIntakeDto shareIntakeDto);

    Object publishIntake(CurrentUserObject currentUserObject, Long intakeProgramId);

    Object linkForm(String string, CurrentUserObject currentUserObject, Long intakeProgramId, Long id);

    ResponseEntity<?> registrationNextPhase(CurrentUserObject currentUserObject, Long intakeProgramId,
                                            Long registrationId);

    Object assignJudges(CurrentUserObject currentUserObject, Long intakeProgramId, Long assessmentId,
                        AssignJudgeDto assignJudgeDto);

    void assignEvaluatorsForScreening(Long intakeId, AssignEvaluatorsDTO assignEvaluatorsDTO);

    Page<GetIntakeProgramSubmissionDto> registartionSubmissions(CurrentUserObject currentUserObject, Long intakeProgramId, String string, String language,
                                                                Pageable paging, String filterKeyword, String filterBy);

    /**
     * Method to retrieve applications of given intake and return result in an object form
     *
     * @param intakeProgramId - To retrieve submissions received for this intake
     * @param applicationIds  - If none passed, will fetch all submissions
     * @return List of DTOs (JSON will be parsed into Java Object using Gson)
     */
    List<RegistrationFormValidationDTO> intakeProgramApplications(Long intakeProgramId, List<Long> applicationIds);

    GetIntakeProgramSubmissionDto findRegistrationSubmission(Long intakeId, Long submissionId);

    Page<GetIntakeProgramSubmissionDto> assessmentSubmissions(Long intakeProgramId, String string, Pageable paging,
                                                              String filterKeyword, String filterBy);

    ResponseEntity<Object> startAssessmentEvaluation(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                     Long assessmentId);

    ResponseEntity<Object> assigneesAssessements(CurrentUserObject currentUserObject, Pageable paging);

    ResponseEntity<Object> assigneesAssessementEvaluations(CurrentUserObject currentUserObject, Long id,
                                                           Pageable paging, String filterKeyword, String filterBy);

    ResponseEntity<Object> screeningEvaluations(CurrentUserObject currentUserObject, Long id,
                                                           Pageable paging, String filterKeyword, String filterBy);

    Object asessmentMoveToEvaluate(CurrentUserObject currentUserObject, Long intakeProgramId, Long assessmentId);

    ResponseEntity<Object> assessmentNextPhase(CurrentUserObject currentUserObject, Long intakeProgramId,
                                               Long summaryId);

    ResponseEntity<?> listStartups(CurrentUserObject currentUserObject, Long intakeProgramId, Pageable paging);

    Object inviteDueDiligence(CurrentUserObject currentUserObject, Long intakeProgramId, Long startupId,
                              InviteDueDiligenceDto inviteDueDiligenceDto);

    ResponseEntity<Object> getEvalSummary(CurrentUserObject currentUserObject, String string, Long intakeProgramId,
                                          String filterBy, String filterKeyword, Pageable paging);

    Object stopAssessmentEvaluation(CurrentUserObject currentUserObject, Long intakeProgramId, Long assessmentId);

    Page<GetIntakeProgramSubmissionDto> listOnboardingStarups(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                              Pageable paging);

    ResponseEntity<Object> assigneesBootcamps(CurrentUserObject currentUserObject, Pageable paging);

    ResponseEntity<Object> getAssignees(List<String> rln, Pageable paging, String string, String filterKeyWord);

    ResponseEntity<Object> submissionTrend(Long intakeProgramId);

    ResponseEntity<?> getIntakePrograms(CurrentUserObject currentUserObject, Pageable paging);

    ResponseEntity<Object> getIntakeProgram(CurrentUserObject currentUserObject, Long intakeProgramId);

    ResponseEntity<Object> deleteIntake(CurrentUserObject currentUserObject, Long intakeProgramId);

    ResponseEntity<?> intakeProgramsShareMembers(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                 List<String> roles);

    ResponseEntity<?> intakeProgramsSharedMembers(CurrentUserObject currentUserObject, Long intakeProgramId);

    ResponseEntity<?> notSelectedToAssessment(CurrentUserObject currentUserObject, Long intakePgmId);

    ResponseEntity<?> toggleAssessmentAttendance(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                 Long assessmentId, Boolean status);

    ResponseEntity<?> toggleBootcampAttendance(CurrentUserObject currentUserObject, Long intakeProgramId,
                                               Long bootcampId, Boolean status);

    ResponseEntity<?> listAllSubmissions(CurrentUserObject currentUserObject, Long intakeProgramId, String phase,
                                         String filterKeyword, Pageable paging);

    List<RegistrationLinkDTO> onGoingIntakes();

    /**
     * Method to update registration form's due date
     *
     * @param currentUserObject
     * @param intakeProgramId
     * @param dueDate
     * @return
     */
    ResponseEntity<?> updateRegistrationFormDueDate(CurrentUserObject currentUserObject, Long intakeProgramId, Date dueDate);

    /**
     * Method to publish assessment form of the given intake
     *
     * @param currentUserObject
     * @param intakeProgramId
     * @return
     */
    ResponseEntity<?> publishAssessmentForm(CurrentUserObject currentUserObject, Long intakeProgramId);

    /**
     * Method to publish bootcamp form of the given intake
     *
     * @param currentUserObject
     * @param intakeProgramId
     * @return
     */
    ResponseEntity<?> publishBootcampForm(CurrentUserObject currentUserObject, Long intakeProgramId);

    /**
     * Method to publish screening form
     *
     * @param currentUserObject
     * @param intakeProgramId
     * @return
     */
    ResponseEntity<?> publishScreeningForm(CurrentUserObject currentUserObject, Long intakeProgramId);

    /**
     * Method to import applications from a valid xlsx file
     *
     * @param intakeId
     * @param file
     */
    void importApplications(Long intakeId, MultipartFile file) throws Exception;

    void importScreeningEvaluations(CurrentUserObject currentUserObject,Long intakeId, MultipartFile file) throws Exception;

    Object assignBootcampJudges(CurrentUserObject currentUserObject, Long intakeProgramId, Long assessmentId,
                                AssignJudgeDto assignJudgeDto);

    ResponseEntity<Object> assigneesBootcampEvaluations(CurrentUserObject currentUserObject, Long id, Pageable paging,
                                                        String filterKeyword, String filterBy);

    ResponseEntity<Object> startBootcampEvaluation(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                   Long bootcampId);

    Object finitBootcamp(CurrentUserObject currentUserObject, Long intakeProgramId,
                         BootCampSelectedStartupDto selectedStartupRequest);

    Object stopBootcampEvaluation(CurrentUserObject currentUserObject, Long intakeProgramId, Long bootcampId);

    ResponseEntity<Object> bootcampSubmissions(CurrentUserObject currentUserObject, Long intakeProgramId, String string,
                                               Pageable paging, String filterKeyword, String filterBy);

    ResponseEntity<?> notSelectedToBootCamp(CurrentUserObject currentUserObject, Long intakePgmId);

    ResponseEntity<?> startEvaluation(CurrentUserObject currentUserObject, Long intakeId, Long applicationId);

    ResponseEntity<?> stopEvaluation(CurrentUserObject currentUserObject, Long intakeId, Long applicationId);

    void startEvaluation(String phase, Long intakeId, List<Long> applicationIds);

    void stopEvaluation(String phase, Long intakeId, List<Long> applicationIds);

    void generateSummary(String phase, Long intakeId, List<Long> applicationIds);

    void moveToNextPhase(String phase, Long intakeId, List<Long> applicationIds);

    ResponseEntity<Object> getScreeningSummary(CurrentUserObject currentUserObject, Long intakeProgramId,
                                               String filterBy, String filterKeyword, Pageable paging);

    StartupEvaluationSummaryDTO findEvaluatorsSummary(Long applicationId,String phase);


}
