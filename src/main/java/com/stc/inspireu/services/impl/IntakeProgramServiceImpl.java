package com.stc.inspireu.services.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.beans.MailMetadata;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.dtos.validation.*;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.enums.EmailKey;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.mappers.*;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.IntakeProgramService;
import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.services.PushNotificationService;
import com.stc.inspireu.services.ResourcePermissionService;
import com.stc.inspireu.utils.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntakeProgramServiceImpl implements IntakeProgramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EvaluationSummaryRepository evaluationSummaryRepository;
    private final ScreeningEvaluationFormRepository screeningEvaluationFormRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final IntakeProgramRepository intakeProgramRepository;
    private final ResourcePermissionService resourcePermissionService;
    private final UserResourcePermissionRepository userResourcePermissionRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final Utility utility;
    private final JwtUtil jwtUtil;
    private final EmailUtil emailUtil;
    private final MarkCard2022Repository markCard2022Repository;
    private final RegistrationFormRepository registrationFormRepository;
    private final AssessmentEvaluationFormRepository evaluationFormRepository;
    private final BootcampFormRepository bootcampFormRepository;
    private final JudgeCalenderRepository judgeCalenderRepository;
    private final DueDiligenceTemplate2021Repository dueDiligenceTemplate2021Repository;
    private final OpenEventRepository openEventRepository;
    private final ProgessReportRepository progessReportRepository;
    private final ProfileCardRepository profileCardRepository;
    private final IntakeProgramSubmissionRepository intakeProgramSubmissionRepository;
    private final PushNotificationService pushNotificationService;
    private final EvaluationSummaryMapper evaluationSummaryMapper;
    private final AssessmentEvaluationFormMapper assessmentEvaluationFormMapper;
    private final IntakeProgramMapper intakeProgramMapper;
    private final IntakeProgramSubmissionMapper intakeProgramSubmissionMapper;

    @Value("${ui.url}")
    private String uiUrl;

    @Value("${ui.dueDiligencePublic}")
    private String dueDiligencePublic;

    @Value("${ui.publicCal}")
    private String publicCal;
    private final StartupRepository startupRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final ScreeningEvaluationFormMapper screeningEvaluationFormMapper;
    private final RegistrationFormMapper registrationFormMapper;
    private final DueDiligenceTemplate2021Mapper dueDiligenceTemplate2021Mapper;

    @Transactional
    @Override
    public ResponseEntity<?> createStartupOnboarding(CurrentUserObject currentUserObject, Long intakeProgramId, PostStartupOnboardingDto postOnboardingDto) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        List<DueDiligenceTemplate2021> dd = dueDiligenceTemplate2021Repository.findByIntakeProgram_IdAndStartupIdIsNull(intakeProgramId);
        DueDiligenceTemplate2021 dueDiligence;
        if (!dd.isEmpty()) {
            dueDiligence = dd.get(0);
            if (!dueDiligence.getStatus().equals(Constant.PUBLISHED.toString())) {
                return ResponseWrapper.response400("publish due diligence", "dueDiligenceId");
            }
        } else {
            dueDiligence = null;
        }
        if (postOnboardingDto.getStartupEmail().size() > 0 && dueDiligence != null) {
            JSONObject jo = new JSONObject(postOnboardingDto.getEmailsAndStartups());
            Set<String> emails = postOnboardingDto.getStartupEmail();

            // steps
            // 1.Create startup entry
            // 2.create startup user entry
            // 3.send invitation mail having dueDiliengence link
            Role role = roleRepository.findByRoleName(RoleName.ROLE_STARTUPS_ADMIN);
            emails.forEach(email -> createStartupDueDiligenceInvite(intakeProgramId, email, role, dueDiligence.getId(), jo));
            return ResponseWrapper.response(dueDiligenceTemplate2021Mapper.toDueDiligenceTemplate2021DTO(dueDiligence));
        }
        return ResponseWrapper.response(null);
    }

    @Transactional
    void createStartupDueDiligenceInvite(Long intakeProgramId, String email, Role role, Long dueDiligenceId, JSONObject emailsAndStartups) {
        IntakeProgramSubmission intakeProgramSubmission = intakeProgramSubmissionRepository.findByEmailAndIntakeProgram_Id(email, intakeProgramId)
            .orElseThrow(() -> new CustomRunTimeException("Submission not found"));
        String inviteToken = null;
        if (Objects.nonNull(role)) {
            Map<String, Object> claims = new HashMap<>();
            // before creating startup check whether user created already.
            User user = userRepository.findByEmail(email);
            if (Objects.isNull(user)) {
                Startup startup = new Startup();
                String startupName = intakeProgramSubmission.getStartupName();
                if (emailsAndStartups.has(email)) {
                    Object val = emailsAndStartups.get(email);
                    if (val instanceof String) {
                        startupName = (String) val;
                    }
                }
                startup.setStartupName(startupName);
                startup.setRegistrationJsonForm(intakeProgramSubmission.getJsonRegistrationForm());
                startup.setIntakeProgram(intakeProgramSubmission.getIntakeProgram());
                startup.setProfileCardJsonForm(intakeProgramSubmission.getJsonProfileCard());
                startup.setProfileInfoJson(intakeProgramSubmission.getProfileInfoJson());
                startupRepository.save(startup);
                String userName = email.substring(0, email.indexOf("@"));
                user = new User();
                user.setEmail(email);
                user.setAlias(userName);
                user.setStartup(startup);
                user.setRole(role);
                user.setInvitationStatus(Constant.STARTUP_DUEDILIGENCE_INVITATION.toString());
                user.setInviteToken(inviteToken);
                userRepository.save(user);
                claims.put("userId", user.getId());
                claims.put("intakeProgramId", intakeProgramId);
                claims.put("intakeProgramName", intakeProgramSubmission.getIntakeProgram().getProgramName());
                claims.put("dueDiligenceId", dueDiligenceId);
                claims.put("startupId", user.getStartup().getId());
                claims.put("email", user.getEmail());
                claims.put("roleName", user.getRole().getRoleName());
                claims.put("roleId", user.getRole().getId());
                claims.put("message", Constant.STARTUP_DUEDILIGENCE_INVITATION.toString());
                long milliSeconds = (intakeProgramSubmission.getIntakeProgram().getPeriodEnd().getTime() - new Date().getTime());
                inviteToken = jwtUtil.genericJwtToken(claims, milliSeconds);
                if (inviteToken != null && !inviteToken.equals("invalid_token") && user.getInvitationStatus().equals(Constant.STARTUP_DUEDILIGENCE_INVITATION.toString())) {
                    user.setInvitationStatus(Constant.STARTUP_DUEDILIGENCE_INVITATION.toString());
                    user.setInviteToken(inviteToken);
                    userRepository.save(user);
                }
            }
        }
        if (inviteToken != null && !inviteToken.equals("invalid_token")) {
            MailMetadata mailMetadata = new MailMetadata();
            Map<String, Object> props = new HashMap<>();
            String link = uiUrl + dueDiligencePublic + "/" + inviteToken;
            props.put("inviteLink", link);
            props.put("toMail", email);
            mailMetadata.setFrom("");
            mailMetadata.setTo(email);
            mailMetadata.setProps(props);
            mailMetadata.setSubject("due-diligence-public mail");
            mailMetadata.setTemplateFile("due-diligence-public");
            notificationService.sendStartupDueDiligenceInviteMail(mailMetadata);
        }

    }

    @Transactional
    @Override
    public IntakeProgram saveIntakeProgram(CurrentUserObject currentUserObject, PostIntakeProgramDto postIntakeProgramDto) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        IntakeProgram ip = new IntakeProgram();
        ip.setPeriodEnd(new Date(postIntakeProgramDto.getPeriodEnd()));
        ip.setPeriodStart(new Date(postIntakeProgramDto.getPeriodStart()));
        ip.setProgramName(postIntakeProgramDto.getProgramName());
        ip.setStatus(postIntakeProgramDto.getIsDraft() ? Constant.DRAFT.toString() : Constant.PUBLISHED.toString());
        ip.setCreatedUser(user);
        ip.setPhaseStatus(1);
        ip = intakeProgramRepository.save(ip);
        Map<String, Object> claims = new HashedMap<>();
        claims.put("intakeProgramPhase", Constant.REGISTRATION.toString());
        claims.put("intakeProgramId", ip.getId());
        claims.put("intakeProgramName", ip.getProgramName());
        long milliSeconds = (ip.getPeriodEnd().getTime() - new Date().getTime());
        String formToken = jwtUtil.genericJwtToken(claims, milliSeconds);
        ip.setFormToken(formToken);
        intakeProgramRepository.save(ip);
        progessReportRepository.save(setProgressReportValues(user, ip));
        MarkCard2022 markCard = new MarkCard2022();
        markCard.setName("Intake_" + ip.getId() + "_Mark_Card");
        markCard.setIntakeProgram(ip);
        markCard2022Repository.save(markCard);
        DueDiligenceTemplate2021 e = new DueDiligenceTemplate2021();
        e.setCreatedUser(user);
        e.setIntakeProgram(ip);
        e.setJsonForm(ConstantUtility.DUE_DILIGENCE_BASIC_TEMPLATE);
        e.setName("Intake_" + ip.getId() + "_Due_Diligence");
        e.setStatus(Constant.DRAFT.toString());
        dueDiligenceTemplate2021Repository.save(e);
        return ip;
    }

    private ProgressReport setProgressReportValues(User user, IntakeProgram intakeProgram) {
        ProgressReport progressReport = new ProgressReport();
        progressReport.setJsonReportDetail(ConstantUtility.PROGRESS_REPORT_BASIC_TEMPLATE);
        progressReport.setReportName("Intake_" + intakeProgram.getId() + "_Progress_Report");
        progressReport.setStatus(Constant.DRAFT.toString());
        progressReport.setCreatedUser(user);
        progressReport.setIntakeProgram(intakeProgram);
        progressReport.setFundraiseInvestment(0);
        progressReport.setMarketValue(0);
        progressReport.setProfitLoss(0);
        progressReport.setProfitLossExpected(0);
        progressReport.setRevenue(0);
        progressReport.setRevenueExpected(0);
        progressReport.setSales(0);
        progressReport.setSalesExpected(0);
        progressReport.setUsers(0);
        progressReport.setUsersExpected(0);
        progressReport.setFteEmployees(0);
        progressReport.setFteEmployeesExpected(0);
        progressReport.setPteEmployees(0);
        progressReport.setPteEmployeesExpected(0);
        progressReport.setFreelancers(0);
        progressReport.setFreelancersExpected(0);
        progressReport.setLoans(0);
        progressReport.setHighGrossMerchandise(0);
        return progressReport;
    }

    @Transactional
    @Override
    public IntakeProgram updateIntakeProgram(Long intakeProgramId, PostIntakeProgramDto postIntakeProgramDto) {
        return intakeProgramRepository.findById(intakeProgramId)
            .map(intakeProgram -> {
                intakeProgram.setPeriodEnd(new Date(postIntakeProgramDto.getPeriodEnd()));
                intakeProgram.setPeriodStart(new Date(postIntakeProgramDto.getPeriodStart()));
                intakeProgram.setProgramName(postIntakeProgramDto.getProgramName());
                intakeProgram.setStatus(postIntakeProgramDto.getIsDraft() ? Constant.DRAFT.toString() : Constant.PUBLISHED.toString());
                return intakeProgramRepository.save(intakeProgram);
            }).orElse(null);
    }

    @Transactional
    @Override
    public Object shareIntake(CurrentUserObject currentUserObject, Long intakeProgramId, ShareIntakeDto shareIntakeDto) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return intakeProgramRepository.findById(intakeProgramId)
            .map(intakeProgram -> {
                if (intakeProgram.getStatus().equals(Constant.DRAFT.toString())) {
                    resourcePermissionService.createResource(user.getId(), intakeProgramId, ResourceUtil.mip);
                    return intakeProgram;
                } else {
                    return "share only in draf mode";
                }
            }).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
    }

    @Transactional
    @Override
    public Object publishIntake(CurrentUserObject currentUserObject, Long intakeProgramId) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return intakeProgramRepository.findById(intakeProgramId)
            .map(intakeProgram -> {
                if (intakeProgram.getStatus().equals(Constant.PUBLISHED.toString()))
                    throw new CustomRunTimeException("Intake is already published", HttpStatus.BAD_REQUEST);
                if (Objects.isNull(intakeProgram.getRegistrationForm()))
                    throw new CustomRunTimeException("Registration form is required to activate an intake", HttpStatus.BAD_REQUEST);
                intakeProgram.setStatus(Constant.PUBLISHED.toString());
                intakeProgramRepository.save(intakeProgram);
                registrationFormRepository.findById(intakeProgram.getRegistrationForm().getId()).ifPresent(registrationForm -> {
                    registrationForm.setStatus(Constant.PUBLISHED.toString());
                    registrationFormRepository.save(registrationForm);
                });
                AssessmentEvaluationForm assessmentEvaluationForm = intakeProgram.getAssessmentEvaluationForm();
                if (Objects.nonNull(intakeProgram.getAssessmentEvaluationForm())) {
                    assessmentEvaluationForm.setStatus(Constant.PUBLISHED.toString());
                    assessmentEvaluationForm.setPublishedAt(new Date());
                    assessmentEvaluationForm.setPublishedUser(user);
                    evaluationFormRepository.save(assessmentEvaluationForm);
                }
                BootcampEvaluationForm bootcampEvaluationForm = intakeProgram.getBootcampEvaluationForm();
                if (Objects.nonNull(bootcampEvaluationForm)) {
                    bootcampEvaluationForm.setStatus(Constant.PUBLISHED.toString());
                    bootcampEvaluationForm.setPublishedAt(new Date());
                    bootcampEvaluationForm.setPublishedUser(user);
                    bootcampFormRepository.save(bootcampEvaluationForm);
                }
                ScreeningEvaluationForm screeningEvaluationForm = intakeProgram.getScreeningEvaluationForm();
                if (Objects.nonNull(screeningEvaluationForm)) {
                    screeningEvaluationForm.setStatus(Constant.PUBLISHED.toString());
                    screeningEvaluationForm.setPublishedAt(new Date());
                    screeningEvaluationForm.setPublishedUser(user);
                    screeningEvaluationFormRepository.save(screeningEvaluationForm);
                }
                return intakeProgram;
            }).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
    }

    @Transactional
    @Override
    public Object linkForm(String string, CurrentUserObject currentUserObject, Long intakeProgramId, Long formId) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return intakeProgramRepository.findById(intakeProgramId)
            .map(intakeProgram -> {
                switch (string) {
                    case "bootCampForm":
                        BootcampEvaluationForm evaluationForm = intakeProgram.getBootcampEvaluationForm();
                        if (Objects.isNull(evaluationForm) || (!evaluationForm.getId().equals(formId))) {
                            return bootcampFormRepository.findById(formId)
                                .map(form -> {
                                    intakeProgram.setBootcampEvaluationForm(form);
                                    intakeProgramRepository.save(intakeProgram);
                                    return intakeProgramMapper.toGetIntakeProgramDto(intakeProgram);
                                }).orElseThrow(() -> new CustomRunTimeException("Invalid formId"));
                        }
                        break;
                    case "registrationForm":
                        RegistrationForm registrationForm = intakeProgram.getRegistrationForm();
                        if (Objects.isNull(registrationForm) || (!registrationForm.getId().equals(formId))) {
                            return registrationFormRepository.findById(formId)
                                .map(form -> {
                                    intakeProgram.setRegistrationForm(form);
                                    intakeProgramRepository.save(intakeProgram);
                                    return intakeProgramMapper.toGetIntakeProgramDto(intakeProgram);
                                }).orElseThrow(() -> new CustomRunTimeException("Invalid formId"));
                        }
                        break;
                    case "evaluationForm":
                        AssessmentEvaluationForm assessmentEvaluationForm = intakeProgram.getAssessmentEvaluationForm();
                        if (Objects.isNull(assessmentEvaluationForm) || (!assessmentEvaluationForm.getId().equals(formId))) {
                            return evaluationFormRepository.findById(formId)
                                .map(form -> {
                                    intakeProgram.setAssessmentEvaluationForm(form);
                                    intakeProgramRepository.save(intakeProgram);
                                    return intakeProgramMapper.toGetIntakeProgramDto(intakeProgram);
                                }).orElseThrow(() -> new CustomRunTimeException("Invalid formId"));
                        }
                        break;
                }
                return null;
            }).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> registrationNextPhase(CurrentUserObject currentUserObject, Long intakeProgramId, Long registrationId) {
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeProgramId).orElseThrow(() -> new CustomRunTimeException("Intake not found", HttpStatus.NOT_FOUND));
        if (Objects.isNull(intakeProgram.getAssessmentEvaluationForm()))
            throw new CustomRunTimeException("Please create an Assessment Evaluation Form", HttpStatus.BAD_REQUEST);
        if (!intakeProgram.getAssessmentEvaluationForm().getStatus().equals(Constant.PUBLISHED.toString()))
            throw new CustomRunTimeException("Assessment form is not published", HttpStatus.BAD_REQUEST);
        if (!openEventRepository.existsByIntakeProgram_IdAndEventPhase(intakeProgramId, Constant.ASSESSMENT.toString())) {
            throw new CustomRunTimeException("Please create an event for assessment");
        }
        return intakeProgramSubmissionRepository.findByIdAndIntakeProgram_Id(registrationId, intakeProgramId)
            .map(intakeProgramSubmission -> {
                intakeProgramSubmission.setPhase(Constant.ASSESSMENT.toString());
                intakeProgramSubmission.getEvaluationSummary().setPhase("SCREENING_SUMMARY");
                IntakeProgramSubmission e = intakeProgramSubmissionRepository.save(intakeProgramSubmission);
                Map<String, Object> data = new HashMap<>();
                data.put("Assessment", registrationId);
                data.put("phase", e.getPhase());
                MailMetadata mailMetadata = new MailMetadata();
                Map<String, Object> props = new HashMap<>();
                props.put("toMail", e.getEmail());
                mailMetadata.setFrom("");
                mailMetadata.setTo(e.getEmail());
                mailMetadata.setProps(props);
                mailMetadata.setSubject("Selected to assessment");
                mailMetadata.setTemplateFile("Selected to assessment");
                notificationService.sendMailMangeFormsNotifications(mailMetadata, EmailKey.selected_for_assessment.toString());
                return ResponseWrapper.response(data);
            }).orElseThrow(() -> new CustomRunTimeException("Submission not found"));
    }

    @Transactional
    @Override
    public Object assignJudges(CurrentUserObject currentUserObject, Long intakeProgramId, Long assessmentId, AssignJudgeDto assignJudgeDto) {
        List<OpenEvent> oes = openEventRepository.findByIntakeProgram_IdAndEventPhase(intakeProgramId, Constant.ASSESSMENT.toString());
        if (oes.isEmpty()) {
            throw new CustomRunTimeException("Please create an assessment event");
        }
        Set<User> users = userRepository.findByIdIn(assignJudgeDto.getAssigneeIds());
        assignJudgeDto.getAssessmentIds().forEach(judgeId -> assignJudge(judgeId, users, oes.get(0)));
        Map<String, Object> data = new HashMap<>();
        data.put("assigneeIds", assignJudgeDto.getAssigneeIds());
        data.put("assessmentIds", assignJudgeDto.getAssessmentIds());
        return data;
    }

    @Transactional
    @Override
    public void assignEvaluatorsForScreening(Long intakeId, AssignEvaluatorsDTO assignEvaluatorsDTO) {
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        ScreeningEvaluationForm screeningEvaluationForm = intakeProgram.getScreeningEvaluationForm();
        if (Objects.isNull(screeningEvaluationForm))
            throw new CustomRunTimeException("Screening evaluation form is not found");
        if (!screeningEvaluationForm.getStatus().equals(Constant.PUBLISHED.toString()))
            throw new CustomRunTimeException("Screening evaluation form is not published");
        Set<User> evaluators = new HashSet<>((Collection) userRepository.findAllById(assignEvaluatorsDTO.getEvaluatorIds()));
        List<String> allowedRoles = Arrays.asList(RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER);
        evaluators.forEach(evaluator -> {
            if (!allowedRoles.contains(evaluator.getRole().getRoleName()))
                throw new CustomRunTimeException(evaluator.getAlias() + " is not allowed to evaluate in the screening");
        });
        List<IntakeProgramSubmission> applications = (List<IntakeProgramSubmission>) intakeProgramSubmissionRepository.findAllById(assignEvaluatorsDTO.getApplicationIds());
        applications.forEach(application -> {
            if (!application.getPhase().equals(Constant.REGISTRATION.toString()))
                throw new CustomRunTimeException("Startup: " + application.getStartupName() + " is not in the screening phase");
            if (!application.getScreeningEvaluators().isEmpty())
                throw new CustomRunTimeException("Evaluators already set for the startup: " + application.getStartupName());
            application.setScreeningEvaluators(evaluators);
        });
        intakeProgramSubmissionRepository.saveAll(applications);
        applications.forEach(application -> {
            MailMetadata mailMetadata = new MailMetadata();
            Map<String, Object> props = new HashMap<>();
            props.put("toMail", application.getEmail());
            mailMetadata.setFrom("");
            mailMetadata.setTo(application.getEmail());
            mailMetadata.setProps(props);
            mailMetadata.setSubject("");
            mailMetadata.setTemplateFile("");
            notificationService.sendMailMangeFormsNotifications(mailMetadata, EmailKey.ASSIGNED_TO_SCREENING_EVALUATORS.toString());
        });
    }

    @Transactional
    void assignJudge(Long assId, Set<User> users, OpenEvent openEvent) {
        intakeProgramSubmissionRepository.findById(assId).ifPresent(intakeProgramSubmission -> {
            intakeProgramSubmission.setPhase(Constant.ASSESSMENT_EVALUATE.toString());
            intakeProgramSubmission.setUsers(users);
            intakeProgramSubmissionRepository.save(intakeProgramSubmission);
            users.forEach(user ->
                addToJudgeCalender(Constant.ASSESSMENT.toString(), intakeProgramSubmission.getEmail(), user, intakeProgramSubmission.getIntakeProgram(), openEvent));
            MailMetadata mailMetadata = new MailMetadata();
            Map<String, Object> props = new HashMap<>();
            props.put("toMail", intakeProgramSubmission.getEmail());
            mailMetadata.setFrom("");
            mailMetadata.setTo(intakeProgramSubmission.getEmail());
            mailMetadata.setProps(props);
            mailMetadata.setSubject("Assigned for assessment");
            mailMetadata.setTemplateFile("Assigned for assessment");
            notificationService.sendMailMangeFormsNotifications(mailMetadata, EmailKey.assigned_to_assessment_judges.toString());
            IntakeProgram intakeProgram = intakeProgramSubmission.getIntakeProgram();
            Map<String, Object> data1 = new HashMap<>();
            data1.put("intakeProgramId", intakeProgram.getId());
            data1.put("intakeProgramName", intakeProgram.getProgramName());
            data1.put("openEventId", openEvent.getId());
            data1.put("email", intakeProgramSubmission.getEmail());
            long milliSeconds = (intakeProgram.getPeriodEnd().getTime() - new Date().getTime());
            String claims = jwtUtil.genericJwtToken(data1, milliSeconds);
            if (!claims.equals("invalid_token")) {
                LOGGER.debug("Invite mail sending");
                MailMetadata mailMetadata1 = new MailMetadata();
                Map<String, Object> props1 = new HashMap<>();
                String link = uiUrl + publicCal + "?formToken=" + claims;
                props1.put("inviteLink", link);
                props1.put("toMail", intakeProgramSubmission.getEmail());
                mailMetadata1.setFrom("");
                mailMetadata1.setTo(intakeProgramSubmission.getEmail());
                mailMetadata1.setProps(props1);
                mailMetadata1.setSubject("Book slot for Assessment");
                mailMetadata1.setTemplateFile("Book slot for Assessment");
                notificationService.sendCalendlyNotification(openEvent, mailMetadata1, "Choose your convenient timing for Assessment");
            }
        });
    }

    @Transactional
    void addToJudgeCalender(String phase, String mail, User judge, IntakeProgram intakeProgram, OpenEvent openEvent) {
        judgeCalenderRepository.findByJudgeIdAndPhaseAndIntakeProgramIdAndEmail(judge.getId(), phase, intakeProgram.getId(), mail)
            .ifPresent(judgeCalendar -> {
                judgeCalendar.setJudge(judge);
                judgeCalendar.setEmail(mail);
                judgeCalendar.setIntakeProgram(intakeProgram);
                judgeCalendar.setPhase(phase);
                judgeCalenderRepository.save(judgeCalendar);
            });
    }

    @Transactional
    @Override
    public Page<GetIntakeProgramSubmissionDto> registartionSubmissions(CurrentUserObject currentUserObject, Long intakeProgramId, String string, String language, Pageable paging, String filterKeyword, String filterBy) {
        Page<IntakeProgramSubmission> ls;
        if (!filterKeyword.isEmpty()) {
            if (filterBy.equalsIgnoreCase("email")) {
                ls = intakeProgramSubmissionRepository.findByEmailContainingIgnoreCaseAndIntakeProgram_IdAndPhaseAndScreeningEvaluatorsEmpty(filterKeyword, intakeProgramId, Constant.REGISTRATION.toString(), paging);
            } else if (filterBy.contains("Startup")) {
                ls = intakeProgramSubmissionRepository.findByJsonRegistrationFormContainingIgnoreCaseAndIntakeProgram_IdAndPhaseAndScreeningEvaluatorsEmpty(filterKeyword, intakeProgramId, Constant.REGISTRATION.toString(), paging);
            } else {
                ls = intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhaseAndScreeningEvaluatorsEmpty(intakeProgramId, Constant.REGISTRATION.toString(), paging);
            }
        } else {
            ls = intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhaseAndScreeningEvaluatorsEmpty(intakeProgramId, Constant.REGISTRATION.toString(), paging);
        }
        return ls.map(l -> intakeProgramSubmissionMapper.toGetIntakeProgramSubmissionDtoWithCurrentUserObject(l, currentUserObject));
    }

    @Override
    public List<RegistrationFormValidationDTO> intakeProgramApplications(Long intakeProgramId, List<Long> applicationIds) {
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeProgramId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        List<IntakeProgramSubmission> submissions;
        if (Objects.isNull(applicationIds) || applicationIds.isEmpty())
            submissions = intakeProgramSubmissionRepository.findByIntakeProgramAndPhaseOrderByIdDesc(intakeProgram, Constant.REGISTRATION.toString());
        else
            submissions = intakeProgramSubmissionRepository.findAllByIdInAndIntakeProgramOrderByIdDesc(applicationIds, intakeProgram);
        return submissions.stream().map(submission -> {
            RegistrationFormValidationDTO dto = new Gson().fromJson(submission.getJsonRegistrationForm(), RegistrationFormValidationDTO.class);
            dto.setCreatedOn(submission.getCreatedOn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public GetIntakeProgramSubmissionDto findRegistrationSubmission(Long intakeId, Long submissionId) {
        return intakeProgramSubmissionMapper.toGetIntakeProgramSubmissionDto(intakeProgramSubmissionRepository.findByIntakeProgram_IdAndId(intakeId, submissionId)
            .orElseThrow(() -> new CustomRunTimeException("Submission not found", HttpStatus.NOT_FOUND)));
    }

    @Transactional
    @Override
    public Page<GetIntakeProgramSubmissionDto> assessmentSubmissions(Long intakeProgramId, String string, Pageable paging, String filterKeyword, String filterBy) {
        Page<IntakeProgramSubmission> ls;
        if (!filterKeyword.isEmpty()) {
            if (filterBy.equalsIgnoreCase("email")) {
                ls = intakeProgramSubmissionRepository.findByEmailContainingIgnoreCaseAndIntakeProgram_IdAndPhase(filterKeyword, intakeProgramId, string, paging);
            } else if (filterBy.contains("Startup")) {
                ls = intakeProgramSubmissionRepository.findByJsonRegistrationFormContainingIgnoreCaseAndIntakeProgram_IdAndPhase(filterKeyword, intakeProgramId, string, paging);
            } else {
                ls = intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhase(intakeProgramId, string, paging);
            }
        } else {
            ls = intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhase(intakeProgramId, string, paging);
        }
        return ls.map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionDto);
    }

    @Transactional
    @Override
    public ResponseEntity<Object> startAssessmentEvaluation(CurrentUserObject currentUserObject, Long intakeProgramId, Long assessmentId) {
        return intakeProgramSubmissionRepository.findById(assessmentId)
            .map(intakeProgramSubmission -> {
                if (intakeProgramSubmissionRepository.existsByIntakeProgram_IdAndPhase(intakeProgramId, Constant.ASSESSMENT_EVALUATION_START.toString())) {
                    return ResponseWrapper.response400("There is aother assessment evaluation already inprogress", "intakeProgramId|assessmentId");
                }
                if (intakeProgramSubmission.getUsers().size() > 0) {
                    intakeProgramSubmission.setEvaluationStartedOn(new Date());
                    intakeProgramSubmission.setPhase(Constant.ASSESSMENT_EVALUATION_START.toString());
                    intakeProgramSubmissionRepository.save(intakeProgramSubmission);
                    notificationService.evaluationStartNotification(Constant.ASSESSMENT.toString(), intakeProgramSubmission);
                    return ResponseWrapper.response(sendWebsocketNotification(intakeProgramSubmission));
                }
                return ResponseWrapper.response400("Assign judge first", "intakeProgramId|assessmentId");
            }).orElseThrow(() -> new CustomRunTimeException("Submission not found"));
    }

    @Transactional
    @Override
    public Object stopAssessmentEvaluation(CurrentUserObject currentUserObject, Long intakeProgramId, Long assessmentId) {
        return intakeProgramSubmissionRepository.findById(assessmentId)
            .map(intakeProgramSubmission -> {
                if (intakeProgramSubmission.getPhase().equals(Constant.ASSESSMENT_EVALUATION_START.toString())) {
                    intakeProgramSubmission.setPhase(Constant.ASSESSMENT_EVALUATION_STOP.toString());
                    intakeProgramSubmission.setEvaluationEndedOn(new Date());
                    intakeProgramSubmission = intakeProgramSubmissionRepository.save(intakeProgramSubmission);
                    return sendWebsocketNotification(intakeProgramSubmission);
                }
                return ResponseWrapper.response400("Evaluation not started", "intakeProgramId|assessmentId");
            }).orElseThrow(() -> new CustomRunTimeException("Submission not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<Object> assigneesAssessements(CurrentUserObject currentUserObject, Pageable paging) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<IntakeProgramSubmission> list = intakeProgramSubmissionRepository.findByUsers_Id(user.getId(), paging);
        return ResponseWrapper.response(list.map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionDto));
    }

    @Transactional
    @Override
    public ResponseEntity<Object> assigneesAssessementEvaluations(CurrentUserObject currentUserObject, Long intakeProgramId, Pageable paging, String filterKeyword, String filterBy) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<IntakeProgramSubmission> list;
        if (!filterKeyword.isEmpty()) {
            if (filterBy.equalsIgnoreCase("email")) {
                list = intakeProgramSubmissionRepository.getEvaluationsByEmail(intakeProgramId, Constant.ASSESSMENT_EVALUATE.toString(), Constant.ASSESSMENT_EVALUATION_START.toString(), Constant.ASSESSMENT_EVALUATION_STOP.toString(), paging, filterKeyword);
            } else if (filterBy.contains("Startup")) {
                list = intakeProgramSubmissionRepository.getEvaluationsByStartUp(intakeProgramId, Constant.ASSESSMENT_EVALUATE.toString(), Constant.ASSESSMENT_EVALUATION_START.toString(), Constant.ASSESSMENT_EVALUATION_STOP.toString(), paging, filterKeyword);
            } else {
                list = intakeProgramSubmissionRepository.getEvaluations(intakeProgramId, Constant.ASSESSMENT_EVALUATE.toString(), Constant.ASSESSMENT_EVALUATION_START.toString(), Constant.ASSESSMENT_EVALUATION_STOP.toString(), paging);
            }
        } else {
            list = intakeProgramSubmissionRepository.getEvaluations(intakeProgramId, Constant.ASSESSMENT_EVALUATE.toString(), Constant.ASSESSMENT_EVALUATION_START.toString(), Constant.ASSESSMENT_EVALUATION_STOP.toString(), paging);
        }
        return ResponseWrapper.response(list.map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionDto));
    }

    @Override
    public ResponseEntity<Object> screeningEvaluations(CurrentUserObject currentUserObject, Long intakeProgramId, Pageable paging, String filterKeyword, String filterBy) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<IntakeProgramSubmission> list;
        List<String> phases = Arrays.asList(Constant.REGISTRATION.toString(), Constant.SCREENING_EVALUATION_START.toString(), Constant.SCREENING_EVALUATION_STOP.toString());
        if (Objects.nonNull(filterKeyword) && !filterKeyword.isEmpty()) {
            if (filterBy.equalsIgnoreCase("email")) {
                list = intakeProgramSubmissionRepository.findAllByPhaseInAndScreeningEvaluatorsNotEmptyAndIntakeProgram_idAndEmailContainsIgnoreCase(phases, intakeProgramId, filterKeyword, paging);
            } else if (filterBy.contains("Startup")) {
                list = intakeProgramSubmissionRepository.findAllByPhaseInAndScreeningEvaluatorsNotEmptyAndIntakeProgram_idAndStartupNameContainsIgnoreCase(phases, intakeProgramId, filterKeyword, paging);
            } else {
                list = intakeProgramSubmissionRepository.findAllByPhaseInAndScreeningEvaluatorsNotEmptyAndIntakeProgram_id(phases, intakeProgramId, paging);
            }
        } else {
            list = intakeProgramSubmissionRepository.findAllByPhaseInAndScreeningEvaluatorsNotEmptyAndIntakeProgram_id(phases, intakeProgramId, paging);
        }
        return ResponseWrapper.response(list.map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionDto));
    }

    @Transactional
    @Override
    public Object asessmentMoveToEvaluate(CurrentUserObject currentUserObject, Long intakeProgramId, Long assessmentId) {
        return intakeProgramSubmissionRepository.findByIdAndIntakeProgram_Id(assessmentId, intakeProgramId)
            .map(intakeProgramSubmission -> {
                intakeProgramSubmission.setPhase(Constant.ASSESSMENT_EVALUATE.toString());
                IntakeProgramSubmission e = intakeProgramSubmissionRepository.save(intakeProgramSubmission);
                Map<String, Object> data = new HashMap<>();
                data.put("assessmentId", assessmentId);
                data.put("phase", e.getPhase());
                return data;
            }).orElseThrow(() -> new CustomRunTimeException("Subission not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<Object> assessmentNextPhase(CurrentUserObject currentUserObject, Long intakeProgramId, Long summaryId) {
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeProgramId).orElseThrow(() -> new CustomRunTimeException("Intake not found", HttpStatus.NOT_FOUND));
        if (Objects.isNull(intakeProgram.getBootcampEvaluationForm()))
            throw new CustomRunTimeException("Please create a Bootcamp Evaluation Form", HttpStatus.BAD_REQUEST);
        if (!intakeProgram.getBootcampEvaluationForm().getStatus().equals(Constant.PUBLISHED.toString()))
            throw new CustomRunTimeException("Bootcamp form is not published", HttpStatus.BAD_REQUEST);
        if (openEventRepository.existsByIntakeProgram_IdAndEventPhase(intakeProgramId, Constant.BOOTCAMP.toString())) {
            return ResponseWrapper.response400("Create Bootcamp Event First", "emptyBootcampEvent");
        }
        EvaluationSummary es = evaluationSummaryRepository.findByIdAndIntakeProgram_IdAndPhaseAndStatus(summaryId, intakeProgramId, Constant.ASSESSMENT_EVALUATION_COMPLETED.toString(), Constant.SUMMARY.toString());
        if (Objects.nonNull(es)) {
            es.setStatus("");
            es.setPhase(Constant.BOOTCAMP.toString());
            evaluationSummaryRepository.save(es);
            intakeProgramSubmissionRepository.updateAssesmentNextPhaseState(intakeProgramId, es.getEmail(), Constant.BOOTCAMP.toString());
            MailMetadata mailMetadata = new MailMetadata();
            Map<String, Object> props = new HashMap<>();
            props.put("toMail", es.getEmail());
            mailMetadata.setFrom("");
            mailMetadata.setTo(es.getEmail());
            mailMetadata.setProps(props);
            mailMetadata.setSubject("Selected to assessment");
            mailMetadata.setTemplateFile("Selected to assessment");
            notificationService.sendMailMangeFormsNotifications(mailMetadata, EmailKey.selected_for_bootcamp.toString());
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("summaryId", summaryId);
            resultData.put("phase", Constant.BOOTCAMP.toString());
            return ResponseWrapper.response(resultData);
        }
        return ResponseWrapper.response400("Invalid intakeProgramId|summaryId", "intakeProgramId|summaryId");
    }

    @Transactional
    @Override
    public ResponseEntity<?> listStartups(CurrentUserObject currentUserObject, Long intakeProgramId, Pageable paging) {
        return intakeProgramRepository.findById(intakeProgramId).map(intakeProgram -> {
            if (intakeProgram.getBootcampFinished()) {
                Page<IntakeProgramSubmission> list = intakeProgramSubmissionRepository.findByUsers_Id(intakeProgramId, paging);
                return ResponseWrapper.response(list.map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionDto));
            }
            return ResponseWrapper.response(Collections.EMPTY_LIST);
        }).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
    }

    @Transactional
    @Override
    public Object inviteDueDiligence(CurrentUserObject currentUserObject, Long intakeProgramId, Long startupId, InviteDueDiligenceDto inviteDueDiligenceDto) {
        return intakeProgramSubmissionRepository.findByIdAndIntakeProgram_Id(startupId, intakeProgramId)
            .map(intakeProgramSubmission -> {
                Map<String, Object> data = new HashMap<>();
                data.put("intakeProgramId", intakeProgramId);
                data.put("intakeProgramName", intakeProgramSubmission.getIntakeProgram().getProgramName());
                data.put("dueDiligenceId", inviteDueDiligenceDto.getDueDiligenceId());
                data.put("startupId", inviteDueDiligenceDto.getStartupId());
                data.put("email", inviteDueDiligenceDto.getEmail());
                long milliSeconds = (intakeProgramSubmission.getIntakeProgram().getPeriodEnd().getTime() - new Date().getTime());
                String claims = jwtUtil.genericJwtToken(data, milliSeconds);
                if (!claims.equals("invalid_token")) {
                    MailMetadata mailMetadata = new MailMetadata();
                    Map<String, Object> props = new HashMap<>();
                    String link = uiUrl + dueDiligencePublic + "/" + claims;
                    props.put("inviteLink", link);
                    props.put("toMail", inviteDueDiligenceDto.getEmail());
                    mailMetadata.setFrom("");
                    mailMetadata.setTo(inviteDueDiligenceDto.getEmail());
                    mailMetadata.setProps(props);
                    mailMetadata.setSubject("due-diligence-public mail");
                    mailMetadata.setTemplateFile("due-diligence-public");
                    try {
                        emailUtil.sendEmail(mailMetadata);
                    } catch (MessagingException e) {
                        LOGGER.error("sendMail", e);
                    }
                }
                return data;
            }).orElseThrow(() -> new CustomRunTimeException("Submission not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<Object> assigneesBootcamps(CurrentUserObject currentUserObject, Pageable paging) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<IntakeProgramSubmission> list = intakeProgramSubmissionRepository.findByBootcampUsers_Id(user.getId(), paging);
        return ResponseWrapper.response(list.map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionDto));
    }

    @Transactional
    @Override
    public ResponseEntity<Object> getEvalSummary(CurrentUserObject currentUserObject, String phase, Long intakeProgramId, String filterBy, String filterKeyword, Pageable paging) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<EvaluationSummary> ls;
        if (phase.equals(Constant.BOOTCAMP_EVALUATION_COMPLETED.toString())) {
            if (!filterBy.isEmpty() && !filterKeyword.isEmpty()) {
                ls = evaluationSummaryRepository.findByIntakeProgram_IdAndEmailAndPhaseAndStatus(intakeProgramId, filterKeyword, phase, Constant.SUMMARY.toString(), paging);
            } else {
                ls = evaluationSummaryRepository.findByIntakeProgram_IdAndPhaseAndStatus(intakeProgramId, phase, Constant.SUMMARY.toString(), paging);
            }
        } else {
            if (!filterBy.isEmpty() && !filterKeyword.isEmpty()) {
                ls = evaluationSummaryRepository.findByIntakeProgram_IdAndEmailAndPhaseContainingAndStatus(intakeProgramId, filterKeyword, phase, Constant.SUMMARY.toString(), paging);
            } else {
                ls = evaluationSummaryRepository.findByIntakeProgram_IdAndPhaseContainingAndStatus(intakeProgramId, phase, Constant.SUMMARY.toString(), paging);
            }
        }
        return ResponseWrapper.response(ls.map(evaluationSummaryMapper::toEvaluationSummaryDto));
    }

    @Transactional
    @Override
    public Page<GetIntakeProgramSubmissionDto> listOnboardingStarups(CurrentUserObject currentUserObject, Long intakeProgramId, Pageable paging) {
        Page<IntakeProgramSubmission> ls = intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhase(intakeProgramId, Constant.BOOTCAMP_FINISH.toString(), paging);
        return ls.map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionDto);
    }

    @Transactional
    @Override
    public ResponseEntity<Object> getAssignees(List<String> rln, Pageable paging, String string, String filterKeyword) {
        Page<User> list;
        if (Objects.nonNull(filterKeyword) && !filterKeyword.isEmpty()) {
            list = userRepository.findByAliasContainingIgnoreCaseAndRole_RoleNameInAndInvitationStatus(filterKeyword, rln, paging, string);
        } else {
            list = userRepository.findByRole_RoleNameInAndInvitationStatus(rln, paging, string);
        }
        return ResponseWrapper.response(list.map(userMapper::toUserDTO));
    }

    @Transactional
    @Override
    public ResponseEntity<Object> submissionTrend(Long intakeProgramId) {
        Map<String, Object> data = new HashedMap<>();
        data.put("totalCount", intakeProgramSubmissionRepository.totalCount(intakeProgramId));
        Calendar t = Calendar.getInstance();
        t.setTime(new Date());
        data.put("todayCount", intakeProgramSubmissionRepository.getTrendCountByDay(intakeProgramId, t.get(Calendar.DAY_OF_MONTH)));
        t.set(Calendar.DATE, t.get(Calendar.DATE) - 1);
        data.put("yeterdayCount", intakeProgramSubmissionRepository.getTrendCountByDay(intakeProgramId, t.get(Calendar.DAY_OF_MONTH)));
        t.set(Calendar.DATE, t.get(Calendar.DATE) - 1);
        data.put("dayBeforeYesterdayCount", intakeProgramSubmissionRepository.getTrendCountByDay(intakeProgramId, t.get(Calendar.DAY_OF_MONTH)));
        return ResponseWrapper.response(data);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getIntakePrograms(CurrentUserObject currentUserObject, Pageable paging) {
        Page<IntakeProgram> ls = intakeProgramRepository.findByStatusNot(Constant.EXISTING_INTAKE.toString(), paging);
        return ResponseWrapper.response(ls.map(intakeProgramMapper::toGetIntakeProgramDto));
    }

    @Transactional
    @Override
    public ResponseEntity<Object> getIntakeProgram(CurrentUserObject currentUserObject, Long intakeProgramId) {
        return intakeProgramRepository.findById(intakeProgramId).map(intakeProgram -> {
            Map<String, Object> data = new HashedMap<>();
            data.put("bootcampFinished", intakeProgram.getBootcampFinished());
            data.put("assessmentFinished", intakeProgram.getAssessmentFinished());
            data.put("intakeProgramId", intakeProgram.getId());
            data.put("intakeProgramName", intakeProgram.getProgramName());
            data.put("periodStart", intakeProgram.getPeriodStart().toInstant().toEpochMilli());
            data.put("periodEnd", intakeProgram.getPeriodEnd().toInstant().toEpochMilli());
            data.put("status", intakeProgram.getStatus());
            data.put("formToken", intakeProgram.getFormToken());
            data.put("phaseStatus", intakeProgram.getPhaseStatus());
            data.put("registrationForm", intakeProgram.getRegistrationForm() != null ? registrationFormMapper.toGetRegistrationFormDto(intakeProgram.getRegistrationForm()) : null);
            data.put("assessmentEvaluationForm", intakeProgram.getAssessmentEvaluationForm() != null ? assessmentEvaluationFormMapper.toGetAssessmentEvaluationFormDto(intakeProgram.getAssessmentEvaluationForm()) : null);
            data.put("screeningEvaluationForm", intakeProgram.getScreeningEvaluationForm() != null ? screeningEvaluationFormMapper.toScreeningEvaluationFormDTO(intakeProgram.getScreeningEvaluationForm()) : null);
            Map<String, Object> bcf = null;
            if (intakeProgram.getBootcampEvaluationForm() != null) {
                bcf = new HashMap<>();
                bcf.put("formId", intakeProgram.getBootcampEvaluationForm().getId());
                bcf.put("formName", intakeProgram.getBootcampEvaluationForm().getFormName());
                bcf.put("jsonForm", intakeProgram.getBootcampEvaluationForm().getJsonForm());
                bcf.put("status", intakeProgram.getBootcampEvaluationForm().getStatus());
            }
            data.put("bootcampEvaluationForm", bcf);
            return ResponseWrapper.response(data);
        }).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<Object> deleteIntake(CurrentUserObject currentUserObject, Long intakeProgramId) {
        return intakeProgramRepository.findById(intakeProgramId).map(intakeProgram -> {
            if (intakeProgram.getStatus().equals(Constant.DRAFT.toString())) {
                intakeProgramRepository.deleteById(intakeProgramId);
                return ResponseWrapper.response(null, "intakeProgram " + intakeProgramId + "removed");
            } else {
                return ResponseWrapper.response(Constant.DRAFT + " mode cannot remove", "intakeProgramId", HttpStatus.BAD_REQUEST);
            }
        }).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> intakeProgramsShareMembers(CurrentUserObject currentUserObject, Long intakeProgramId, List<String> roles) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return intakeProgramRepository.findById(intakeProgramId)
            .map(intakeProgram -> {
                List<Object> list = new ArrayList<>();
                List<UserResourcePermission> sharedUsers = userResourcePermissionRepository
                    .findByResourceAndResourceIdAndUserIdNot(ResourceUtil.mip, intakeProgramId, currentUserObject.getUserId());
                userRepository.findByUserRoles(roles, Constant.REGISTERED.toString()).forEach(user -> {
                    if (!user.getId().equals(intakeProgram.getCreatedUser().getId())) {
                        SharedMemberDto sh = new SharedMemberDto();
                        sh.setName(user.getAlias());
                        sh.setId(user.getId());
                        sh.setSharedStatus(sharedUsers.stream().anyMatch(su -> su.getUserId().equals(user.getId())));
                        if (Objects.nonNull(user.getRole())) {
                            sh.setRoleName(user.getRole().getRoleAlias());
                        }
                        list.add(sh);
                    }
                });
                return ResponseWrapper.response(list);
            }).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> intakeProgramsSharedMembers(CurrentUserObject currentUserObject, Long intakeProgramId) {
        return null;
    }

    @Transactional
    @Override
    public ResponseEntity<?> notSelectedToAssessment(CurrentUserObject currentUserObject, Long intakePgmId) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return intakeProgramRepository.findById(intakePgmId)
            .map(intakeProgram -> {
                Map<String, Object> response = new HashMap<>();
                intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhase(intakePgmId, Constant.REGISTRATION.toString())
                    .forEach(intakeProgramSubmission -> {
                        MailMetadata mailMetadata = new MailMetadata();
                        Map<String, Object> props = new HashMap<>();
                        props.put("toMail", intakeProgramSubmission.getEmail());
                        mailMetadata.setFrom("");
                        mailMetadata.setTo(intakeProgramSubmission.getEmail());
                        mailMetadata.setProps(props);
                        mailMetadata.setSubject("not selected for assessment");
                        mailMetadata.setTemplateFile("not selected for assessment");
                        notificationService.sendMailMangeFormsNotifications(mailMetadata, EmailKey.not_selected_for_assessment.toString());
                        response.put("MailSent", intakeProgramSubmission.getEmail());
                    });
                intakeProgram.setPhaseStatus(2);
                intakeProgramRepository.save(intakeProgram);

                return ResponseWrapper.response(response);
            }).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> toggleAssessmentAttendance(CurrentUserObject currentUserObject, Long intakeProgramId, Long assessmentId, Boolean status) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> new CustomRunTimeException("Invalid user"));
        intakeProgramRepository.findById(intakeProgramId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        IntakeProgramSubmission ips = intakeProgramSubmissionRepository.findById(assessmentId).orElseThrow(() -> new CustomRunTimeException("Startup not found"));
        if (ips.getPhase().equals(Constant.ASSESSMENT_EVALUATION_START.toString()))
            throw new CustomRunTimeException("Assessment evaluation already started");
        if (status == null) {
            ips.setIsAbsent(true);
        } else {
            ips.setIsAbsent(!status);
        }
        intakeProgramSubmissionRepository.save(ips);
        return ResponseWrapper.response(null, "Updated");
    }

    @Transactional
    @Override
    public ResponseEntity<?> toggleBootcampAttendance(CurrentUserObject currentUserObject, Long intakeProgramId, Long bootcampId, Boolean status) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> new CustomRunTimeException("Invalid user"));
        intakeProgramRepository.findById(intakeProgramId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        IntakeProgramSubmission ips = intakeProgramSubmissionRepository.findById(bootcampId).orElseThrow(() -> new CustomRunTimeException("Startup not found"));
        if (ips.getPhase().equals(Constant.BOOTCAMP_EVALUATION_START.toString()))
            throw new CustomRunTimeException("Bootcamp evaluation already started");
        if (status == null) {
            ips.setIsAbsentBootcamp(true);
        } else {
            ips.setIsAbsentBootcamp(!status);
        }
        intakeProgramSubmissionRepository.save(ips);
        return ResponseWrapper.response(null, "Updated");

    }

    @Override
    public ResponseEntity<?> listAllSubmissions(CurrentUserObject currentUserObject, Long intakeProgramId, String phase, String filterKeyword, Pageable paging) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> new CustomRunTimeException("Invalid user"));
        return intakeProgramRepository.findById(intakeProgramId)
            .map(intakeProgram -> {
                Page<GetIntakeProgramSubmissionDto> list;
                Page<IntakeProgramSubmission> ls;
                if (phase.equalsIgnoreCase("Assessment")) {
                    if (filterKeyword != null && !filterKeyword.isEmpty()) {
                        ls = intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhaseContainingIgnoreCaseAndStartupNameContainingIgnoreCase(intakeProgramId, Constant.ASSESSMENT.toString(), filterKeyword, paging);
                    } else {
                        ls = intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhaseContainingIgnoreCase(intakeProgramId, Constant.ASSESSMENT.toString(), paging);
                    }
                    list = ls.map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionDto);
                } else {
                    if (filterKeyword != null && !filterKeyword.isEmpty()) {
                        ls = intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhaseContainingIgnoreCaseAndStartupNameContainingIgnoreCase(intakeProgramId, Constant.BOOTCAMP.toString(), filterKeyword, paging);
                    } else {
                        ls = intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhaseContainingIgnoreCase(intakeProgramId, Constant.BOOTCAMP.toString(), paging);
                    }
                    list = ls.map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionDto);
                }
                return ResponseWrapper.response(list);
            }).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
    }

    @Override
    public List<RegistrationLinkDTO> onGoingIntakes() {
        return intakeProgramRepository.findOngoingIntakes()
            .stream()
            .map(i ->
                new RegistrationLinkDTO("Intake " + i.getId(), i.getRegistrationForm().getDueDate(), uiUrl + "/public/registration-form/" + i.getRegistrationForm().getId())).collect(Collectors.toList()
            );
    }

    @Override
    public ResponseEntity<?> updateRegistrationFormDueDate(CurrentUserObject currentUserObject, Long intakeProgramId, Date dueDate) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> new CustomRunTimeException("User not found", HttpStatus.FORBIDDEN));
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeProgramId).orElseThrow(() -> new CustomRunTimeException("Intake not found", HttpStatus.BAD_REQUEST));
        RegistrationForm registrationForm = intakeProgram.getRegistrationForm();
        if (Objects.isNull(registrationForm))
            throw new CustomRunTimeException("Registration form is not found", HttpStatus.BAD_REQUEST);
        if (dueDate.compareTo(intakeProgram.getPeriodEnd()) > 0)
            throw new CustomRunTimeException("Registration form end date should not exceed intake's end date");
        if (dueDate.compareTo(intakeProgram.getPeriodStart()) < 0)
            throw new CustomRunTimeException("Registration form end date should not be before intake's start date");
        registrationForm.setDueDate(dueDate);
        long milliSeconds = (dueDate.getTime() - new Date().getTime());
        Map<String, Object> claims = new HashedMap<String, Object>();
        claims.put("intakeProgramPhase", Constant.REGISTRATION.toString());
        claims.put("intakeProgramId", intakeProgram.getId());
        claims.put("intakeProgramName", intakeProgram.getProgramName());
        String formToken = jwtUtil.genericJwtToken(claims, milliSeconds);
        intakeProgram.setFormToken(formToken);
        intakeProgramRepository.save(intakeProgram);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Updated successfully");
        result.put("formToken", formToken);
        result.put("data", null);
        return ResponseEntity.ok(result);
    }

    @Transactional
    @Override
    public ResponseEntity<?> publishAssessmentForm(CurrentUserObject currentUserObject, Long intakeProgramId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> new CustomRunTimeException("User not found", HttpStatus.FORBIDDEN));
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeProgramId).orElseThrow(() -> new CustomRunTimeException("Intake not found", HttpStatus.BAD_REQUEST));
        AssessmentEvaluationForm evaluationForm = intakeProgram.getAssessmentEvaluationForm();
        if (Objects.isNull(evaluationForm))
            throw new CustomRunTimeException("Assessment form not found", HttpStatus.NOT_FOUND);
        if (Objects.nonNull(evaluationForm.getStatus()) && evaluationForm.getStatus().equals(Constant.PUBLISHED.toString()))
            throw new CustomRunTimeException("Assessment form already published", HttpStatus.BAD_REQUEST);
        evaluationForm.setStatus(Constant.PUBLISHED.toString());
        evaluationForm.setPublishedUser(user);
        evaluationForm.setPublishedAt(new Date());
        intakeProgramSubmissionRepository.updateAssessmentFormJson(intakeProgramId, evaluationForm.getJsonForm());
        this.intakeProgramRepository.save(intakeProgram);
        return ResponseWrapper.response("Assessment form successfully published", HttpStatus.OK);
    }

    @Transactional
    @Override
    public ResponseEntity<?> publishBootcampForm(CurrentUserObject currentUserObject, Long intakeProgramId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> new CustomRunTimeException("User not found", HttpStatus.FORBIDDEN));
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeProgramId).orElseThrow(() -> new CustomRunTimeException("Intake not found", HttpStatus.BAD_REQUEST));
        BootcampEvaluationForm evaluationForm = intakeProgram.getBootcampEvaluationForm();
        if (Objects.isNull(evaluationForm))
            throw new CustomRunTimeException("Bootcamp form not found", HttpStatus.NOT_FOUND);
        if (Objects.nonNull(evaluationForm.getStatus()) && evaluationForm.getStatus().equals(Constant.PUBLISHED.toString()))
            throw new CustomRunTimeException("Bootcamp form already published", HttpStatus.BAD_REQUEST);
        evaluationForm.setStatus(Constant.PUBLISHED.toString());
        evaluationForm.setPublishedUser(user);
        evaluationForm.setPublishedAt(new Date());
        intakeProgramSubmissionRepository.updateBootcampFormJson(intakeProgramId, evaluationForm.getJsonForm());
        this.intakeProgramRepository.save(intakeProgram);
        return ResponseWrapper.response("Bootcamp form successfully published", HttpStatus.OK);
    }

    @Transactional
    @Override
    public ResponseEntity<?> publishScreeningForm(CurrentUserObject currentUserObject, Long intakeProgramId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> new CustomRunTimeException("User not found", HttpStatus.FORBIDDEN));
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeProgramId).orElseThrow(() -> new CustomRunTimeException("Intake not found", HttpStatus.BAD_REQUEST));
        ScreeningEvaluationForm evaluationForm = intakeProgram.getScreeningEvaluationForm();
        if (Objects.isNull(evaluationForm))
            throw new CustomRunTimeException("Screening form not found", HttpStatus.NOT_FOUND);
        if (Objects.nonNull(evaluationForm.getStatus()) && evaluationForm.getStatus().equals(Constant.PUBLISHED.toString()))
            throw new CustomRunTimeException("Screening form already published", HttpStatus.BAD_REQUEST);
        evaluationForm.setStatus(Constant.PUBLISHED.toString());
        evaluationForm.setPublishedUser(user);
        evaluationForm.setPublishedAt(new Date());
        intakeProgramSubmissionRepository.updatePreAssessmentFormJson(intakeProgramId, evaluationForm.getJsonForm());
        this.intakeProgramRepository.save(intakeProgram);
        return ResponseWrapper.response("Screening form successfully published", HttpStatus.OK);
    }

    @Override
    public void importApplications(Long intakeId, MultipartFile file) throws Exception {
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeId).orElseThrow(() -> new CustomRunTimeException("Intake not found", HttpStatus.NOT_FOUND));
        if (!intakeProgram.getStatus().equals(Constant.PUBLISHED.name()))
            throw new CustomRunTimeException("Program is not published");
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        List<Map<String, String>> applications = new ArrayList<>();
        List<String> questions = new ArrayList<>();
        List<String> profileCardQuestions = new ArrayList<>();
        for (Sheet sheet : workbook) {
            int rowCount = 0;
            for (Row row : sheet) {
                if (rowCount == 0) {
                    for (Cell cell : row) {
                        questions.add(cell.toString());
                        if (workbook.getFontAt(cell.getCellStyle().getFontIndex()).getBold())
                            profileCardQuestions.add(cell.toString());
                    }
                } else {
                    Map<String, String> application = new LinkedHashMap<>();
                    for (int i = 0; i < questions.size(); i++) {
                        Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        application.put(questions.get(i), Objects.nonNull(cell) ? cell.toString() : "");
                    }
                    applications.add(application);
                }
                rowCount++;
            }
        }
        if (questions.isEmpty() || questions.size() < 2) throw new CustomRunTimeException("Invalid file");
        List<IntakeProgramSubmission> submissions = new ArrayList<>();
        AssessmentEvaluationForm assessmentEvaluationForm = intakeProgram.getAssessmentEvaluationForm();
        BootcampEvaluationForm bootcampEvaluationForm = intakeProgram.getBootcampEvaluationForm();
        ProgressReport progressReport = intakeProgram.getProgressReport();
        applications.forEach(application -> {
            IntakeProgramSubmission submission = new IntakeProgramSubmission();
            submission.setIntakeProgram(intakeProgram);
            submission.setLanguage("null");
            submission.setPhase(Constant.REGISTRATION.toString());
            submission.setStartupName(application.get(questions.get(0)));
            submission.setEmail(application.get(questions.get(1)));
            submission.setProfileInfoJson(setProfileInfoJsonToSubmission(application, profileCardQuestions));
            submission.setJsonRegistrationForm(setJsonRegistrationFormToSubmission(application));
            submission.setJsonAssessmentEvaluationForm(Objects.nonNull(assessmentEvaluationForm) ? assessmentEvaluationForm.getJsonForm() : "");
            submission.setJsonBootcampEvaluationForm(Objects.nonNull(bootcampEvaluationForm) ? bootcampEvaluationForm.getJsonForm() : "");
            submission.setJsonProgressReport(Objects.nonNull(progressReport) ? progressReport.getJsonReportDetail() : "");
            submissions.add(submission);
        });
        intakeProgramSubmissionRepository.saveAll(submissions);
    }

    @Override
    public void importScreeningEvaluations(CurrentUserObject currentUserObject, Long intakeId, MultipartFile file) throws Exception {
        User user = userRepository.findById(currentUserObject.getUserId() != null ? currentUserObject.getUserId() : (long) 0).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeId).orElseThrow(() -> new CustomRunTimeException("Intake not found", HttpStatus.NOT_FOUND));
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        List<Map<String, String>> applications = new ArrayList<>();
        List<String> questions = new ArrayList<>();
        for (Sheet sheet : workbook) {
            int rowCount = 0;
            for (Row row : sheet) {
                if (rowCount == 0) {
                    for (Cell cell : row) {
                        questions.add(cell.toString());
                    }
                } else {
                    Map<String, String> application = new LinkedHashMap<>();
                    for (int i = 0; i < questions.size(); i++) {
                        Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        application.put(questions.get(i), Objects.nonNull(cell) ? cell.toString() : "");
                    }
                    applications.add(application);
                }
                rowCount++;
            }
        }
        List<IntakeProgramSubmission> submissionsToBeMoved = new ArrayList<>();
        applications.forEach(application -> {
            if (application.get("Final result").equals("Yes"))
                submissionsToBeMoved.add(intakeProgramSubmissionRepository.findByIntakeProgramAndStartupNameIgnoreCase(intakeProgram, application.get("Startup name?")).orElseThrow(() -> new CustomRunTimeException("Application not found")));
        });
        submissionsToBeMoved.forEach(submission -> {
            submission.setPhase(Constant.ASSESSMENT.toString());
            EvaluationSummary evaluationSummary = new EvaluationSummary();
            evaluationSummary.setSubmittedUser(user);
            evaluationSummary.setIntakeProgram(intakeProgram);
            evaluationSummary.setIntakeProgramSubmission(submission);
            evaluationSummary.setStatus(Constant.SUMMARY.toString());
            evaluationSummary.setEmail(submission.getEmail());
            evaluationSummary.setStartupName(submission.getStartupName());
            evaluationSummary.setPhase(Constant.ASSESSMENT.toString());
            evaluationSummary.setProfileCard(submission.getProfileInfoJson());
            evaluationSummary.setScreeningEvaluatorsMarks(new JSONArray().toString());
            evaluationSummary.setJudgeBootcampMarks(new JSONArray().toString());
            evaluationSummary.setJudgesMarks(new JSONArray().toString());
            evaluationSummary.setScreeningAverage(0f);
            evaluationSummary.setScreeningTotal(0f);
            evaluationSummary.setCreatedOn(LocalDateTime.now());
            evaluationSummary.setModifiedOn(LocalDateTime.now());
            submission.setEvaluationSummary(evaluationSummary);
        });
        this.intakeProgramSubmissionRepository.saveAll(submissionsToBeMoved);
    }

    private String setJsonRegistrationFormToSubmission(Map<String, String> application) {
        RegistrationFormValidationDTO dto = new RegistrationFormValidationDTO();
        dto.setMetadata(new RegistrationFormValidationDTO_metadata("Import", "Application imported from a file"));
        RegistrationFormValidationDTO_form form = new RegistrationFormValidationDTO_form();
        form.setType("custom-pager");
        form.setDefaultValue(1);
        form.setFieldGroup(new ArrayList<>());
        AtomicInteger index = new AtomicInteger(1);
        application.forEach((question, answer) -> {
            RegistrationFormValidationDTO_form_fieldGroup fieldGroup = new RegistrationFormValidationDTO_form_fieldGroup();
            fieldGroup.setKey(index.toString());
            fieldGroup.setType("custom-text");
            fieldGroup.setDefaultValue(answer);
            RegistrationFormValidationDTO_form_fieldGroup_templateOptions templateOptions = new RegistrationFormValidationDTO_form_fieldGroup_templateOptions();
            templateOptions.set_title(question);
            fieldGroup.setTemplateOptions(templateOptions);
            form.getFieldGroup().add(fieldGroup);
            index.getAndIncrement();
        });
        dto.setForm(Collections.singletonList(form));
        return new Gson().toJson(dto);
    }

    private String setProfileInfoJsonToSubmission(Map<String, String> application, List<String> profileCardQuestions) {
        List<RegistrationFormValidationDTO_form_fieldGroup> fieldGroups = new ArrayList<>();
        RegistrationFormValidationDTO_form_fieldGroup logoField = new RegistrationFormValidationDTO_form_fieldGroup();
        logoField.setType("custom-file");
        logoField.setTemplateOptions(new RegistrationFormValidationDTO_form_fieldGroup_templateOptions());
        List<RegistrationFormValidationDTO_form_fieldGroup> logoFieldGroups = new ArrayList<>();
        logoFieldGroups.add(new RegistrationFormValidationDTO_form_fieldGroup());
        logoFieldGroups.add(new RegistrationFormValidationDTO_form_fieldGroup());
        logoField.setFieldGroup(logoFieldGroups);
        fieldGroups.add(logoField);
        profileCardQuestions.forEach(question -> {
            RegistrationFormValidationDTO_form_fieldGroup fieldGroup = new RegistrationFormValidationDTO_form_fieldGroup();
            fieldGroup.setKey(Objects.toString(profileCardQuestions.indexOf(question) + 1));
            fieldGroup.setType("custom-text");
            fieldGroup.setDefaultValue(application.get(question));
            RegistrationFormValidationDTO_form_fieldGroup_templateOptions templateOptions = new RegistrationFormValidationDTO_form_fieldGroup_templateOptions();
            templateOptions.set_title(question);
            fieldGroup.setTemplateOptions(templateOptions);
            fieldGroups.add(fieldGroup);
        });
        return new Gson().toJson(fieldGroups);
    }

    @Transactional
    void assignBootcampJudge(Long bootCampId, Set<User> users, OpenEvent openEvent) {
        intakeProgramSubmissionRepository.findById(bootCampId).ifPresent(intakeProgramSubmission -> {
            intakeProgramSubmission.setPhase(Constant.BOOTCAMP_EVALUATE.toString());
            intakeProgramSubmission.setBootcampUsers(users);
            intakeProgramSubmissionRepository.save(intakeProgramSubmission);
            users.forEach(u -> addToJudgeCalender(Constant.BOOTCAMP.toString(), intakeProgramSubmission.getEmail(), u, intakeProgramSubmission.getIntakeProgram(), openEvent));
            MailMetadata mailMetadata = new MailMetadata();
            Map<String, Object> props = new HashMap<>();
            props.put("toMail", intakeProgramSubmission.getEmail());
            mailMetadata.setFrom("");
            mailMetadata.setTo(intakeProgramSubmission.getEmail());
            mailMetadata.setProps(props);
            mailMetadata.setSubject("");
            mailMetadata.setTemplateFile("");
            notificationService.sendMailMangeFormsNotifications(mailMetadata, EmailKey.assigned_to_bootcamp_judges.toString());
            IntakeProgram intakeProgram = intakeProgramSubmission.getIntakeProgram();
            Map<String, Object> data1 = new HashMap<>();
            data1.put("intakeProgramId", intakeProgram.getId());
            data1.put("intakeProgramName", intakeProgram.getProgramName());
            data1.put("openEventId", openEvent.getId());
            data1.put("email", intakeProgramSubmission.getEmail());
            long milliSeconds = (intakeProgram.getPeriodEnd().getTime() - new Date().getTime());
            String claims = jwtUtil.genericJwtToken(data1, milliSeconds);
            if (!claims.equals("invalid_token")) {
                MailMetadata mailMetadata1 = new MailMetadata();
                Map<String, Object> props1 = new HashMap<>();
                String link = uiUrl + publicCal + "?formToken=" + claims;
                props1.put("inviteLink", link);
                props1.put("toMail", intakeProgramSubmission.getEmail());
                mailMetadata1.setFrom("");
                mailMetadata1.setTo(intakeProgramSubmission.getEmail());
                mailMetadata1.setProps(props1);
                mailMetadata1.setSubject("Book slot for Bootcamp");
                mailMetadata1.setTemplateFile("Book slot for Bootcamp");
                notificationService.sendCalendlyNotification(openEvent, mailMetadata1, "Choose your convenient timing for Bootcamp");
            }
        });
    }

    @Transactional
    @Override
    public ResponseEntity<Object> assigneesBootcampEvaluations(CurrentUserObject currentUserObject, Long intakeProgramId, Pageable paging, String filterKeyword, String filterBy) {
        userRepository.findById(currentUserObject.getUserId() != null ? currentUserObject.getUserId() : (long) 0).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<IntakeProgramSubmission> list;
        if (Objects.nonNull(filterKeyword) && !filterKeyword.isEmpty()) {
            if (filterBy.equalsIgnoreCase("email")) {
                list = intakeProgramSubmissionRepository.getEvaluationsByEmail(intakeProgramId, Constant.BOOTCAMP_EVALUATE.toString(), Constant.BOOTCAMP_EVALUATION_START.toString(), Constant.BOOTCAMP_EVALUATION_STOP.toString(), paging, filterKeyword);
            } else if (filterBy.contains("Startup")) {
                list = intakeProgramSubmissionRepository.getEvaluationsByStartUp(intakeProgramId, Constant.BOOTCAMP_EVALUATE.toString(), Constant.BOOTCAMP_EVALUATION_START.toString(), Constant.BOOTCAMP_EVALUATION_STOP.toString(), paging, filterKeyword);

            } else {
                list = intakeProgramSubmissionRepository.getEvaluations(intakeProgramId, Constant.BOOTCAMP_EVALUATE.toString(), Constant.BOOTCAMP_EVALUATION_START.toString(), Constant.BOOTCAMP_EVALUATION_STOP.toString(), paging);
            }
        } else {
            list = intakeProgramSubmissionRepository.getEvaluations(intakeProgramId, Constant.BOOTCAMP_EVALUATE.toString(), Constant.BOOTCAMP_EVALUATION_START.toString(), Constant.BOOTCAMP_EVALUATION_STOP.toString(), paging);
        }
        return ResponseWrapper.response(list.map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionDto));
    }

    @Transactional
    @Override
    public Object assignBootcampJudges(CurrentUserObject currentUserObject, Long intakeProgramId, Long assessmentId, AssignJudgeDto assignJudgeDto) {
        List<OpenEvent> oes = openEventRepository.findByIntakeProgram_IdAndEventPhase(intakeProgramId, Constant.BOOTCAMP.toString());
        if (oes.isEmpty()) {
            throw new CustomRunTimeException("Please create a bootcamp event");
        }
        Set<Long> assigneeIds = assignJudgeDto.getAssigneeIds();
        Set<User> users = userRepository.findByIdIn(assigneeIds);
        assignJudgeDto.getAssessmentIds().forEach(id -> assignBootcampJudge(id, users, oes.get(0)));
        Map<String, Object> data = new HashMap<>();
        data.put("assigneeIds", assignJudgeDto.getAssigneeIds());
        data.put("assessmentIds", assignJudgeDto.getAssessmentIds());
        return data;
    }

    @Transactional
    @Override
    public ResponseEntity<Object> startBootcampEvaluation(CurrentUserObject currentUserObject, Long intakeProgramId, Long bootcampId) {
        return intakeProgramSubmissionRepository.findById(bootcampId)
            .map(intakeProgramSubmission -> {
                if (intakeProgramSubmission.getInterviewStartBootcamp() == null) {
                    return ResponseWrapper.response400("Startup did not pick interview slot", "emptyInterviewSlot");
                }
                if (intakeProgramSubmissionRepository.existsByIntakeProgram_IdAndPhase(intakeProgramId, Constant.BOOTCAMP_EVALUATION_START.toString())) {
                    return ResponseWrapper.response400("There is other bootcamp evaluation already inprogress", "intakeProgramId|bootcampId");
                }
                if (intakeProgramSubmission.getUsers().size() > 0) {
                    intakeProgramSubmission.setEvaluationStartedOn(new Date());
                    intakeProgramSubmission.setPhase(Constant.BOOTCAMP_EVALUATION_START.toString());
                    intakeProgramSubmissionRepository.save(intakeProgramSubmission);
                    notificationService.evaluationStartNotification(Constant.BOOTCAMP.toString(), intakeProgramSubmission);
                    return ResponseWrapper.response(sendWebsocketNotification(intakeProgramSubmission));
                }
                return ResponseWrapper.response400("Assign judge first", "intakeProgramId|bootcampId");
            }).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
    }

    @Transactional
    @Override
    public Object stopBootcampEvaluation(CurrentUserObject currentUserObject, Long intakeProgramId, Long bootcampId) {
        return intakeProgramSubmissionRepository.findById(bootcampId)
            .map(intakeProgramSubmission -> {
                if (intakeProgramSubmission.getPhase().equals(Constant.BOOTCAMP_EVALUATION_START.toString())) {
                    intakeProgramSubmission.setPhase(Constant.BOOTCAMP_EVALUATION_STOP.toString());
                    intakeProgramSubmission.setEvaluationEndedOn(new Date());
                    IntakeProgramSubmission e = intakeProgramSubmissionRepository.save(intakeProgramSubmission);
                    return sendWebsocketNotification(intakeProgramSubmission);
                }
                throw new CustomRunTimeException("Evaluation not started");
            }).orElseThrow(() -> new CustomRunTimeException("Submission not found"));
    }

    @Transactional
    @Override
    public Object finitBootcamp(CurrentUserObject currentUserObject, Long intakeProgramId, BootCampSelectedStartupDto selectedStartupRequest) {
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeProgramId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        List<IntakeProgramSubmission> intakeProgramSubmissionList = new ArrayList<>();
        intakeProgram.setBootcampFinished(true);
        intakeProgram.setAssessmentFinished(true);
        IntakeProgram c = intakeProgramRepository.save(intakeProgram);
        Iterable<EvaluationSummary> evaluationSummaries = evaluationSummaryRepository.findAllById(selectedStartupRequest.getStartupRegisterationIds());
        evaluationSummaries.forEach(summary -> {
            summary.setPhase(Constant.BOOTCAMP_END.toString());
            IntakeProgramSubmission submission = summary.getIntakeProgramSubmission();
            submission.setPhase(Constant.BOOTCAMP_FINISH.toString());
            intakeProgramSubmissionList.add(submission);
        });
        intakeProgramSubmissionRepository.saveAll(intakeProgramSubmissionList);
        return intakeProgramMapper.toGetIntakeProgramDto(c);
    }

    @Async
    @Transactional
    void updateStartupBootcampFinishStatus(Long intakeProgramId, String email) {
        intakeProgramSubmissionRepository.bootcampFinish(intakeProgramId, email, Constant.BOOTCAMP_END.toString(), Constant.BOOTCAMP_FINISH.toString());
    }

    @Transactional
    @Override
    public ResponseEntity<Object> bootcampSubmissions(CurrentUserObject currentUserObject, Long intakeProgramId, String string, Pageable paging, String filterKeyword, String filterBy) {
        userRepository.findById(currentUserObject.getUserId() != null ? currentUserObject.getUserId() : (long) 0).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<IntakeProgramSubmission> ls;
        if (Objects.nonNull(filterKeyword) && !filterKeyword.isEmpty()) {
            if (filterBy.equalsIgnoreCase("email")) {
                ls = intakeProgramSubmissionRepository.findByEmailContainingIgnoreCaseAndIntakeProgram_IdAndPhase(filterKeyword, intakeProgramId, string, paging);
            } else {
                ls = intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhase(intakeProgramId, string, paging);
            }
        } else {
            ls = intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhase(intakeProgramId, string, paging);
        }
        return ResponseWrapper.response(ls.map(intakeProgramSubmissionMapper::toGetIntakeProgramSubmissionDto));
    }

    @Override
    public ResponseEntity<?> notSelectedToBootCamp(CurrentUserObject currentUserObject, Long intakePgmId) {
        userRepository.findById(currentUserObject.getUserId() != null ? currentUserObject.getUserId() : (long) 0).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return intakeProgramRepository.findById(intakePgmId)
            .map(intakeProgram -> {
                Map<String, Object> response = new HashMap<>();
                List<IntakeProgramSubmission> result = intakeProgramSubmissionRepository.findByIntakeProgram_IdAndPhase(intakePgmId, Constant.ASSESSMENT.toString());
                if (Objects.nonNull(result) && !result.isEmpty()) {
                    for (IntakeProgramSubmission es : result) {
                        MailMetadata mailMetadata = new MailMetadata();
                        Map<String, Object> props = new HashMap<>();
                        props.put("toMail", es.getEmail());
                        mailMetadata.setFrom("");
                        mailMetadata.setTo(es.getEmail());
                        mailMetadata.setProps(props);
                        mailMetadata.setSubject("not selected for assessment");
                        mailMetadata.setTemplateFile("not selected for assessment");
                        notificationService.sendMailMangeFormsNotifications(mailMetadata, EmailKey.not_selected_for_assessment.toString());
                        response.put("MailSent", es.getEmail());
                    }
                    intakeProgramRepository.save(intakeProgram);
                }
                return ResponseWrapper.response(response);
            }).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
    }

    @Override
    public ResponseEntity<?> startEvaluation(CurrentUserObject currentUserObject, Long intakeId, Long applicationId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        ScreeningEvaluationForm screeningEvaluationForm = intakeProgram.getScreeningEvaluationForm();
        if (Objects.isNull(screeningEvaluationForm))
            throw new CustomRunTimeException("Please create a screening evaluation form");
        if (!screeningEvaluationForm.getStatus().equals(Constant.PUBLISHED.toString()))
            throw new CustomRunTimeException("Screening evaluation form is not published");
        IntakeProgramSubmission application = intakeProgramSubmissionRepository.findById(applicationId).orElseThrow(() -> new CustomRunTimeException("Application not found"));
        if (application.getPhase().equals(Constant.SCREENING_EVALUATION_START.toString()))
            throw new CustomRunTimeException("Evaluation already started for this application");
        application.setPhase(Constant.SCREENING_EVALUATION_START.toString());
        application.setScreeningEvaluationStartedOn(new Date());
        this.intakeProgramSubmissionRepository.save(application);
        sendWebsocketNotification(application);
        notificationService.evaluationStartNotification(Constant.SCREENING.toString(), application);
        return ResponseWrapper.response(screeningEvaluationFormMapper.toScreeningEvaluationFormDTO(screeningEvaluationForm));
    }

    private Map<String, Object> sendWebsocketNotification(IntakeProgramSubmission application) {
        Map<String, Object> data = new LinkedMap<>();
        data.put("intakeProgramId", application.getIntakeProgram().getId());
        data.put("applicationId", application.getId());
        data.put("phase", application.getPhase());
        if (application.getPhase().contains("SCREENING")) application.getScreeningEvaluators().forEach(evaluator -> {
            simpMessagingTemplate.convertAndSend("/topic/evaluationQue/user_" + evaluator.getId(), data);
        });
        else if (application.getPhase().contains("ASSESSMENT")) application.getUsers().forEach(evaluator -> {
            simpMessagingTemplate.convertAndSend("/topic/evaluationQue/user_" + evaluator.getId(), data);
        });
        else if (application.getPhase().contains("BOOTCAMP")) application.getBootcampUsers().forEach(evaluator -> {
            simpMessagingTemplate.convertAndSend("/topic/evaluationQue/user_" + evaluator.getId(), data);
        });
        return data;
    }

    @Override
    public ResponseEntity<?> stopEvaluation(CurrentUserObject currentUserObject, Long intakeId, Long applicationId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        intakeProgramRepository.findById(intakeId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        IntakeProgramSubmission application = intakeProgramSubmissionRepository.findById(applicationId).orElseThrow(() -> new CustomRunTimeException("Application not found"));
        if (application.getPhase().equals(Constant.SCREENING_EVALUATION_STOP.toString()))
            throw new CustomRunTimeException("Evaluation already stopped for this application");
        application.setPhase(Constant.SCREENING_EVALUATION_STOP.toString());
        application.setScreeningEvaluationEndedOn(new Date());
        this.intakeProgramSubmissionRepository.save(application);
        sendWebsocketNotification(application);
        return ResponseWrapper.response(null, "Evaluation stopped");
    }

    @Override
    public void startEvaluation(String phase, Long intakeId, List<Long> applicationIds) {
        if (!phase.equals("screening") && !phase.equals("assessment") && !phase.equals("bootcamp"))
            throw new CustomRunTimeException("Invalid phase received");
        if (Objects.isNull(applicationIds) || applicationIds.isEmpty())
            throw new CustomRunTimeException("Startup IDs must not be empty");
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        Iterable<IntakeProgramSubmission> applications = intakeProgramSubmissionRepository.findAllById(applicationIds);
        switch (phase) {
            case "screening":
                startScreeningEvaluations(intakeProgram, applications);
                break;
            case "assessment":
                startAssessmentEvaluations(intakeProgram, applications);
                break;
            case "bootcamp":
                startBootcampEvaluations(intakeProgram, applications);
                break;
        }
    }

    @Override
    public void stopEvaluation(String phase, Long intakeId, List<Long> applicationIds) {
        if (!phase.equals("screening") && !phase.equals("assessment") && !phase.equals("bootcamp"))
            throw new CustomRunTimeException("Invalid phase received");
        if (Objects.isNull(applicationIds) || applicationIds.isEmpty())
            throw new CustomRunTimeException("Startup IDs must not be empty");
        intakeProgramRepository.findById(intakeId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        Iterable<IntakeProgramSubmission> applications = intakeProgramSubmissionRepository.findAllById(applicationIds);
        switch (phase) {
            case "screening":
                stopScreeningEvaluations(applications);
                break;
            case "assessment":
                stopAssessmentEvaluations(applications);
                break;
            case "bootcamp":
                stopBootcampEvaluations(applications);
                break;
        }
    }

    @Override
    public void generateSummary(String phase, Long intakeId, List<Long> applicationIds) {
        if (!phase.equals("screening") && !phase.equals("assessment") && !phase.equals("bootcamp"))
            throw new CustomRunTimeException("Invalid phase received");
        if (Objects.isNull(applicationIds) || applicationIds.isEmpty())
            throw new CustomRunTimeException("Startup IDs must not be empty");
        intakeProgramRepository.findById(intakeId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        Iterable<IntakeProgramSubmission> applications = intakeProgramSubmissionRepository.findAllById(applicationIds);
        List<IntakeProgramSubmission> applicationsToBeUpdated = new ArrayList<>();
        switch (phase) {
            case "screening":
                applicationsToBeUpdated = generateScreeningSummary(applications);
                break;
            case "assessment":
                applicationsToBeUpdated = generateAssessmentSummary(applications);
                break;
            case "bootcamp":
                applicationsToBeUpdated = generateBootcampSummary(applications);
                break;
            default:
                throw new CustomRunTimeException("Invalid phase received in the request");
        }
        if (applicationsToBeUpdated.isEmpty())
            throw new CustomRunTimeException("Failed to generate summary for the given startups");
        this.intakeProgramSubmissionRepository.saveAll(applicationsToBeUpdated);
    }

    @Override
    public void moveToNextPhase(String phase, Long intakeId, List<Long> applicationIds) {
        if (!phase.equals("screening") && !phase.equals("assessment"))
            throw new CustomRunTimeException("Invalid phase received");
        if (Objects.isNull(applicationIds) || applicationIds.isEmpty())
            throw new CustomRunTimeException("Startup IDs must not be empty");
        IntakeProgram intakeProgram = intakeProgramRepository.findById(intakeId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        Iterable<IntakeProgramSubmission> applications = intakeProgramSubmissionRepository.findAllById(applicationIds);
        switch (phase) {
            case "screening":
                moveToAssessment(intakeProgram, applications);
                break;
            case "assessment":
                moveToBootcamp(intakeProgram, applications);
                break;
            default:
                throw new CustomRunTimeException("Invalid phase received in the request");
        }
    }

    private void moveToAssessment(IntakeProgram intakeProgram, Iterable<IntakeProgramSubmission> applications) {
        if (Objects.isNull(intakeProgram.getAssessmentEvaluationForm()))
            throw new CustomRunTimeException("Please create an Assessment Evaluation Form", HttpStatus.BAD_REQUEST);
        if (!intakeProgram.getAssessmentEvaluationForm().getStatus().equals(Constant.PUBLISHED.toString()))
            throw new CustomRunTimeException("Assessment form is not published", HttpStatus.BAD_REQUEST);
        if (openEventRepository.existsByIntakeProgramAndEventPhase(intakeProgram, Constant.ASSESSMENT.toString()).equals(Boolean.FALSE))
            throw new CustomRunTimeException("Assessment event not created");
        List<IntakeProgramSubmission> applicationsToBeUpdated = new ArrayList<>();
        applications.forEach(application -> {
            if (!application.getPhase().equals(Constant.SCREENING_EVALUATION_COMPLETED.toString()))
                throw new CustomRunTimeException("Summary not generated for " + application.getStartupName());
            application.setPhase(Constant.ASSESSMENT.toString());
            application.getEvaluationSummary().setPhase("SCREENING_SUMMARY");
            applicationsToBeUpdated.add(application);
        });

        if (applicationsToBeUpdated.isEmpty())
            throw new CustomRunTimeException("Failed to perform this action for the given startups");
        this.intakeProgramSubmissionRepository.saveAll(applicationsToBeUpdated);
        applications.forEach(application -> {
            MailMetadata mailMetadata = new MailMetadata();
            Map<String, Object> props = new HashMap<>();
            props.put("toMail", application.getEmail());
            mailMetadata.setFrom("");
            mailMetadata.setTo(application.getEmail());
            mailMetadata.setProps(props);
            mailMetadata.setSubject("Selected to assessment");
            mailMetadata.setTemplateFile("Selected to assessment");
            notificationService.sendMailMangeFormsNotifications(mailMetadata, EmailKey.selected_for_assessment.toString());
        });
    }

    private void moveToBootcamp(IntakeProgram intakeProgram, Iterable<IntakeProgramSubmission> applications) {
        if (Objects.isNull(intakeProgram.getBootcampEvaluationForm()))
            throw new CustomRunTimeException("Please create an Bootcamp Evaluation Form", HttpStatus.BAD_REQUEST);
        if (!intakeProgram.getBootcampEvaluationForm().getStatus().equals(Constant.PUBLISHED.toString()))
            throw new CustomRunTimeException("Bootcamp form is not published", HttpStatus.BAD_REQUEST);
        if (openEventRepository.existsByIntakeProgramAndEventPhase(intakeProgram, Constant.BOOTCAMP.toString()).equals(Boolean.FALSE))
            throw new CustomRunTimeException("Bootcamp event not created");
        List<IntakeProgramSubmission> applicationsToBeUpdated = new ArrayList<>();
        applications.forEach(application -> {
            if (!application.getPhase().equals(Constant.ASSESSMENT_EVALUATION_COMPLETED.toString()))
                throw new CustomRunTimeException("Summary not generated for " + application.getStartupName());
            application.setPhase(Constant.BOOTCAMP.toString());
            application.getEvaluationSummary().setPhase(Constant.BOOTCAMP.toString());
            applicationsToBeUpdated.add(application);
        });

        if (applicationsToBeUpdated.isEmpty())
            throw new CustomRunTimeException("Failed to perform this action for the given startups");
        this.intakeProgramSubmissionRepository.saveAll(applicationsToBeUpdated);
        applications.forEach(application -> {
            MailMetadata mailMetadata = new MailMetadata();
            Map<String, Object> props = new HashMap<>();
            props.put("toMail", application.getEmail());
            mailMetadata.setFrom("");
            mailMetadata.setTo(application.getEmail());
            mailMetadata.setProps(props);
            mailMetadata.setSubject("Selected to bootcamp");
            mailMetadata.setTemplateFile("Selected to bootcamp");
            notificationService.sendMailMangeFormsNotifications(mailMetadata, EmailKey.selected_for_bootcamp.toString());
        });
    }

    private List<IntakeProgramSubmission> generateScreeningSummary(Iterable<IntakeProgramSubmission> applications) {
        List<IntakeProgramSubmission> applicationsToBeUpdated = new ArrayList<>();
        applications.forEach(application -> {
            if (application.getPhase().equals(Constant.SCREENING_EVALUATION_START.toString()) || application.getPhase().equals(Constant.SCREENING_EVALUATION_STOP.toString())) {
                EvaluationSummary evaluationSummary = application.getEvaluationSummary();
                if (Objects.isNull(evaluationSummary))
                    throw new CustomRunTimeException("Evaluation not conducted for " + application.getStartupName());
                evaluationSummary.setPhase(Constant.SCREENING_EVALUATION_COMPLETED.toString());
                evaluationSummary.setStatus(Constant.SUMMARY.toString());
                application.setPhase(Constant.SCREENING_EVALUATION_COMPLETED.toString());
                applicationsToBeUpdated.add(application);
            }
        });
        return applicationsToBeUpdated;
    }

    private List<IntakeProgramSubmission> generateAssessmentSummary(Iterable<IntakeProgramSubmission> applications) {
        List<IntakeProgramSubmission> applicationsToBeUpdated = new ArrayList<>();
        applications.forEach(application -> {
            if (application.getPhase().equals(Constant.ASSESSMENT_EVALUATION_START.toString()) || application.getPhase().equals(Constant.ASSESSMENT_EVALUATION_STOP.toString())) {
                EvaluationSummary evaluationSummary = application.getEvaluationSummary();
                if (Objects.isNull(evaluationSummary))
                    throw new CustomRunTimeException("Evaluation not conducted for " + application.getStartupName());
                List<EvaluatorMarksDTO> evaluatorMarksDTOS = new Gson().fromJson(evaluationSummary.getJudgesMarks(), new TypeToken<ArrayList<EvaluatorMarksDTO>>() {
                }.getType());
                if (Objects.isNull(evaluatorMarksDTOS) || evaluatorMarksDTOS.isEmpty())
                    throw new CustomRunTimeException("Evaluation not conducted for " + application.getStartupName());
                evaluationSummary.setPhase(Constant.ASSESSMENT_EVALUATION_COMPLETED.toString());
                evaluationSummary.setStatus(Constant.SUMMARY.toString());
                application.setPhase(Constant.ASSESSMENT_EVALUATION_COMPLETED.toString());
                applicationsToBeUpdated.add(application);
            }
        });
        return applicationsToBeUpdated;
    }

    private List<IntakeProgramSubmission> generateBootcampSummary(Iterable<IntakeProgramSubmission> applications) {
        List<IntakeProgramSubmission> applicationsToBeUpdated = new ArrayList<>();
        applications.forEach(application -> {
            if (application.getPhase().equals(Constant.BOOTCAMP_EVALUATION_START.toString()) || application.getPhase().equals(Constant.BOOTCAMP_EVALUATION_STOP.toString())) {
                EvaluationSummary evaluationSummary = application.getEvaluationSummary();
                if (Objects.isNull(evaluationSummary))
                    throw new CustomRunTimeException("Evaluation not conducted for " + application.getStartupName());
                List<EvaluatorMarksDTO> evaluatorMarksDTOS = new Gson().fromJson(evaluationSummary.getJudgeBootcampMarks(), new TypeToken<ArrayList<EvaluatorMarksDTO>>() {
                }.getType());
                if (Objects.isNull(evaluatorMarksDTOS) || evaluatorMarksDTOS.isEmpty())
                    throw new CustomRunTimeException("Evaluation not conducted for " + application.getStartupName());
                evaluationSummary.setPhase(Constant.BOOTCAMP_EVALUATION_COMPLETED.toString());
                evaluationSummary.setStatus(Constant.SUMMARY.toString());
                application.setPhase(Constant.BOOTCAMP_EVALUATION_COMPLETED.toString());
                applicationsToBeUpdated.add(application);
            }
        });
        return applicationsToBeUpdated;
    }

    private void startScreeningEvaluations(IntakeProgram intakeProgram, Iterable<IntakeProgramSubmission> applications) {
        ScreeningEvaluationForm screeningEvaluationForm = intakeProgram.getScreeningEvaluationForm();
        if (Objects.isNull(screeningEvaluationForm))
            throw new CustomRunTimeException("Please create a screening evaluation form");
        if (!screeningEvaluationForm.getStatus().equals(Constant.PUBLISHED.toString()))
            throw new CustomRunTimeException("Screening evaluation form is not published");
        List<IntakeProgramSubmission> applicationToBeUpdated = new ArrayList<>();
        applications.forEach(app -> {
            if (app.getPhase().equals(Constant.REGISTRATION.toString()) || app.getPhase().equals(Constant.SCREENING_EVALUATION_STOP.toString())) {
                app.setPhase(Constant.SCREENING_EVALUATION_START.toString());
                app.setScreeningEvaluationStartedOn(new Date());
                applicationToBeUpdated.add(app);
            }
        });
        if (applicationToBeUpdated.isEmpty())
            throw new CustomRunTimeException("Failed to start evaluation for the given startups");
        this.intakeProgramSubmissionRepository.saveAll(applicationToBeUpdated);
        applicationToBeUpdated.forEach(application -> {
            sendWebsocketNotification(application);
            notificationService.evaluationStartNotification(Constant.SCREENING.toString(), application);
        });
    }

    private void startAssessmentEvaluations(IntakeProgram intakeProgram, Iterable<IntakeProgramSubmission> applications) {
        AssessmentEvaluationForm assessmentEvaluationForm = intakeProgram.getAssessmentEvaluationForm();
        if (Objects.isNull(assessmentEvaluationForm))
            throw new CustomRunTimeException("Please create an assessment evaluation form");
        if (!assessmentEvaluationForm.getStatus().equals(Constant.PUBLISHED.toString()))
            throw new CustomRunTimeException("Assessment evaluation form is not published");
        List<IntakeProgramSubmission> applicationToBeUpdated = new ArrayList<>();
        applications.forEach(app -> {
            if (app.getPhase().equals(Constant.ASSESSMENT.toString()) || app.getPhase().equals(Constant.ASSESSMENT_EVALUATE.toString()) || app.getPhase().equals(Constant.ASSESSMENT_EVALUATION_STOP.toString())) {
                if (Objects.nonNull(app.getIsAbsent()) && app.getIsAbsent())
                    throw new CustomRunTimeException(app.getStartupName() + " marked as absent");
                if (Objects.isNull(app.getInterviewStart()))
                    throw new CustomRunTimeException("Startup: " + app.getStartupName() + " didn't pick an interview slot");
                app.setPhase(Constant.ASSESSMENT_EVALUATION_START.toString());
                app.setEvaluationStartedOn(new Date());
                applicationToBeUpdated.add(app);
            }
        });
        if (applicationToBeUpdated.isEmpty())
            throw new CustomRunTimeException("Failed to start evaluation for the given startups");
        this.intakeProgramSubmissionRepository.saveAll(applicationToBeUpdated);
        applicationToBeUpdated.forEach(application -> {
            sendWebsocketNotification(application);
            notificationService.evaluationStartNotification(Constant.ASSESSMENT.toString(), application);
        });
    }

    private void startBootcampEvaluations(IntakeProgram intakeProgram, Iterable<IntakeProgramSubmission> applications) {
        BootcampEvaluationForm bootcampEvaluationForm = intakeProgram.getBootcampEvaluationForm();
        if (Objects.isNull(bootcampEvaluationForm))
            throw new CustomRunTimeException("Please create a bootcamp evaluation form");
        if (!bootcampEvaluationForm.getStatus().equals(Constant.PUBLISHED.toString()))
            throw new CustomRunTimeException("Bootcamp evaluation form is not published");
        List<IntakeProgramSubmission> applicationToBeUpdated = new ArrayList<>();
        applications.forEach(app -> {
            if (Objects.nonNull(app.getIsAbsentBootcamp()) && app.getIsAbsentBootcamp())
                throw new CustomRunTimeException(app.getStartupName() + " marked as absent");
            if (Objects.isNull(app.getInterviewStartBootcamp()))
                throw new CustomRunTimeException("Startup: " + app.getStartupName() + " didn't pick an interview slot");
            if (app.getPhase().equals(Constant.BOOTCAMP.toString()) || app.getPhase().equals(Constant.BOOTCAMP_EVALUATE.toString()) || app.getPhase().equals(Constant.BOOTCAMP_EVALUATION_STOP.toString())) {
                app.setPhase(Constant.BOOTCAMP_EVALUATION_START.toString());
                app.setBootcampEvaluationStartedOn(new Date());
                applicationToBeUpdated.add(app);
            }
        });
        if (applicationToBeUpdated.isEmpty())
            throw new CustomRunTimeException("Failed to start evaluation for the given startups");
        this.intakeProgramSubmissionRepository.saveAll(applicationToBeUpdated);
        applicationToBeUpdated.forEach(application -> {
            sendWebsocketNotification(application);
            notificationService.evaluationStartNotification(Constant.BOOTCAMP.toString(), application);
        });
    }

    private void stopScreeningEvaluations(Iterable<IntakeProgramSubmission> applications) {
        List<IntakeProgramSubmission> applicationToBeUpdated = new ArrayList<>();
        applications.forEach(application -> {
            if (application.getPhase().equals(Constant.SCREENING_EVALUATION_START.toString())) {
                application.setPhase(Constant.SCREENING_EVALUATION_STOP.toString());
                application.setScreeningEvaluationEndedOn(new Date());
                applicationToBeUpdated.add(application);
            }
        });
        if (applicationToBeUpdated.isEmpty())
            throw new CustomRunTimeException("Failed to stop evaluation for the given startups");
        this.intakeProgramSubmissionRepository.saveAll(applicationToBeUpdated);
        applicationToBeUpdated.forEach(this::sendWebsocketNotification);
    }

    private void stopAssessmentEvaluations(Iterable<IntakeProgramSubmission> applications) {
        List<IntakeProgramSubmission> applicationToBeUpdated = new ArrayList<>();
        applications.forEach(application -> {
            if (application.getPhase().equals(Constant.ASSESSMENT_EVALUATION_START.toString())) {
                application.setPhase(Constant.ASSESSMENT_EVALUATION_STOP.toString());
                application.setEvaluationEndedOn(new Date());
                applicationToBeUpdated.add(application);
            }
        });
        if (applicationToBeUpdated.isEmpty())
            throw new CustomRunTimeException("Failed to stop evaluation for the given startups");
        this.intakeProgramSubmissionRepository.saveAll(applicationToBeUpdated);
        applicationToBeUpdated.forEach(this::sendWebsocketNotification);
    }

    private void stopBootcampEvaluations(Iterable<IntakeProgramSubmission> applications) {
        List<IntakeProgramSubmission> applicationToBeUpdated = new ArrayList<>();
        applications.forEach(application -> {
            if (application.getPhase().equals(Constant.BOOTCAMP_EVALUATION_START.toString())) {
                application.setPhase(Constant.BOOTCAMP_EVALUATION_STOP.toString());
                application.setBootcampEvaluationEndedOn(new Date());
                applicationToBeUpdated.add(application);
            }
        });
        if (applicationToBeUpdated.isEmpty())
            throw new CustomRunTimeException("Failed to stop evaluation for the given startups");
        this.intakeProgramSubmissionRepository.saveAll(applicationToBeUpdated);
        applicationToBeUpdated.forEach(this::sendWebsocketNotification);
    }


    @Transactional
    @Override
    public ResponseEntity<Object> getScreeningSummary(CurrentUserObject currentUserObject, Long intakeProgramId, String filterBy, String filterKeyword, Pageable paging) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId() != null ? currentUserObject.getUserId() : (long) 0);
        if (!user.isPresent()) {
            return ResponseWrapper.response(Page.empty());
        }
        Page<EvaluationSummary> ls;
        if (!filterBy.isEmpty() && !filterKeyword.isEmpty()) {
            ls = evaluationSummaryRepository.findByIntakeProgram_IdAndEmailAndPhaseAndStatus(intakeProgramId, filterKeyword, Constant.SCREENING_EVALUATION_COMPLETED.toString(), Constant.SUMMARY.toString(), paging);
        } else {
            ls = evaluationSummaryRepository.findByIntakeProgram_IdAndPhaseAndStatus(intakeProgramId, Constant.SCREENING_EVALUATION_COMPLETED.toString(), Constant.SUMMARY.toString(), paging);
        }
        return ResponseWrapper.response(ls.map(evaluationSummaryMapper::toEvaluationSummaryDto));
    }

    @Override
    public StartupEvaluationSummaryDTO findEvaluatorsSummary(Long applicationId, String phase) {
        IntakeProgramSubmission application = intakeProgramSubmissionRepository.findById(applicationId).orElseThrow(() -> new CustomRunTimeException("Application not found"));
        EvaluationSummary evaluationSummary = application.getEvaluationSummary();
        Set<User> evaluators;
        List<EvaluatorMarksDTO> evaluatorMarksDTOS = new ArrayList<>();
        switch (phase) {
            case "screening":
                evaluators = application.getScreeningEvaluators();
                if (Objects.nonNull(evaluationSummary))
                    evaluatorMarksDTOS = new Gson().fromJson(evaluationSummary.getScreeningEvaluatorsMarks(), new TypeToken<ArrayList<EvaluatorMarksDTO>>() {
                    }.getType());
                break;
            case "assessment":
                evaluators = application.getUsers();
                if (Objects.nonNull(evaluationSummary))
                    evaluatorMarksDTOS = new Gson().fromJson(evaluationSummary.getJudgesMarks(), new TypeToken<ArrayList<EvaluatorMarksDTO>>() {
                    }.getType());
                break;
            case "bootcamp":
                evaluators = application.getBootcampUsers();
                if (Objects.nonNull(evaluationSummary))
                    evaluatorMarksDTOS = new Gson().fromJson(evaluationSummary.getJudgeBootcampMarks(), new TypeToken<ArrayList<EvaluatorMarksDTO>>() {
                    }.getType());
                break;
            default:
                throw new CustomRunTimeException("Invalid phase");
        }
        StartupEvaluationSummaryDTO dto = new StartupEvaluationSummaryDTO();
        dto.setApplicationId(applicationId);
        dto.setPhase(application.getPhase());
        dto.setStartupName(application.getStartupName());
        List<StartupEvaluationSummaryEvaluatorDTO> evaluatorDTOs = evaluators.stream().map(evaluator -> {
            StartupEvaluationSummaryEvaluatorDTO evaluatorDTO = new StartupEvaluationSummaryEvaluatorDTO();
            evaluatorDTO.setEvaluator(evaluator.getAlias());
            evaluatorDTO.setEvaluatorId(evaluator.getId());
            evaluatorDTO.setHasConductedEvaluation(Boolean.FALSE);
            evaluatorDTO.setMarks(0f);
            return evaluatorDTO;
        }).collect(Collectors.toList());
        evaluatorMarksDTOS.forEach(markDTO -> {
            StartupEvaluationSummaryEvaluatorDTO evaluatorDTO = evaluatorDTOs.stream().filter(evalDto -> evalDto.getEvaluatorId().equals(markDTO.getEvaluatorId())).findFirst().orElse(null);
            if (Objects.nonNull(evaluatorDTO)) {
                evaluatorDTO.setMarks(markDTO.getMarks());
                evaluatorDTO.setHasConductedEvaluation(Boolean.TRUE);
            }
        });
        dto.setEvaluators(evaluatorDTOs);
        return dto;
    }

}
