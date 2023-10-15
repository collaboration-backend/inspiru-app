package com.stc.inspireu.services.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.EvaluationSubmissionDto;
import com.stc.inspireu.dtos.EvaluatorMarksDTO;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.EvaluationSummaryService;
import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.utils.RoleName;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EvaluationSummaryServiceImpl implements EvaluationSummaryService {

    private final EvaluationSummaryRepository evaluationSummaryRepository;
    private final NotificationService notificationService;
    private final IntakeProgramRepository intakeProgramRepository;
    private final IntakeProgramSubmissionRepository intakeProgramSubmissionRepository;
    private final UserRepository userRepository;
    private final EvaluationJudgeSummaryRepository evaluationJudgeSummaryRepository;

    @Transactional
    @Override
    public Object screeningEvaluationSubmission(CurrentUserObject currentUserObject, EvaluationSubmissionDto evaluationSubmissionDto) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        IntakeProgram intakeProgram = intakeProgramRepository
            .findById(evaluationSubmissionDto.getIntakeProgramId()).orElseThrow(() -> ItemNotFoundException.builder("Intake").build());

        IntakeProgramSubmission intakeProgramSubmission = intakeProgramSubmissionRepository
            .findById(evaluationSubmissionDto.getSubmissionId()).orElseThrow(() -> ItemNotFoundException.builder("Submission").build());
        if (intakeProgramSubmission.getPhase().equals(Constant.SCREENING_EVALUATION_STOP.toString()))
            throw new CustomRunTimeException("Evaluation stopped for this application");
        if (intakeProgramSubmission.getPhase().equals(Constant.SCREENING_EVALUATION_COMPLETED.toString()))
            throw new CustomRunTimeException("Evaluation completed for this application");
        if (intakeProgramSubmission.getPhase().equals(Constant.REGISTRATION.toString()))
            throw new CustomRunTimeException("Evaluation has not started for this application");
        Set<User> evaluators = intakeProgramSubmission.getScreeningEvaluators();
        if (!evaluators.contains(user))
            throw new CustomRunTimeException("You are not assigned to evaluate this application");/**/
        EvaluationSummary es = evaluationSummaryRepository.findByIntakeProgramSubmission(intakeProgramSubmission);
        List<EvaluationJudgeSummary> judgeSummaries = new ArrayList<>();
        if (Objects.isNull(es))
            es = new EvaluationSummary();
        else {
            if (evaluationJudgeSummaryRepository.existsBySubmittedJudge_IdAndEvaluationSummary_Id(user.getId(), es.getId()))
                throw new CustomRunTimeException("You've already evaluated this application");
            judgeSummaries = evaluationJudgeSummaryRepository.findAllByEvaluationSummary_Id(es.getId());
        }
        int c = 0;
        float total = (Objects.nonNull(es.getScreeningTotal()) ? es.getScreeningTotal() : 0);
        float average = (Objects.nonNull(es.getScreeningAverage()) ? es.getScreeningAverage() : 0);
        List<EvaluatorMarksDTO> evaluatorMarksDTOS = new ArrayList<>();
        if (Objects.nonNull(es.getScreeningEvaluatorsMarks()) && !es.getScreeningEvaluatorsMarks().isEmpty())
            evaluatorMarksDTOS = new Gson().fromJson(es.getScreeningEvaluatorsMarks(), new TypeToken<ArrayList<EvaluatorMarksDTO>>() {
            }.getType());
        if (evaluatorMarksDTOS.stream().anyMatch(e -> e.getEvaluatorId().equals(user.getId())))
            throw new CustomRunTimeException("User has already submitted evaluation for this startup");
        JSONObject marks1 = new JSONObject(evaluationSubmissionDto.getJsonFormValues());
        Map<String, Double> weightagesMap = new Gson().fromJson(marks1.get("_weightages").toString(), HashMap.class);
        JSONArray objectKeys = (JSONArray) marks1.get("objectKeys");
        float rowTotal = 0;
        for (Object answer : objectKeys) {
            JSONObject ans = (JSONObject) answer;
            if (ans.get("value") instanceof Number) {
                BigDecimal weightage = BigDecimal.valueOf(weightagesMap.get(ans.get("key"))).divide(new BigDecimal(5), 2, RoundingMode.HALF_EVEN);
                float t = ((Number) ans.get("value")).floatValue();
                t = weightage.multiply(new BigDecimal(t)).setScale(2, RoundingMode.HALF_EVEN).floatValue();
                rowTotal = (rowTotal + t);
                total = (total + t);
                c++;
            }
        }
        evaluatorMarksDTOS.add(new EvaluatorMarksDTO(user.getId(), user.getAlias(), rowTotal));
        try {
            average = total / evaluatorMarksDTOS.size();
        } catch (Exception e) {
        }
        es.setStatus(Constant.SUMMARY.toString());
        es.setScreeningTotal(total);
        es.setScreeningAverage(average);
        es.setEmail(intakeProgramSubmission.getEmail());
        es.setIntakeProgram(intakeProgram);
        es.setScreeningEvaluatorsMarks(new Gson().toJson(evaluatorMarksDTOS));
        es.setJudgesMarks(new JSONArray().toString());
        es.setSubmittedUser(user);
        es.setJudgeBootcampMarks(new JSONArray().toString());
        es.setStartupName(intakeProgramSubmission.getStartupName());
        es.setProfileCard(intakeProgramSubmission.getProfileInfoJson());
        es.setIntakeProgramSubmission(intakeProgramSubmission);
        if (judgeSummaries.size() + 1 == evaluators.size()) {
            intakeProgramSubmission.setPhase(Constant.SCREENING_EVALUATION_COMPLETED.toString());
            intakeProgramSubmission.setScreeningEvaluationEndedOn(new Date());
            es.setPhase(Constant.SCREENING_EVALUATION_COMPLETED.toString());
        } else es.setPhase(evaluationSubmissionDto.getPhase());
        intakeProgramSubmission.setEvaluationSummary(es);
        intakeProgramSubmission = intakeProgramSubmissionRepository.save(intakeProgramSubmission);
        es = intakeProgramSubmission.getEvaluationSummary();
        Map<String, Object> data = assessmentResponseBuilder(true, es, evaluationSubmissionDto);
        EvaluationJudgeSummary evaluationJudgeSummary1 = new EvaluationJudgeSummary();
        evaluationJudgeSummary1.setEvaluationSummary(es);
        evaluationJudgeSummary1.setScreeningEvaluationJsonForm(evaluationSubmissionDto.getJsonForm());
        evaluationJudgeSummary1.setSubmittedJudge(user);
        evaluationJudgeSummary1.setIntakeProgramSubmission(intakeProgramSubmission);
        evaluationJudgeSummaryRepository.save(evaluationJudgeSummary1);
        return data;
    }

    @Transactional
    @Override
    public Object assessmentEvaluationSubmission(CurrentUserObject currentUserObject,
                                                 EvaluationSubmissionDto evaluationSubmissionDto) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<IntakeProgram> intakeProgram = intakeProgramRepository
            .findById(evaluationSubmissionDto.getIntakeProgramId());
        IntakeProgramSubmission intakeProgramSubmission = intakeProgramSubmissionRepository
            .findById(evaluationSubmissionDto.getSubmissionId()).orElseThrow(() -> ItemNotFoundException.builder("Application").build());
        if (intakeProgram.isPresent()
            && (user.getRole().getRoleName().contains(RoleName.ROLE_STC_JUDGES)
            || user.getRole().getRoleName().contains(RoleName.ROLE_NON_STC_JUDGES))) {
            if (!intakeProgramSubmission.getPhase().equals(Constant.ASSESSMENT_EVALUATION_START.toString()))
                throw new CustomRunTimeException("Evaluation either stopped or completed for this startup");
            Set<User> assessmentJudges = intakeProgramSubmission.getUsers();
            EvaluationSummary evaluationSummary = evaluationSummaryRepository.findByIntakeProgramSubmission(intakeProgramSubmission);
            List<EvaluatorMarksDTO> evaluatorMarksDTOS = new ArrayList<>();
            if (Objects.nonNull(evaluationSummary.getJudgesMarks()) && !evaluationSummary.getJudgesMarks().isEmpty())
                evaluatorMarksDTOS = new Gson().fromJson(evaluationSummary.getJudgesMarks(), new TypeToken<ArrayList<EvaluatorMarksDTO>>() {
                }.getType());
            if (evaluatorMarksDTOS.stream().anyMatch(e -> e.getEvaluatorId().equals(user.getId())))
                throw new CustomRunTimeException("User has already submitted evaluation for this startup");
            int c = 0;
            float total = (Objects.nonNull(evaluationSummary.getTotal()) ? evaluationSummary.getTotal() : 0);
            float average = (Objects.nonNull(evaluationSummary.getAvarage()) ? evaluationSummary.getAvarage() : 0);
            Set<User> evaluators = intakeProgramSubmission.getUsers();
            JSONObject marks = new JSONObject(evaluationSubmissionDto.getJsonFormValues());
            Iterator<String> keys = marks.keys();
            float rowTotal = 0;
            while (keys.hasNext()) {
                String key = keys.next();
                Object val = marks.get(key);

                if (val instanceof Number) {
                    float t = ((Number) val).floatValue();
                    rowTotal = rowTotal + t;
                    total = total + t;
                    c++;
                }
            }
            evaluatorMarksDTOS.add(new EvaluatorMarksDTO(user.getId(), user.getAlias(), rowTotal));
            try {
                average = total / evaluatorMarksDTOS.size();
            } catch (Exception ignored) {
            }
            evaluationSummary.setTotal(total);
            evaluationSummary.setAvarage(average);
            evaluationSummary.setEmail(evaluationSubmissionDto.getEmail());
            evaluationSummary.setIntakeProgram(intakeProgram.get());
            evaluationSummary.setJudgesMarks(new Gson().toJson(evaluatorMarksDTOS));
            if (evaluators.size() == evaluatorMarksDTOS.size()) {
                evaluationSummary.setPhase(Constant.ASSESSMENT_EVALUATION_COMPLETED.toString());
                intakeProgramSubmission.setPhase(Constant.ASSESSMENT_EVALUATION_COMPLETED.toString());
            } else evaluationSummary.setPhase(evaluationSubmissionDto.getPhase());
            evaluationSummary.setSubmittedUser(user);
            evaluationSummary.setJudgeBootcampMarks(new JSONArray().toString());
            evaluationSummary.setProfileCard(intakeProgramSubmission.getProfileInfoJson());
            boolean completed = true;
            for (User u : assessmentJudges) {
                completed = false;
                break;
            }
            intakeProgramSubmission.setEvaluationSummary(evaluationSummary);
            EvaluationSummary s = intakeProgramSubmissionRepository.save(intakeProgramSubmission).getEvaluationSummary();

            Map<String, Object> data = assessmentResponseBuilder(completed, s, evaluationSubmissionDto);
            notificationService.evaluationFormSubmitted(user);

            EvaluationJudgeSummary evaluationJudgeSummary = evaluationJudgeSummaryRepository
                .findBySubmittedJudge_IdAndEvaluationSummary_Id(user.getId(), s.getId());

            if (evaluationJudgeSummary != null) {
                evaluationJudgeSummary.setJsonForm(evaluationSubmissionDto.getJsonForm());
                evaluationJudgeSummaryRepository.save(evaluationJudgeSummary);
            } else {
                EvaluationJudgeSummary evaluationJudgeSummary1 = new EvaluationJudgeSummary();
                evaluationJudgeSummary1.setEvaluationSummary(s);
                evaluationJudgeSummary1.setJsonForm(evaluationSubmissionDto.getJsonForm());
                evaluationJudgeSummary1.setSubmittedJudge(user);
                evaluationJudgeSummary1.setIntakeProgramSubmission(intakeProgramSubmission);
                evaluationJudgeSummaryRepository.save(evaluationJudgeSummary1);
            }
            return data;
        }
        return null;
    }

    @Transactional
    @Override
    public Object bootcampEvaluationSubmission(CurrentUserObject currentUserObject,
                                               EvaluationSubmissionDto evaluationSubmissionDto) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<IntakeProgram> intakeProgram = intakeProgramRepository
            .findById(evaluationSubmissionDto.getIntakeProgramId());
        IntakeProgramSubmission intakeProgramSubmission = intakeProgramSubmissionRepository
            .findById(evaluationSubmissionDto.getSubmissionId()).orElseThrow(() -> ItemNotFoundException.builder("Intake").build());
        if (intakeProgram.isPresent()
            && (user.getRole().getRoleName().contains(RoleName.ROLE_STC_JUDGES)
            || user.getRole().getRoleName().contains(RoleName.ROLE_NON_STC_JUDGES))) {
            if (intakeProgramSubmission.getPhase().equals(Constant.BOOTCAMP_EVALUATION_STOP.toString()))
                throw new CustomRunTimeException("Evaluation stopped for this startup");
            if (intakeProgramSubmission.getPhase().equals(Constant.BOOTCAMP_EVALUATION_COMPLETED.toString()))
                throw new CustomRunTimeException("Evaluation completed for this startup");
            Set<User> bootcampJudges = intakeProgramSubmission.getBootcampUsers();

            List<EvaluationSummary> ls = evaluationSummaryRepository.findByIntakeProgram_IdAndEmailAndPhase(
                intakeProgram.get().getId(), evaluationSubmissionDto.getEmail(),
                evaluationSubmissionDto.getPhase());

            if (ls.size() > 0) {
                EvaluationSummary evaluationSummary = evaluationSummaryRepository.findById(ls.get(0).getId()).get();
                boolean completed = true;
                for (User u : bootcampJudges) {
                    completed = false;
                    break;
                }
                intakeProgramSubmission.setEvaluationSummary(setBootcampValuesToEvaluationSummary(evaluationSummary, user, evaluationSubmissionDto,
                    intakeProgram.get(), intakeProgramSubmission, bootcampJudges.size()));
                intakeProgramSubmission = intakeProgramSubmissionRepository.save(intakeProgramSubmission);
                EvaluationSummary s = intakeProgramSubmission.getEvaluationSummary();
                Map<String, Object> data = assessmentResponseBuilder(completed, s, evaluationSubmissionDto);
                notificationService.evaluationFormSubmitted(user);

                EvaluationJudgeSummary evaluationJudgeSummary = evaluationJudgeSummaryRepository
                    .findBySubmittedJudge_IdAndEvaluationSummary_Id(user.getId(), s.getId());

                if (evaluationJudgeSummary != null) {
                    evaluationJudgeSummary.setJsonFormBootcamp(evaluationSubmissionDto.getJsonForm());
                    evaluationJudgeSummaryRepository.save(evaluationJudgeSummary);
                } else {
                    evaluationJudgeSummaryRepository.save(setBootcampAssessmentEvaluationJudgeSummary(s, evaluationSubmissionDto.getJsonForm(), user, intakeProgramSubmission));
                }
                return data;
            } else {
                boolean completed = true;
                for (User u : bootcampJudges) {
                    completed = false;
                    break;
                }
                intakeProgramSubmission.setEvaluationSummary(setBootcampValuesToNewEvaluationSummary(user, evaluationSubmissionDto, intakeProgram.get(),
                    intakeProgramSubmission, bootcampJudges.size()));
                intakeProgramSubmission = intakeProgramSubmissionRepository.save(intakeProgramSubmission);
                EvaluationSummary s = intakeProgramSubmission.getEvaluationSummary();
                Map<String, Object> data = assessmentResponseBuilder(completed, s, evaluationSubmissionDto);
                notificationService.evaluationFormSubmitted(user);

                EvaluationJudgeSummary evaluationJudgeSummary = evaluationJudgeSummaryRepository
                    .findBySubmittedJudge_IdAndEvaluationSummary_Id(user.getId(), s.getId());

                if (evaluationJudgeSummary != null) {
                    evaluationJudgeSummary.setJsonFormBootcamp(evaluationSubmissionDto.getJsonForm());
                    evaluationJudgeSummaryRepository.save(evaluationJudgeSummary);
                } else {
                    evaluationJudgeSummaryRepository.save(setBootcampAssessmentEvaluationJudgeSummary(s, evaluationSubmissionDto.getJsonForm(), user, intakeProgramSubmission));
                }
                return data;
            }
        }
        return null;
    }

    private EvaluationSummary setBootcampValuesToNewEvaluationSummary(User user,
                                                                      EvaluationSubmissionDto evaluationSubmissionDto,
                                                                      IntakeProgram intakeProgram,
                                                                      IntakeProgramSubmission intakeProgramSubmission,
                                                                      int bootcampJudgesSize) {
        EvaluationSummary es = new EvaluationSummary();
        float total = 0;
        float average = 0;
        JSONObject marks1 = new JSONObject(evaluationSubmissionDto.getJsonFormValues());
        Iterator<String> keys1 = marks1.keys();
        List<EvaluatorMarksDTO> evaluatorMarksDTOS = new ArrayList<>();
        while (keys1.hasNext()) {
            String key = keys1.next();
            Object val = marks1.get(key);
            if (val instanceof Number) {
                float t = ((Number) val).floatValue();
                total = total + t;
            }
        }
        try {
            average = total / bootcampJudgesSize;
        } catch (Exception ignored) {
        }
        evaluatorMarksDTOS.add(new EvaluatorMarksDTO(user.getId(), user.getAlias(), total));
        es.setBootcampTotal(total);
        es.setBootcampAvarage(average);
        es.setEmail(evaluationSubmissionDto.getEmail());
        es.setIntakeProgram(intakeProgram);
        es.setJudgeBootcampMarks(evaluationSubmissionDto.getJsonFormValues());
        if (bootcampJudgesSize == evaluatorMarksDTOS.size()) {
            es.setPhase(Constant.BOOTCAMP_EVALUATION_COMPLETED.toString());
            es.setStatus(Constant.SUMMARY.toString());
            intakeProgramSubmission.setPhase(Constant.BOOTCAMP_EVALUATION_COMPLETED.toString());
        } else es.setPhase(evaluationSubmissionDto.getPhase());
        es.setSubmittedUser(user);
        try {
            JSONObject d = new JSONObject(evaluationSubmissionDto.getProfileCardForm());
            es.setProfileCard(d.toString());
        } catch (Exception e) {
            JSONObject d = new JSONObject(
                "{\"metadata\":{\"name\":\"\",\"description\":\"\"},\"form\":[{\"fieldGroup\":[]}]}");
            es.setProfileCard(d.toString());
        }
        return es;
    }

    private EvaluationSummary setBootcampValuesToEvaluationSummary(EvaluationSummary evaluationSummary,
                                                                   User user,
                                                                   EvaluationSubmissionDto evaluationSubmissionDto,
                                                                   IntakeProgram intakeProgram,
                                                                   IntakeProgramSubmission intakeProgramSubmission,
                                                                   int bootcampJudgesSize) {
        List<EvaluatorMarksDTO> evaluatorMarksDTOS = new ArrayList<>();
        if (Objects.nonNull(evaluationSummary.getJudgeBootcampMarks()) && !evaluationSummary.getJudgeBootcampMarks().isEmpty())
            evaluatorMarksDTOS = new Gson().fromJson(evaluationSummary.getJudgeBootcampMarks(), new TypeToken<ArrayList<EvaluatorMarksDTO>>() {
            }.getType());
        if (evaluatorMarksDTOS.stream().anyMatch(e -> e.getEvaluatorId().equals(user.getId())))
            throw new CustomRunTimeException("User has already submitted evaluation for this startup");
        float total = (Objects.nonNull(evaluationSummary.getBootcampTotal()) ? evaluationSummary.getBootcampTotal() : 0);
        float average = (Objects.nonNull(evaluationSummary.getBootcampAvarage()) ? evaluationSummary.getBootcampAvarage() : 0);

        JSONObject marks1 = new JSONObject(evaluationSubmissionDto.getJsonFormValues());

        Iterator<String> keys1 = marks1.keys();
        float rowTotal = 0;
        while (keys1.hasNext()) {
            String key = keys1.next();
            Object val = marks1.get(key);

            if (val instanceof Number) {
                float t = ((Number) val).floatValue();
                rowTotal = rowTotal + t;
                total = total + t;
            }
        }
        evaluatorMarksDTOS.add(new EvaluatorMarksDTO(user.getId(), user.getAlias(), rowTotal));
        try {
            average = total / evaluatorMarksDTOS.size();
        } catch (Exception ignored) {
        }
        evaluationSummary.setBootcampTotal(total);
        evaluationSummary.setBootcampAvarage(average);
        evaluationSummary.setEmail(evaluationSubmissionDto.getEmail());
        evaluationSummary.setIntakeProgram(intakeProgram);
        evaluationSummary.setJudgeBootcampMarks(new Gson().toJson(evaluatorMarksDTOS));
        if (bootcampJudgesSize == evaluatorMarksDTOS.size()) {
            evaluationSummary.setPhase(Constant.BOOTCAMP_EVALUATION_COMPLETED.toString());
            intakeProgramSubmission.setPhase(Constant.BOOTCAMP_EVALUATION_COMPLETED.toString());
            evaluationSummary.setStatus(Constant.SUMMARY.toString());
        } else evaluationSummary.setPhase(evaluationSubmissionDto.getPhase());
        evaluationSummary.setSubmittedUser(user);

        try {
            JSONObject d = new JSONObject(evaluationSubmissionDto.getProfileCardForm());
            evaluationSummary.setProfileCard(d.toString());
        } catch (Exception e) {
            JSONObject d = new JSONObject(
                "{\"metadata\":{\"name\":\"\",\"description\":\"\"},\"form\":[{\"fieldGroup\":[]}]}");
            evaluationSummary.setProfileCard(d.toString());
        }
        return evaluationSummary;
    }

    private EvaluationJudgeSummary setBootcampAssessmentEvaluationJudgeSummary(EvaluationSummary s, String jsonForm, User user, IntakeProgramSubmission intakeProgramSubmission) {
        EvaluationJudgeSummary evaluationJudgeSummary1 = new EvaluationJudgeSummary();
        evaluationJudgeSummary1.setEvaluationSummary(s);
        evaluationJudgeSummary1.setJsonFormBootcamp(jsonForm);
        evaluationJudgeSummary1.setSubmittedJudge(user);
        evaluationJudgeSummary1.setIntakeProgramSubmission(intakeProgramSubmission);
        return evaluationJudgeSummary1;
    }

    @Transactional
    @Override
    public Object bootcampEvaluationExit(CurrentUserObject currentUserObject,
                                         EvaluationSubmissionDto evaluationSubmissionDto) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<IntakeProgram> intakeProgram = intakeProgramRepository
            .findById(evaluationSubmissionDto.getIntakeProgramId());
        Optional<IntakeProgramSubmission> intakeProgramSubmission = intakeProgramSubmissionRepository
            .findById(evaluationSubmissionDto.getSubmissionId());
        if (intakeProgram.isPresent() && intakeProgramSubmission.isPresent()
            && (user.getRole().getRoleName().contains(RoleName.ROLE_STC_JUDGES)
            || user.getRole().getRoleName().contains(RoleName.ROLE_NON_STC_JUDGES))) {
            Set<User> bootcampJudges = intakeProgramSubmission.get().getBootcampUsers();
            List<EvaluationSummary> ls = evaluationSummaryRepository.findByIntakeProgram_IdAndEmailAndPhase(
                intakeProgram.get().getId(), evaluationSubmissionDto.getEmail(),
                evaluationSubmissionDto.getPhase());
            if (!ls.isEmpty()) {
                EvaluationSummary es = ls.get(0);
                int c = 0;
                float total = 0;
                float average = 0;
                JSONObject full = new JSONObject();
                JSONObject marks;
                try {
                    marks = new JSONObject(es.getJudgeBootcampMarks());
                } catch (Exception e) {
                    marks = new JSONObject();
                }
                Iterator<String> keys = marks.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object val = marks.get(key);
                    if (val instanceof Number) {
                        float t = ((Number) val).floatValue();
                        total = total + t;
                        c++;
                        full.put(key, val);
                    }
                }
                JSONObject marks1 = new JSONObject(evaluationSubmissionDto.getJsonFormValues());
                Iterator<String> keys1 = marks1.keys();
                while (keys1.hasNext()) {
                    String key = keys1.next();
                    Object val = marks1.get(key);
                    if (!full.has(key)) {
                        if (val instanceof Number) {
                            float t = ((Number) val).floatValue();
                            total = total + t;
                            c++;
                            full.put(key, val);
                        }
                    }
                }
                try {
                    average = total / c;
                } catch (Exception ignored) {
                }
                es.setBootcampTotal(total);
                es.setBootcampAvarage(average);
                es.setEmail(evaluationSubmissionDto.getEmail());
                es.setIntakeProgram(intakeProgram.get());
                es.setJudgeBootcampMarks(full.toString());
                es.setPhase(evaluationSubmissionDto.getPhase());
                es.setSubmittedUser(user);
                try {
                    JSONObject d = new JSONObject(evaluationSubmissionDto.getProfileCardForm());
                    es.setProfileCard(d.toString());
                } catch (Exception e) {
                    JSONObject d = new JSONObject(
                        "{\"metadata\":{\"name\":\"\",\"description\":\"\"},\"form\":[{\"fieldGroup\":[]}]}");
                    es.setProfileCard(d.toString());
                }
                boolean completed = true;
                for (User u : bootcampJudges) {
                    if (!full.has(Long.toString(u.getId()))) {
                        completed = false;
                        break;
                    }
                }
                EvaluationSummary s = evaluationSummaryRepository.save(es);
                return assessmentResponseBuilder(completed, s, evaluationSubmissionDto);
            } else {
                EvaluationSummary es = new EvaluationSummary();
                int c = 0;
                float total = 0;
                float average = 0;
                JSONObject marks1 = new JSONObject(evaluationSubmissionDto.getJsonFormValues());
                Iterator<String> keys1 = marks1.keys();
                while (keys1.hasNext()) {
                    String key = keys1.next();
                    Object val = marks1.get(key);
                    if (val instanceof Number) {
                        float t = ((Number) val).floatValue();
                        total = total + t;
                        c++;
                    }
                }
                try {
                    average = total / c;
                } catch (Exception ignored) {
                }
                es.setBootcampTotal(total);
                es.setBootcampAvarage(average);
                es.setEmail(evaluationSubmissionDto.getEmail());
                es.setIntakeProgram(intakeProgram.get());
                es.setJudgeBootcampMarks(evaluationSubmissionDto.getJsonFormValues());
                es.setPhase(evaluationSubmissionDto.getPhase());
                es.setSubmittedUser(user);
                try {
                    JSONObject d = new JSONObject(evaluationSubmissionDto.getProfileCardForm());
                    es.setProfileCard(d.toString());
                } catch (Exception e) {
                    JSONObject d = new JSONObject(
                        "{\"metadata\":{\"name\":\"\",\"description\":\"\"},\"form\":[{\"fieldGroup\":[]}]}");
                    es.setProfileCard(d.toString());
                }
                boolean completed = true;
                for (User u : bootcampJudges) {
                    if (!marks1.has(Long.toString(u.getId()))) {
                        completed = false;
                        break;
                    }
                }
                EvaluationSummary s = evaluationSummaryRepository.save(es);
                return assessmentResponseBuilder(completed, s, evaluationSubmissionDto);
            }
        }
        return "invalid submissionId";
    }

    @Transactional
    @Override
    public Object assessmentEvaluationExit(CurrentUserObject currentUserObject,
                                           EvaluationSubmissionDto evaluationSubmissionDto) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        IntakeProgram intakeProgram = intakeProgramRepository
            .findById(evaluationSubmissionDto.getIntakeProgramId()).orElseThrow(() -> ItemNotFoundException.builder("Intake").build());
        IntakeProgramSubmission intakeProgramSubmission = intakeProgramSubmissionRepository
            .findById(evaluationSubmissionDto.getSubmissionId()).orElseThrow(() -> ItemNotFoundException.builder("Submission").build());
        if (user.getRole().getRoleName().contains(RoleName.ROLE_STC_JUDGES)
            || user.getRole().getRoleName().contains(RoleName.ROLE_NON_STC_JUDGES)) {
            Set<User> assessmentJudges = intakeProgramSubmission.getUsers();
            List<EvaluationSummary> ls = evaluationSummaryRepository.findByIntakeProgram_IdAndEmailAndPhase(
                intakeProgram.getId(), evaluationSubmissionDto.getEmail(),
                evaluationSubmissionDto.getPhase());
            if (!ls.isEmpty()) {
                EvaluationSummary es = ls.get(0);
                int c = 0;
                float total = 0;
                float average = 0;
                JSONObject full = new JSONObject();
                JSONObject marks = new JSONObject(es.getJudgesMarks());
                Iterator<String> keys = marks.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object val = marks.get(key);
                    if (val instanceof Number) {
                        float t = ((Number) val).floatValue();
                        total = total + t;
                        c++;
                        full.put(key, val);
                    }
                }
                JSONObject marks1 = new JSONObject(evaluationSubmissionDto.getJsonFormValues());
                Iterator<String> keys1 = marks1.keys();
                while (keys1.hasNext()) {
                    String key = keys1.next();
                    Object val = marks1.get(key);
                    if (!full.has(key) && val instanceof Number) {
                        float t = ((Number) val).floatValue();
                        total = total + t;
                        c++;
                        full.put(key, val);
                    }
                }

                try {
                    average = total / c;
                } catch (Exception ignored) {
                }

                es.setTotal(total);
                es.setAvarage(average);
                es.setEmail(evaluationSubmissionDto.getEmail());
                es.setIntakeProgram(intakeProgram);
                es.setJudgesMarks(full.toString());
                es.setPhase(evaluationSubmissionDto.getPhase());
                es.setSubmittedUser(user);
                es.setJudgeBootcampMarks(new JSONArray().toString());
                try {
                    JSONObject d = new JSONObject(evaluationSubmissionDto.getProfileCardForm());
                    es.setProfileCard(d.toString());
                } catch (Exception e) {
                    JSONObject d = new JSONObject(
                        "{\"metadata\":{\"name\":\"\",\"description\":\"\"},\"form\":[{\"fieldGroup\":[]}]}");
                    es.setProfileCard(d.toString());
                }
                boolean completed = true;
                for (User u : assessmentJudges) {
                    if (!full.has(Long.toString(u.getId()))) {
                        completed = false;
                        break;
                    }
                }
                EvaluationSummary s = evaluationSummaryRepository.save(es);
                Map<String, Object> data = assessmentResponseBuilder(completed, s, evaluationSubmissionDto);
                return data;
            } else {
                boolean completed = true;
                JSONObject marks1 = new JSONObject(evaluationSubmissionDto.getJsonFormValues());
                for (User u : assessmentJudges) {
                    if (!marks1.has(Long.toString(u.getId()))) {
                        completed = false;
                        break;
                    }
                }
                EvaluationSummary s = evaluationSummaryRepository.save(createAssessmentEvaluation(evaluationSubmissionDto, user, intakeProgram));
                return assessmentResponseBuilder(completed, s, evaluationSubmissionDto);
            }
        }
        return "invalid submissionId";
    }

    @NotNull
    private static Map<String, Object> assessmentResponseBuilder(boolean completed, EvaluationSummary s, EvaluationSubmissionDto evaluationSubmissionDto) {
        Map<String, Object> data = new HashMap<>();
        data.put("completed", completed);
        data.put("evaluationSubmissionId", s.getId());
        String startupName = evaluationSubmissionDto.getEmail()
            .substring(evaluationSubmissionDto.getEmail().indexOf("@") + 1);
        try {
            startupName = startupName.substring(0, startupName.indexOf("."));
        } catch (Exception ignored) {
        }
        data.put("startupName", startupName);
        return data;
    }

    private EvaluationSummary createAssessmentEvaluation(EvaluationSubmissionDto evaluationSubmissionDto, User user, IntakeProgram intakeProgram) {
        EvaluationSummary es = new EvaluationSummary();
        int c = 0;
        float total = 0;
        float average = 0;
        JSONObject marks1 = new JSONObject(evaluationSubmissionDto.getJsonFormValues());
        Iterator<String> keys1 = marks1.keys();
        while (keys1.hasNext()) {
            String key = keys1.next();
            Object val = marks1.get(key);
            if (val instanceof Number) {
                float t = ((Number) val).floatValue();
                total = total + t;
                c++;
            }
        }
        try {
            average = total / c;
        } catch (Exception ignored) {
        }
        es.setTotal(total);
        es.setAvarage(average);
        es.setEmail(evaluationSubmissionDto.getEmail());
        es.setIntakeProgram(intakeProgram);
        es.setJudgesMarks(evaluationSubmissionDto.getJsonFormValues());
        es.setPhase(evaluationSubmissionDto.getPhase());
        es.setSubmittedUser(user);
        es.setJudgeBootcampMarks(new JSONArray().toString());
        try {
            JSONObject d = new JSONObject(evaluationSubmissionDto.getProfileCardForm());
            es.setProfileCard(d.toString());
        } catch (Exception e) {
            JSONObject d = new JSONObject(
                "{\"metadata\":{\"name\":\"\",\"description\":\"\"},\"form\":[{\"fieldGroup\":[]}]}");
            es.setProfileCard(d.toString());
        }
        return es;
    }

    @Transactional
    @Override
    public Object createSummary(CurrentUserObject currentUserObject, Long submissionId, String phase) {
        EvaluationSummary es = evaluationSummaryRepository.findByIdAndPhaseContaining(submissionId, phase);
        if (Objects.nonNull(es)) {
            es.setStatus(Constant.SUMMARY.toString());
            evaluationSummaryRepository.save(es);
            if (phase.contains(Constant.BOOTCAMP.toString())) {
                intakeProgramSubmissionRepository.updateBootcampEnd(es.getIntakeProgram().getId(), es.getEmail(),
                    Constant.BOOTCAMP_END.toString());
            } else {
                intakeProgramSubmissionRepository.updateBootcampEnd(es.getIntakeProgram().getId(), es.getEmail(),
                    Constant.ASSESSMENT_END.toString());
            }
        }
        return null;
    }

    @Transactional
    @Override
    public Object doLater(CurrentUserObject currentUserObject, Long submissionId, String phase,
                          EvaluationSubmissionDto evaluationSubmissionDto) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        IntakeProgramSubmission intakeProgramSubmission = intakeProgramSubmissionRepository
            .findById(evaluationSubmissionDto.getSubmissionId()).orElseThrow(() -> ItemNotFoundException.builder("Submission").build());
        if (user.getRole().getRoleName().contains(RoleName.ROLE_STC_JUDGES)
            || user.getRole().getRoleName().contains(RoleName.ROLE_NON_STC_JUDGES)) {
            Map<String, Object> data = new HashMap<>();
            if (phase.contains(Constant.BOOTCAMP.toString())) {
                intakeProgramSubmission.setPhase(Constant.BOOTCAMP_EVALUATE.toString());
                data.put("phaseTransition", Constant.BOOTCAMP_EVALUATE.toString());
                intakeProgramSubmissionRepository.save(intakeProgramSubmission);
            } else {
                intakeProgramSubmission.setPhase(Constant.ASSESSMENT_EVALUATE.toString());
                data.put("phaseTransition", Constant.ASSESSMENT_EVALUATE.toString());
                intakeProgramSubmissionRepository.save(intakeProgramSubmission);
            }
            data.put("startupName", intakeProgramSubmission.getStartupName());
            return data;
        }
        return "invalid submissionId";
    }

    @Transactional
    @Override
    public Object exitEval(CurrentUserObject currentUserObject, Long submissionId, String phase,
                           EvaluationSubmissionDto evaluationSubmissionDto) {
        return null;
    }

}
