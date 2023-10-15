package com.stc.inspireu.services.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.jpa.projections.ProjectEvaluationJudgeSummary;
import com.stc.inspireu.jpa.projections.ProjectIntakeProgramSubmission;
import com.stc.inspireu.mappers.EvaluationSummaryMapper;
import com.stc.inspireu.mappers.IntakeProgramMapper;
import com.stc.inspireu.mappers.IntakeProgramSubmissionMapper;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.JudgeService;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JudgeServiceImpl implements JudgeService {

    private final IntakeProgramRepository intakeProgramRepository;
    private final IntakeProgramSubmissionRepository intakeProgramSubmissionRepository;
    private final UserRepository userRepository;
    private final EvaluationSummaryRepository evaluationSummaryRepository;
    private final EvaluationJudgeSummaryRepository evaluationJudgeSummaryRepository;
    private final KeyValueRepository keyValueRepository;
    private final EvaluationSummaryMapper evaluationSummaryMapper;
    private final IntakeProgramSubmissionMapper intakeProgramSubmissionMapper;
    private final IntakeProgramMapper intakeProgramMapper;

    @Transactional
    @Override
    public List<GetIntakeProgramDto> judgesIntakePrograms(CurrentUserObject currentUserObject, Pageable paging) {
        User user = userRepository.findById(currentUserObject.getUserId() != null ? currentUserObject.getUserId() : (long) 0)
            .orElseThrow(() -> ItemNotFoundException.builder("User").build());
        List<Long> intakeIds = intakeProgramSubmissionRepository.findByUsers_Id(user.getId())
            .stream().map(s -> s.getIntakeProgram().getId()).collect(Collectors.toList());
        Iterable<IntakeProgram> ls = intakeProgramRepository.findAllById(intakeIds);
        List<GetIntakeProgramDto> ll = intakeProgramMapper.toGetIntakeProgramDtoList(ls);
        ll.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
        return ll;
    }

    @Transactional
    @Override
    public Map<String, Object> submissionDetails(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                 Long submissionId, String phase) {
        User user = userRepository.findById(currentUserObject.getUserId() != null ? currentUserObject.getUserId() : (long) 0)
            .orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Map<String, Object> d = new HashedMap<>();
        IntakeProgramSubmission e = null;
        if (phase.contains(Constant.ASSESSMENT.toString())) {
            e = intakeProgramSubmissionRepository.findByIdAndIntakeProgram_IdAndUsers_Id(submissionId,
                intakeProgramId, user.getId());
        } else if (phase.contains(Constant.BOOTCAMP.toString())) {
            e = intakeProgramSubmissionRepository.findByIdAndIntakeProgram_IdAndBootcampUsers_Id(submissionId,
                intakeProgramId, user.getId());
        } else if (phase.contains(Constant.SCREENING.toString())) {
            e = intakeProgramSubmissionRepository.findByIdAndIntakeProgram_IdAndScreeningEvaluators_Id(submissionId,
                intakeProgramId, user.getId());
            if (Objects.nonNull(e)) {
                EvaluationSummary evaluationSummary = e.getEvaluationSummary();
                if (Objects.nonNull(evaluationSummary) && evaluationJudgeSummaryRepository
                    .existsBySubmittedJudge_IdAndEvaluationSummary_Id(user.getId(), evaluationSummary.getId()))
                    e = null;
            }
        }
        if (Objects.nonNull(e)) {
            d.put("submissionId", e.getId());
            d.put("phase", e.getPhase());
            d.put("email", e.getEmail());
            d.put("jsonRegistrationForm", e.getJsonRegistrationForm());
            d.put("jsonAssessmentEvaluationForm", e.getJsonAssessmentEvaluationForm());
            d.put("jsonBootcampEvaluationForm", e.getJsonBootcampEvaluationForm());
            d.put("jsonProgressReport", e.getJsonProgressReport());
            d.put("jsonScreeningEvaluationForm", Objects.nonNull(e.getIntakeProgram().getScreeningEvaluationForm()) ?
                e.getIntakeProgram().getScreeningEvaluationForm().getJsonForm() : "");
            d.put("jsonProfileCard", null);
            d.put("evaluationStartedOn",
                e.getEvaluationStartedOn() != null ? e.getEvaluationStartedOn().toInstant().toEpochMilli()
                    : null);
            d.put("language", e.getLanguage());
            d.put("submitted", false);
            if (Objects.nonNull(e.getProfileInfoJson()))
                e.setProfileInfoJson(e.getProfileInfoJson().replace("custom-mobile", "custom-text"));
            d.put("profileInfoJson", e.getProfileInfoJson());
            d.put("proceedSummary", false);
            d.put("evaluationSubmissionId", 0);
            d.put("startupName", e.getStartupName());
            if ((e.getIntakeProgram() != null) && (e.getIntakeProgram().getProfileCard() != null)) {
                d.put("jsonProfileCard", e.getIntakeProgram().getProfileCard().getJsonForm());
            }
            List<EvaluationSummary> esls = evaluationSummaryRepository.findByIntakeProgram_IdAndPhaseAndEmail(
                intakeProgramId,
                e.getPhase().contains(Constant.ASSESSMENT.toString()) ? Constant.ASSESSMENT.toString()
                    : Constant.BOOTCAMP.toString(),
                e.getEmail());
            if (!esls.isEmpty()) {
                EvaluationSummary es = esls.get(0);
                d.put("evaluationSubmissionId", es.getId());
                List<EvaluatorMarksDTO> evaluatorMarksDTOS;
                if (e.getPhase().contains(Constant.ASSESSMENT.toString())) {
                    evaluatorMarksDTOS = new Gson().fromJson(es.getJudgesMarks(), new TypeToken<ArrayList<EvaluatorMarksDTO>>() {
                    }.getType());
                } else {
                    evaluatorMarksDTOS = new Gson().fromJson(es.getJudgeBootcampMarks(), new TypeToken<ArrayList<EvaluatorMarksDTO>>() {
                    }.getType());
                }
                if (evaluatorMarksDTOS.stream().anyMatch(dto -> dto.getEvaluatorId().equals(user.getId()))) {
                    d.put("submitted", true);
                } else {
                    d.put("submitted", false);
                }
                if (e.getPhase().contains(Constant.ASSESSMENT.toString())) {
                    Set<User> assUsers = e.getUsers();
                    if (evaluatorMarksDTOS.size() == assUsers.size()) {
                        d.put("proceedSummary", true);
                    }
                } else {
                    Set<User> bootUsers = e.getBootcampUsers();
                    if (evaluatorMarksDTOS.size() == bootUsers.size()) {
                        d.put("proceedSummary", true);
                    }
                }
            }
        }
        return d;
    }

    @Override
    public Map<String, Object> evaluationNextItemInQueue(CurrentUserObject currentUserObject) {
        Map<String, Object> result = new HashedMap<>();
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        IntakeProgramSubmission application = intakeProgramSubmissionRepository.findScreeningEvaluationQueue(user.getId())
            .orElse(intakeProgramSubmissionRepository.findAssessmentEvaluationQueue(user.getId())
                .orElse(intakeProgramSubmissionRepository.findBootcampEvaluationQueue(user.getId())
                    .orElse(null)));
        if (Objects.isNull(application)) {
            return result;
        }
        IntakeProgram intakeProgram = application.getIntakeProgram();
        result.put("submissionId", application.getId());
        result.put("startupName", application.getStartupName());
        result.put("phase", application.getPhase());
        result.put("email", application.getEmail());
        result.put("jsonRegistrationForm", application.getJsonRegistrationForm());
        result.put("jsonAssessmentEvaluationForm", application.getJsonAssessmentEvaluationForm());
        result.put("jsonBootcampEvaluationForm", application.getJsonBootcampEvaluationForm());
        result.put("jsonProgressReport", application.getJsonProgressReport());
        result.put("jsonScreeningEvaluationForm", intakeProgram.getScreeningEvaluationForm().getJsonForm());
        result.put("intakeId", application.getIntakeProgram().getId());
        result.put("evaluationStartedOn",
            application.getEvaluationStartedOn() != null ? application.getEvaluationStartedOn().toInstant().toEpochMilli()
                : null);
        result.put("language", application.getLanguage());
        result.put("submitted", false);
        result.put("profileInfoJson", application.getProfileInfoJson());
        result.put("proceedSummary", false);
        result.put("evaluationSubmissionId", 0);
        return result;
    }

    @Transactional
    @Override
    public Map<String, Object> currentEvaluationStatus(CurrentUserObject currentUserObject) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        List<ProjectIntakeProgramSubmission> assessmentDetails = intakeProgramSubmissionRepository
            .findByPhaseAndUsers_Id(Constant.ASSESSMENT_EVALUATION_START.toString(), user.getId());
        List<ProjectIntakeProgramSubmission> bootcampDetails = intakeProgramSubmissionRepository
            .findByPhaseAndBootcampUsers_Id(Constant.BOOTCAMP_EVALUATION_START.toString(), user.getId());
        Map<String, Object> d = new HashedMap<>();
        d.put("assessmentDetails", assessmentDetails);
        d.put("bootcampDetails", bootcampDetails);
        return d;
    }

    @Transactional
    @Override
    public ResponseEntity<?> getAssessments(CurrentUserObject currentUserObject, String filterBy, String filterKeyword,
                                            Long filterDate, Pageable paging) {
        return null;
    }

    @Transactional
    @Override
    public ResponseEntity<?> getBootcamps(CurrentUserObject currentUserObject, String filterBy, String filterKeyword,
                                          Long filterDate, Pageable paging) {
        return null;
    }

    @Transactional
    @Override
    public ResponseEntity<?> getAssessmentSummaryStatus(CurrentUserObject currentUserObject, Long judgeId,
                                                        Long intakeProgramId) {
        return null;
    }

    @Transactional
    @Override
    public ResponseEntity<?> getBootcampSummaryStatus(CurrentUserObject currentUserObject, Long judgeId,
                                                      Long intakeProgramId) {
        return null;
    }

    @Transactional
    @Override
    public ResponseEntity<?> getEvaluationsSummaryStatus(CurrentUserObject currentUserObject, Long judgeId,
                                                         Long intakeProgramId) {
        return null;
    }

    @Transactional
    @Override
    public ResponseEntity<?> judgeAssessementEvaluations(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                         Pageable paging, String filterKeyword, String filterBy) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<IntakeProgramSubmission> list = intakeProgramSubmissionRepository
            .getJudgeAssessmentEvaluations(intakeProgramId, Constant.ASSESSMENT.toString(),
                Arrays.asList(Constant.ASSESSMENT_EVALUATE.toString(),
                    Constant.ASSESSMENT_EVALUATION_START.toString(),
                    Constant.ASSESSMENT_EVALUATION_STOP.toString()),
                filterBy, filterKeyword, paging);
        Set<Long> intakeProgramSubmissionIds = list.getContent().stream().map(BaseEntity::getId)
            .collect(Collectors.toSet());
        List<ProjectEvaluationJudgeSummary> ll = evaluationJudgeSummaryRepository
            .findByIntakeProgramSubmission_IdInAndSubmittedJudge_Id(intakeProgramSubmissionIds, user.getId());
        Page<GetIntakeProgramSubmissionForJudgeDto> ls = new PageImpl<>(
            list.getContent().stream().map(i -> {
                GetIntakeProgramSubmissionForJudgeDto e = new GetIntakeProgramSubmissionForJudgeDto();
                e.setEmail(i.getEmail());
                e.setId(i.getId());
                e.setIntakeProgramId(i.getIntakeProgram() != null ? i.getIntakeProgram().getId() : null);
                e.setIntakeProgramName(i.getIntakeProgram() != null ? i.getIntakeProgram().getProgramName() : null);
                e.setInterviewEnd(
                    i.getInterviewEnd() != null ? i.getInterviewEnd().toInstant().toEpochMilli() : null);
                e.setInterviewEndBootcamp(
                    i.getInterviewEndBootcamp() != null ? i.getInterviewEndBootcamp().toInstant().toEpochMilli()
                        : null);
                e.setInterviewStart(
                    i.getInterviewStart() != null ? i.getInterviewStart().toInstant().toEpochMilli() : null);
                e.setInterviewStartBootcamp(i.getInterviewStartBootcamp() != null
                    ? i.getInterviewStartBootcamp().toInstant().toEpochMilli()
                    : null);
                e.setIsAbsent(i.getIsAbsent() != null && i.getIsAbsent());
                e.setIsAbsentBootcamp(
                    i.getIsAbsentBootcamp() != null && i.getIsAbsentBootcamp());
                e.setPhase(i.getPhase());
                e.setProfileInfoJson(i.getProfileInfoJson());
                e.setStartupName(i.getStartupName());
                ProjectEvaluationJudgeSummary ii = ll.stream()
                    .filter(c -> i.getId().equals(c.getIntakeProgramSubmissionId())).findAny().orElse(null);
                e.setSubmissionJson(null);
                if (ii != null) {
                    e.setSubmissionJson(ii.getJsonForm());
                }
                return e;
            }).collect(Collectors.toList()), paging, list.getTotalElements());
        return ResponseWrapper.response(ls);
    }

    @Transactional
    @Override
    public ResponseEntity<?> judgeBootcampEvaluations(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                      Pageable paging, String filterKeyword, String filterBy) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<IntakeProgramSubmission> list = intakeProgramSubmissionRepository.getJudgeBootcampEvaluations(
            intakeProgramId, Constant.BOOTCAMP.toString(), Arrays.asList(Constant.BOOTCAMP_EVALUATE.toString(),
                Constant.BOOTCAMP_EVALUATION_START.toString(), Constant.BOOTCAMP_EVALUATION_STOP.toString()),
            filterBy, filterKeyword, paging);
        Set<Long> intakeProgramSubmissionIds = list.getContent().stream().map(BaseEntity::getId)
            .collect(Collectors.toSet());
        if (intakeProgramSubmissionIds.isEmpty()) {
            intakeProgramSubmissionIds.add(0L);
        }
        List<ProjectEvaluationJudgeSummary> ll = evaluationJudgeSummaryRepository
            .findByIntakeProgramSubmission_IdInAndSubmittedJudge_Id(intakeProgramSubmissionIds, user.getId());
        Page<GetIntakeProgramSubmissionForJudgeDto> ls = new PageImpl<>(
            list.getContent().stream().map(i -> {
                GetIntakeProgramSubmissionForJudgeDto e = new GetIntakeProgramSubmissionForJudgeDto();
                e.setEmail(i.getEmail());
                e.setId(i.getId());
                e.setIntakeProgramId(i.getIntakeProgram() != null ? i.getIntakeProgram().getId() : null);
                e.setIntakeProgramName(i.getIntakeProgram() != null ? i.getIntakeProgram().getProgramName() : null);
                e.setInterviewEnd(
                    i.getInterviewEnd() != null ? i.getInterviewEnd().toInstant().toEpochMilli() : null);
                e.setInterviewEndBootcamp(
                    i.getInterviewEndBootcamp() != null ? i.getInterviewEndBootcamp().toInstant().toEpochMilli()
                        : null);
                e.setInterviewStart(
                    i.getInterviewStart() != null ? i.getInterviewStart().toInstant().toEpochMilli() : null);
                e.setInterviewStartBootcamp(i.getInterviewStartBootcamp() != null
                    ? i.getInterviewStartBootcamp().toInstant().toEpochMilli()
                    : null);
                e.setIsAbsent(i.getIsAbsent() != null && i.getIsAbsent());
                e.setIsAbsentBootcamp(
                    i.getIsAbsentBootcamp() != null && i.getIsAbsentBootcamp());
                e.setPhase(i.getPhase());
                e.setProfileInfoJson(i.getProfileInfoJson());
                e.setStartupName(i.getStartupName());
                ProjectEvaluationJudgeSummary ii = ll.stream()
                    .filter(c -> i.getId().equals(c.getIntakeProgramSubmissionId())).findAny().orElse(null);
                e.setSubmissionJson(null);
                if (ii != null) {
                    e.setSubmissionJson(ii.getJsonFormBootcamp());
                }
                return e;
            }).collect(Collectors.toList()), paging, list.getTotalElements());
        return ResponseWrapper.response(ls);

    }

    @Transactional
    @Override
    public ResponseEntity<?> getProceedToSummayStatus(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                      String phase) {
        Map<String, Object> e = new HashedMap<>();
        e.put("proceedSummaryAssessment", false);
        e.put("proceedSummaryBootcamp", false);
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        KeyValue kv = keyValueRepository.findByKeyName("judge_" + user.getId() + "_intake_" + intakeProgramId
            + "_proceed_summary_" + phase.toLowerCase());
        if (kv != null) {
            return ResponseWrapper.response(e);
        }
        long ipss = intakeProgramSubmissionRepository
            .countByIntakeProgram_IdAndPhaseContainingIgnoreCase(intakeProgramId, phase);
        if (ipss > 0) {
            List<Long> ll;
            if (phase.equals(Constant.BOOTCAMP.toString())) {
                ll = intakeProgramSubmissionRepository.getFinalSummaryBootcamp(intakeProgramId,
                    (phase + "_END").toLowerCase());
                if (ll.size() == ipss) {
                    e.put("proceedSummaryBootcamp", true);
                }
            } else {
                ll = intakeProgramSubmissionRepository.getFinalSummary(intakeProgramId,
                    (phase + "_END").toLowerCase());
                if (ll.size() == ipss) {
                    e.put("proceedSummaryAssessment", true);
                }
            }
        }
        return ResponseWrapper.response(e);
    }

    @Transactional
    @Override
    public ResponseEntity<?> judgeAssessementApps(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                  Pageable paging, String filterKeyword, String filterBy) {
        Page<IntakeProgramSubmission> list = intakeProgramSubmissionRepository
            .getJudgeAssessmentEvaluations(intakeProgramId, Constant.ASSESSMENT.toString(),
                Arrays.asList(Constant.ASSESSMENT_EVALUATE.toString(),
                    Constant.ASSESSMENT_EVALUATION_START.toString(),
                    Constant.ASSESSMENT_EVALUATION_STOP.toString()),
                filterBy, filterKeyword, paging);
        Page<GetIntakeProgramSubmissionForJudgeAppDto> ls = list
            .map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionForJudgeAppDto);
        return ResponseWrapper.response(ls);
    }

    @Transactional
    @Override
    public ResponseEntity<?> judgeBootcampApps(CurrentUserObject currentUserObject, Long intakeProgramId,
                                               Pageable paging, String filterKeyword, String filterBy) {
        Page<IntakeProgramSubmission> list = intakeProgramSubmissionRepository.getJudgeBootcampEvaluations(
            intakeProgramId, Constant.BOOTCAMP.toString(), Arrays.asList(Constant.BOOTCAMP_EVALUATE.toString(),
                Constant.BOOTCAMP_EVALUATION_START.toString(), Constant.BOOTCAMP_EVALUATION_STOP.toString()),
            filterBy, filterKeyword, paging);
        Page<GetIntakeProgramSubmissionForJudgeAppDto> ls = list
            .map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionForJudgeAppDto);
        return ResponseWrapper.response(ls);

    }

    @Transactional
    @Override
    public ResponseEntity<Object> generateSummaryAssessmentBootcamp(CurrentUserObject currentUserObject,
                                                                    Long intakeProgramId, String phase) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        KeyValue keyValue = new KeyValue();// keyValueRepository.findByKeyName();
        keyValue.setKeyName("judge_" + user.getId() + "_intake_" + intakeProgramId + "_proceed_summary_"
            + phase.toLowerCase());
        KeyValue e = keyValueRepository.save(keyValue);
        Map<String, Object> d = new HashedMap<>();
        d.put("keyName", e.getKeyName());
        return ResponseWrapper.response(d);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getFinalSummayStatus(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                  String phase) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        KeyValue e = keyValueRepository.findByKeyName("judge_" + user.getId() + "_intake_" + intakeProgramId
            + "_proceed_summary_" + phase.toLowerCase());
        if (Objects.nonNull(e)) {
            Map<String, Object> d = new HashedMap<>();
            d.put("keyName", e.getKeyName());
            return ResponseWrapper.response(d);
        }
        return ResponseWrapper.response(null);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getAllFinalSummayStatus(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                     String phase) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        List<Long> ipss;
        Map<String, Object> d = new HashedMap<>();
        d.put("allAssessment", false);
        d.put("allBootcamp", false);
        if (phase.equals(Constant.ASSESSMENT.toString())) {
            ipss = intakeProgramSubmissionRepository.getAssessmentIds(intakeProgramId, (phase + "_END").toLowerCase());
            if (!ipss.isEmpty()) {
                Set<Long> ipss2 = new HashSet<>(ipss);
                Set<Long> tt = intakeProgramSubmissionRepository.getAllUserAssessment(ipss2);
                Set<String> kv = new HashSet<>();
                for (Long l : tt) {
                    kv.add("judge_" + l + "_intake_" + intakeProgramId + "_proceed_summary_" + phase.toLowerCase());
                }
                if (!kv.isEmpty()) {
                    long allUserCount = keyValueRepository.countByKeyNameIn(kv);
                    if (allUserCount == kv.size()) {
                        d.put("allAssessment", true);
                    }
                }
            }
        } else {
            ipss = intakeProgramSubmissionRepository.getBootcampIds(intakeProgramId, (phase + "_END").toLowerCase());
            if (!ipss.isEmpty()) {
                Set<Long> ipss2 = new HashSet<>(ipss);
                Set<Long> tt = intakeProgramSubmissionRepository.getAllUserBootcamp(ipss2);
                Set<String> kv = new HashSet<>();
                for (Long l : tt) {
                    kv.add("judge_" + l + "_intake_" + intakeProgramId + "_proceed_summary_" + phase.toLowerCase());
                }
                if (!kv.isEmpty()) {
                    long allUserCount = keyValueRepository.countByKeyNameIn(kv);
                    if (allUserCount == kv.size()) {
                        d.put("allBootcamp", true);
                    }
                }
            }
        }
        return ResponseWrapper.response(d);
    }

    @Transactional
    @Override
    public ResponseEntity<?> judgeAssessmentAllSummary(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                       Pageable paging, String filterKeyword, String filterBy) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<EvaluationSummary> ls = evaluationSummaryRepository.getJudgeSummaryForAssessment(intakeProgramId,
            "ASSESSMENT", filterBy, filterKeyword, paging);
        Page<EvaluationSummaryDto> list = ls.map(evaluationSummaryMapper::toEvaluationSummaryDto);
        return ResponseWrapper.response(list);

    }

    @Transactional
    @Override
    public ResponseEntity<?> judgeBootcampAllSummary(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                     Pageable paging, String filterKeyword, String filterBy) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<EvaluationSummary> ls = evaluationSummaryRepository.getJudgeSummaryForBootcamp(intakeProgramId, "BOOTCAMP",
            filterBy, filterKeyword, paging);
        Page<EvaluationSummaryDto> list = ls.map(evaluationSummaryMapper::toEvaluationSummaryDto);
        return ResponseWrapper.response(list);
    }

}
