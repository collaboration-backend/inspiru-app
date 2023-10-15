package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.jpa.projections.ProjectIdNamePathAssignment;
import com.stc.inspireu.mappers.*;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.AcademyRoomService;
import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.services.ResourcePermissionService;
import com.stc.inspireu.specifications.WorkShopSpecification;
import com.stc.inspireu.utils.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademyRoomServiceImpl implements AcademyRoomService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final AcademyRoomRepository academyRoomRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final WorkshopSessionRepository workshopSessionRepository;
    private final TrainingMaterialRepository trainingMaterialRepository;
    private final SurveyRepository surveyRepository;
    private final FeedbackRepository feedbackRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentFileRepository assignmentFileRepository;
    private final FileAdapter fileAdapter;
    private final AssignmentRepositoryCustom assignmentRepositoryCustom;
    private final IntakeProgramRepository intakeProgramRepository;
    private final StartupRepository startupRepository;
    private final WorkshopSessionSubmissionsRepository workshopSessionSubmissionsRepository;
    private final SurveyRepositoryCustom surveyRepositoryCustom;
    private final FeedbackRepositoryCustom feedbackRepositoryCustom;
    private final ResourcePermissionService resourcePermissionService;
    private final UserResourcePermissionRepository userResourcePermissionRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final AcademyRoomMapper academyRoomMapper;
    private final AssignmentMapper assignmentMapper;
    private final WorkshopSessionMapper workshopSessionMapper;
    private final WorkshopSessionSubmissionsMapper workshopSessionSubmissionsMapper;
    private final TrainingMaterialMapper trainingMaterialMapper;
    private final UserMapper userMapper;
    private final FeedbackMapper feedbackMapper;
    private final AssignmentFileMapper assignmentFileMapper;
    private final SurveyMapper surveyMapper;

    @Override
    @Transactional
    public List<AcademyRoomDto> getStartupAcademyRoomsByAcademyRoomStatus(CurrentUserObject currentUserObject,
                                                                          String academyRoomStatus) {
        List<AcademyRoomDto> list = Collections.emptyList();
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (Objects.nonNull(user.getStartup()) && Objects.nonNull(user.getStartup().getIntakeProgram())) {
            if (academyRoomStatus.equals(Constant.NEW.toString())) {
                List<AcademyRoom> ls = academyRoomRepository.getByStatusNewAfterNow(
                    user.getStartup().getIntakeProgram().getId(), user.getStartup().getId(),
                    academyRoomStatus, new Date());
                list = academyRoomMapper.toAcademyRoomDTOList(ls);
            } else if (academyRoomStatus.equals(Constant.IN_PROGRESS.toString())) {
                List<AcademyRoom> ls = academyRoomRepository.findByStartup_IdAndStatusAndSessionEndGreaterThan(
                    user.getStartup().getId(), academyRoomStatus, new Date());
                list = academyRoomMapper.toAcademyRoomDTOList(ls);
            } else if (academyRoomStatus.equals(Constant.COMPLETE.toString())) {
                List<AcademyRoom> ls = academyRoomRepository
                    .findByStartup_IdAndSessionEndLessThan(user.getStartup().getId(), new Date());
                list = academyRoomMapper.toAcademyRoomDTOList(ls);
            }
        }
        return list;
    }

    @Transactional
    @Override
    public Map<String, Object> getStartupAcademyRoomsById(CurrentUserObject currentUserObject, Long academyRoomId) {
        Map<String, Object> obj = new HashMap<>();
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (Objects.nonNull(user.getStartup())) {
            academyRoomRepository.findByIdAndStartup_Id(academyRoomId,
                user.getStartup().getId()).ifPresent(academyRoom -> {
                List<WorkshopSession> wsl1 = workshopSessionRepository.getByStartupAcademyRoomId(
                    user.getStartup().getId(), academyRoom.getRefAcademyRoom().getId(),
                    academyRoom.getId(), Constant.PUBLISHED.toString());
                List<GetWorkshopSessionDto> getWorkshopSessionDtos = workshopSessionMapper.toGetWorkshopSessionDtoList(wsl1);
                obj.put("workshopSessions", getWorkshopSessionDtos);
                obj.put("academyRoomDescription", academyRoom.getDescription());
                obj.put("academyRoomName", academyRoom.getName());
                obj.put("academyRoomId", academyRoom.getId());
            });
        }
        return obj;
    }

    @Transactional
    @Override
    public Object getStartupAcademyRoomWorksopSession(CurrentUserObject currentUserObject, Long academyRoomId,
                                                      Long workshopSessionId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (Objects.nonNull(user.getStartup())) {
            WorkshopSession entry = workshopSessionRepository.findByIdAndAcademyRoom_IdAndStartup_Id(workshopSessionId,
                academyRoomId, user.getStartup().getId());
            if (entry == null) {
                return "academyRoomId and workshopSessionId combination not found for this startup";
            }
            Map<String, Object> obj = new HashMap<>();
            obj.put("startupId", user.getStartup().getId());
            obj.put("academyRoomId", academyRoomId);
            obj.put("workshopSessionId", workshopSessionId);
            obj.put("workshopSessionName", entry.getName());
            obj.put("workshopSessionDescription", entry.getDescription());
            return obj;
        }
        return "academyRoom not initialized for this startup";
    }

    @Transactional
    @Override
    public Page<TrainingMaterial> getWorksopSessionTrainingMaterials(CurrentUserObject currentUserObject,
                                                                     Long workshopSessionId, String name, Pageable paging, String filterBy) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        AtomicReference<Page<TrainingMaterial>> ls = new AtomicReference<>(Page.empty());
        if (Objects.nonNull(user.getStartup())) {
            workshopSessionRepository.findByIdAndStartup_Id(workshopSessionId,
                user.getStartup().getId()).ifPresent(workshopSession -> {
                if (Objects.nonNull(workshopSession.getRefWorkshopSession())) {
                    if (name.equals("")) {
                        ls.set(trainingMaterialRepository
                            .findAllByWorkshopSession_Id(workshopSession.getRefWorkshopSession().getId(), paging));
                    } else {
                        ls.set(trainingMaterialRepository.findAllByWorkshopSession_IdAndName(
                            workshopSession.getRefWorkshopSession().getId(), name, paging));
                    }
                }
            });
        }

        return ls.get();
    }

    @Override
    @Transactional
    public List<SurveyDto> getWorksopSessionSurveys(CurrentUserObject currentUserObject, Long workshopSessionId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        AtomicReference<List<SurveyDto>> ls = new AtomicReference<>(Collections.emptyList());
        if (Objects.nonNull(user.getStartup())) {
            workshopSessionRepository.findByIdAndAndStartup_Id(workshopSessionId,
                user.getStartup().getId()).ifPresent(workshopSession -> {
                if (Objects.nonNull(workshopSession.getRefWorkshopSession())) {
                    ls.set(SurveyDto.fromEntityList(surveyRepository.getStartupWorkshopSessionSurveys(
                        user.getStartup().getId(), workshopSession.getRefWorkshopSession().getId(), workshopSession.getId(),
                        Constant.PUBLISHED.toString())));
                }
            });
        }
        return ls.get();
    }

    @Override
    @Transactional
    public List<GetFeedbackDto> getWorksopSessionFeedbacks(CurrentUserObject currentUserObject,
                                                           Long workshopSessionId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        AtomicReference<List<GetFeedbackDto>> ls = new AtomicReference<>(Collections.emptyList());
        if (Objects.nonNull(user.getStartup())) {
            workshopSessionRepository.findByIdAndAndStartup_Id(workshopSessionId,
                user.getStartup().getId()).ifPresent(workshopSession -> {
                if (Objects.nonNull(workshopSession.getRefWorkshopSession())) {
                    List<Feedback> feedbacks = feedbackRepository
                        .findByForStartup_IdAndWorkshopSession_IdAndRefFeedback_Status(user.getStartup().getId(),
                            workshopSession.getRefWorkshopSession().getId(), Constant.PUBLISHED.toString());
                    ls.set(feedbackMapper.toGetFeedbackDtoList(feedbacks));
                }
            });
        }
        return ls.get();
    }

    @Override
    @Transactional
    public List<GetAssignmentDto> getWorksopSessionAssignments(CurrentUserObject currentUserObject,
                                                               Long workshopSessionId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        AtomicReference<List<GetAssignmentDto>> ls = new AtomicReference<>(Collections.emptyList());
        if (Objects.nonNull(user.getStartup())) {
            workshopSessionRepository.findByIdAndStartup_Id(workshopSessionId,
                user.getStartup().getId()).ifPresent(workshopSession -> {
                if (Objects.nonNull(workshopSession.getRefWorkshopSession())) {
                    List<Assignment> assignments = assignmentRepository.getStartupWorkshopSessionAssignments(
                        user.getStartup().getId(), workshopSession.getRefWorkshopSession().getId(), workshopSession.getId());
                    ls.set(assignmentMapper.toGetAssignmentDtoList(assignments));
                }
            });
        }
        return ls.get();
    }

    @Override
    @Transactional
    public void submitWorksopSessionSurvey(CurrentUserObject currentUserObject, Long workshopSessionId, Long surveyId,
                                           SubmitStartupSurveyDto submitStartupSurveyDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (Objects.nonNull(user.getStartup())) {
            Optional<WorkshopSession> e = workshopSessionRepository.findByIdAndStartup_Id(workshopSessionId,
                user.getStartup().getId());
            if (e.isPresent()) {
                Optional<Survey> surveyValidity = surveyRepository.findByIdAndWorkshopSession_Id(surveyId,
                    e.get().getRefWorkshopSession().getId());
                if (surveyValidity.isPresent()) {
                    Optional<Survey> entry = surveyRepository.findByRefSurvey_IdAndSubmittedStartup_Id(surveyId,
                        user.getStartup().getId());
                    if (!entry.isPresent()) {
                        Survey e1 = new Survey();

                        e1.setWorkshopSession(e.get());
                        e1.setSubmittedUser(user);
                        e1.setSubmittedStartup(user.getStartup());
                        e1.setCreatedUser(surveyValidity.get().getCreatedUser());
                        e1.setDueDate(surveyValidity.get().getDueDate());
                        e1.setName(surveyValidity.get().getName());
                        e1.setRefSurvey(surveyValidity.get());
                        e1.setStatus(Constant.SUBMITTED.toString());
                        e1.setSubmittedOn(new Date());
                        e1.setJsonForm(submitStartupSurveyDto.getJsonForm());
                        surveyRepository.save(e1);
                        // create workshop submission logs
                        WorkshopSessionSubmissions wss = new WorkshopSessionSubmissions();
                        wss.setCreatedUser(e1.getCreatedUser());
                        wss.setFileType(Constant.SURVEY.toString());
                        wss.setMetaDataId(e1.getId());
                        wss.setCreatedOn(e1.getCreatedOn());
                        wss.setSubmittedUser(e1.getSubmittedUser());
                        wss.setStartup(e1.getSubmittedStartup());
                        wss.setStatus(e1.getStatus());
                        wss.setSubmittedOn(e1.getSubmittedOn());
                        wss.setSubmittedFileName(e1.getName());
                        wss.setWorkshopSession(e1.getWorkshopSession());
                        wss.setMetaDataParentId(e1.getRefSurvey().getId());
                        workshopSessionSubmissionsRepository.save(wss);
                        notificationService.surveySubmitNotification(user, surveyValidity.get().getCreatedUser());
                    }
                }
            }
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> getWorkshopSessionForStartups(CurrentUserObject currentUserObject, Long academicRoomId,
                                                           Long startupId) {
        Map<String, Object> obj = new HashMap<>();
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        startupRepository.findById(startupId).ifPresent(startup -> {
            Optional<AcademyRoom> academyRoom = academyRoomRepository.findByIdAndStartup_Id(academicRoomId,
                startupId);
            if (academyRoom.isPresent()) {
                List<WorkshopSession> wsl1 = workshopSessionRepository.getByStartupAcademyRoomId(startupId,
                    academyRoom.get().getRefAcademyRoom().getId(), academyRoom.get().getId(),
                    Constant.PUBLISHED.toString());
                List<GetWorkshopSessionDto> getWorkshopSessionDtos = workshopSessionMapper.toGetWorkshopSessionDtoList(wsl1);
                obj.put("workshopSessions", getWorkshopSessionDtos);
                obj.put("academyRoomDescription", academyRoom.get().getDescription());
                obj.put("academyRoomName", academyRoom.get().getName());
                obj.put("academyRoomId", academyRoom.get().getId());

            }
        });
        return ResponseWrapper.response(obj);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getAcademyRoomForStartups(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                       Long startupId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<IntakeProgram> intakeProgram = intakeProgramRepository.findById(intakeProgramId);
        Optional<Startup> startup = startupRepository.findById(startupId);
        if (startup.isPresent() && intakeProgram.isPresent()) {
            List<AcademyRoom> academyRooms = academyRoomRepository.findByStartupId(startupId);
            return ResponseWrapper.response(academyRoomMapper.toAcademyRoomManagementDtoList(academyRooms));
        }
        return ResponseWrapper.response400("invalid parameters", "bad Request");
    }

    @Override
    @Transactional
    public ResponseEntity<?> createAcademyRoom(CurrentUserObject currentUserObject,
                                               PostAcademyRoomDto academyRoomRequest) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        // check academy room name already exists or not
        if (academyRoomRepository
            .existsByIntakeProgram_IdAndName(academyRoomRequest.getIntakeProgramId(), academyRoomRequest.getName())) {
            return ResponseWrapper.response400("duplicate academy room name", "name");
        }
        Optional<IntakeProgram> intakeProgram = intakeProgramRepository
            .findById(academyRoomRequest.getIntakeProgramId());
        if (intakeProgram.isPresent()) {
            if (academyRoomRequest.getSessionEnd() > intakeProgram.get().getPeriodEnd().toInstant().toEpochMilli()) {
                return ResponseWrapper.response400("academy room end date should not exceed intake end date",
                    "sessionEnd");
            }
            AcademyRoom academyRoom = new AcademyRoom();
            academyRoom.setName(academyRoomRequest.getName());
            academyRoom.setDescription(academyRoomRequest.getDescription());
            academyRoom.setCreatedUser(user);
            academyRoom.setStatus(Constant.DRAFT.toString());
            academyRoom.setStatusPublish(Constant.NOT_PUBLISHED.toString());
            academyRoom.setIntakeProgram(intakeProgram.get());
            if (academyRoomRequest.getSessionStart() != null) {
                academyRoom.setSessionStart(new Date(academyRoomRequest.getSessionStart()));
            }
            if (academyRoomRequest.getSessionEnd() != null) {
                academyRoom.setSessionEnd(new Date(academyRoomRequest.getSessionEnd()));
            }
            academyRoomRepository.save(academyRoom);
            resourcePermissionService.createAcademyRoom(user.getId(), academyRoom.getId());
            return ResponseWrapper.response(academyRoomMapper.toAcademyRoomManagementDto(academyRoom), "academy room created");
        }
        return ResponseWrapper.response400("invalid userId", "userId");
    }


    @Override
    @Transactional
    public Object deleteAcademyRoom(CurrentUserObject currentUserObject, Long academyRoomId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getWillManagement()) {
            Optional<AcademyRoom> academyRoom = academyRoomRepository.findById(academyRoomId);
            if (academyRoom.isPresent() && academyRoom.get().getStatusPublish() != null
                && !academyRoom.get().getStatusPublish().equals(Constant.PUBLISHED.toString())) {
                // check reference academy rooms and do deletion if no reference exists
                if (!academyRoomRepository.existsByRefAcademyRoom_Id(academyRoomId) && (academyRoom.get().getRefAcademyRoom() == null)
                    && (academyRoom.get().getStartup() == null)) {
                    try {
                        List<UserResourcePermission> academyRoomPermissions = userResourcePermissionRepository
                            .findByResourceAndResourceId(ResourceUtil.mar, academyRoomId);
                        List<UserResourcePermission> workshopSessionPermissions = userResourcePermissionRepository
                            .findByResourceAndParentResourceId(ResourceUtil.mws, academyRoomId);
                        if (!academyRoomPermissions.isEmpty()) {
                            userResourcePermissionRepository.deleteAll(academyRoomPermissions);
                        }
                        if (!workshopSessionPermissions.isEmpty()) {
                            userResourcePermissionRepository.deleteAll(workshopSessionPermissions);
                        }
                        academyRoomRepository.delete(academyRoom.get());
                        return academyRoomId;
                    } catch (Exception ex) {
                        return "Academy room deletion failed";// + ex.getMessage();
                    }
                }
            }
            return "cannot delete published academy room";
        }
        return "invalid academyRoomId";
    }

    @Override
    @Transactional
    public Object publishAcademyRoom(CurrentUserObject currentUserObject, Long academyRoomId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<AcademyRoom> academyRoom = academyRoomRepository.findById(academyRoomId);
        // check which all management users can publish academy rooms
        if (user.getWillManagement() && academyRoom.isPresent()
            && (academyRoom.get().getStatusPublish() == null
            || academyRoom.get().getStatusPublish().equals(Constant.NOT_PUBLISHED.toString()))) {
            AcademyRoom academyRoomPublished = academyRoom.get();
            academyRoomPublished.setStatusPublish(Constant.PUBLISHED.toString());
            academyRoomPublished.setStatus(Constant.NEW.toString());
            academyRoomPublished = academyRoomRepository.save(academyRoomPublished);
            notificationService.acadamicRoomsPublishedNotification(user.getId(), false);
            return academyRoomPublished;
        }
        return null;
    }

    @Override
    @Transactional
    public Object getManagementAcademyRoom(CurrentUserObject currentUserObject, Long academyRoomId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<AcademyRoom> academyRoom = academyRoomRepository.findById(academyRoomId);
        if (user.getWillManagement() && academyRoom.isPresent()) {
            return academyRoomMapper.toAcademyRoomManagementDto(academyRoom.get());
        }
        if (!academyRoom.isPresent()) {
            return "academyRoomId not found";
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity<?> shareAcademyRoom(CurrentUserObject currentUserObject, Long academyRoomId,
                                              List<PutAcademyRoomShareDto> putAcademyRoomShareDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<AcademyRoom> academyRoom = academyRoomRepository.findById(academyRoomId);

        if (user.getWillManagement() && academyRoom.isPresent()
            && (academyRoom.get().getStatusPublish() == null
            || academyRoom.get().getStatusPublish().equals(Constant.NOT_PUBLISHED.toString()))) {

            AcademyRoom academyRoomShared = academyRoom.get();
            List<Long> sharedUserIds = new ArrayList<>();
            for (PutAcademyRoomShareDto memberShared : putAcademyRoomShareDto) {
                // check shared member is a management user or not
                Optional<User> userShared = userRepository.findById(
                    memberShared.getShareMemberId() != null ? memberShared.getShareMemberId() : (long) 0);
                if (userShared.isPresent() && userShared.get().getWillManagement()) {
                    sharedUserIds.add(userShared.get().getId());
                }
            }
            if (!sharedUserIds.isEmpty()) {
                resourcePermissionService.shareAcademyRoom(academyRoomShared.getId(),
                    academyRoomShared.getCreatedUser().getId(), sharedUserIds, user, user.getRole());
                return ResponseWrapper.response(null, "academy room shared");
            }
        }
        if (academyRoom.isPresent() && academyRoom.get().getStatusPublish().equals(Constant.PUBLISHED.toString())) {
            return ResponseWrapper.response400("academyRoom is published already", "academyRoomId");
        }
        return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Transactional
    @Override
    public List<Object> dropdownManagementShareMembers(CurrentUserObject currentUserObject, Long academyRoomId,
                                                       List<String> roles) {
        List<Object> list = new ArrayList<>();
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<AcademyRoom> academyRoom = academyRoomRepository.findById(academyRoomId);
        if (user.getWillManagement() && academyRoom.isPresent()) {
            List<UserResourcePermission> sharedUsers = userResourcePermissionRepository
                .findByResourceAndResourceIdAndUserIdNot(ResourceUtil.mar, academyRoomId,
                    currentUserObject.getUserId());
            userRepository.findByUserRoles(roles, Constant.REGISTERED.toString()).forEach(t -> {
                if (!Objects.equals(t.getId(), academyRoom.get().getCreatedUser().getId())) {
                    SharedMemberDto sh = new SharedMemberDto();
                    sh.setName(t.getAlias());
                    sh.setId(t.getId());
                    sh.setSharedStatus(sharedUsers.stream().anyMatch(su -> su.getUserId().equals(t.getId())));
                    if (t.getRole() != null) {
                        sh.setRoleName(t.getRole().getRoleAlias());
                    }
                    list.add(sh);
                }
            });
        }
        return list;
    }

    @Transactional
    @Override
    public List<AcademyRoomManagementDto> getManagementAcademyRoomsByAcademyRoomStatus(
        CurrentUserObject currentUserObject, String academyRoomStatus, Pageable paging, Set<Long> academyRoomIds) {
        List<AcademyRoomManagementDto> list = Collections.emptyList();
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getRole().getRoleName().equals(RoleName.ROLE_SUPER_ADMIN)) {
            if (academyRoomStatus.equals(Constant.NEW.toString())) {
                Page<AcademyRoom> academyRoomList = academyRoomRepository.superAdminAcademyroomsNew(
                    Constant.DRAFT.toString(), Constant.NEW.toString(), new Date(), paging);
                list = academyRoomMapper.toAcademyRoomManagementDtoList(academyRoomList);
            } else if (academyRoomStatus.equals(Constant.IN_PROGRESS.toString())) {
                Page<AcademyRoom> academyRoomList = academyRoomRepository.superAdminAcademyroomsInprogress(new Date(),
                    paging);
                list = academyRoomMapper.toAcademyRoomManagementDtoList(academyRoomList);
            } else if (academyRoomStatus.equals(Constant.COMPLETE.toString())) {
                Page<AcademyRoom> academyRoomList = academyRoomRepository
                    .findByStartupIsNullAndRefAcademyRoomIsNullAndSessionEndLessThan(new Date(), paging);
                list = academyRoomMapper.toAcademyRoomManagementDtoList(academyRoomList);
            }
        } else {
            if (academyRoomStatus.equals(Constant.NEW.toString())) {
                Page<AcademyRoom> academyRoomList = academyRoomRepository.superAdminAcademyroomsNewIdIn(
                    Constant.DRAFT.toString(), Constant.NEW.toString(), new Date(), academyRoomIds, paging);
                list = academyRoomMapper.toAcademyRoomManagementDtoList(academyRoomList);
            } else if (academyRoomStatus.equals(Constant.IN_PROGRESS.toString())) {
                Page<AcademyRoom> academyRoomList = academyRoomRepository
                    .superAdminAcademyroomsInprogressIdIn(new Date(), academyRoomIds, paging);
                list = academyRoomMapper.toAcademyRoomManagementDtoList(academyRoomList);
            } else if (academyRoomStatus.equals(Constant.COMPLETE.toString())) {
                Page<AcademyRoom> academyRoomList = academyRoomRepository
                    .findByIdInAndStartupIsNullAndRefAcademyRoomIsNullAndSessionEndLessThan(academyRoomIds,
                        new Date(), paging);
                list = academyRoomMapper.toAcademyRoomManagementDtoList(academyRoomList);
            }
        }
        return list;
    }

    @Transactional
    @Override
    public List<Object> dropdownManagementAcademyRoomIntakes(CurrentUserObject currentUserObject, String programName) {
        List<Object> list = new ArrayList<>();
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getWillManagement()) {
            intakeProgramRepository.findByProgramName(programName).forEach(t -> {
                if (t.getStatus().equals(Constant.PUBLISHED.toString())) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", t.getId());
                    map.put("name", t.getId());
                    list.add(map);
                }
            });
        }
        return list;
    }

    // ============================================================================================

    @Override
    @Transactional
    public List<WorkshopSessionManagementDto> getManagementAcademyRoomWorkShopSessions(
        CurrentUserObject currentUserObject, Long academyRoomId, Set<Long> workshopSessionIds, Pageable paging) {
        List<WorkshopSessionManagementDto> list = Collections.emptyList();
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getWillManagement() && academyRoomRepository.existsById(academyRoomId)) {
            Page<WorkshopSession> workshopSessionList;
            if (workshopSessionIds != null) {
                workshopSessionList = workshopSessionRepository
                    .findByIdInAndAcademyRoom_Id(workshopSessionIds, academyRoomId, paging);
            } else {
                workshopSessionList = workshopSessionRepository.getWorkshpSessions(academyRoomId,
                    paging);
            }
            list = workshopSessionMapper.toWorkshopSessionManagementDtoList(workshopSessionList);
        }
        return list;

    }

    @Override
    @Transactional
    public ResponseEntity<?> createManagementAcademyRoomWorkShopSession(CurrentUserObject currentUserObject,
                                                                        PostAcademyRoomWorkShopSessionDto academyRoomWorkShopSessionRequest, Long academyRoomId) {
        if (workshopSessionRepository.existsByNameIgnoreCaseAndAcademyRoom_Id(academyRoomWorkShopSessionRequest.getName(), academyRoomId)) {
            return ResponseWrapper.response400("name already exist", "name");
        }
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<AcademyRoom> academyRoom = academyRoomRepository.findById(academyRoomId);
        if (academyRoom.isPresent()) {
            if (academyRoomWorkShopSessionRequest.getSessionEnd() > academyRoom.get().getSessionEnd().toInstant()
                .toEpochMilli()) {
                return ResponseWrapper.response400("workshop session end date should not exceed academy room end date",
                    "sessionEnd");
            }
            WorkshopSession workshopSession = new WorkshopSession();
            workshopSession.setName(academyRoomWorkShopSessionRequest.getName());
            workshopSession.setDescription(academyRoomWorkShopSessionRequest.getDescription());
            workshopSession.setCreatedUser(user);
            workshopSession.setStatus(Constant.DRAFT.toString());
            workshopSession.setStatusPublish(Constant.NOT_PUBLISHED.toString());
            workshopSession.setAcademyRoom(academyRoom.get());
            workshopSession.setPercentageFinish(0);
            if (academyRoomWorkShopSessionRequest.getSessionStart() != null) {
                workshopSession.setSessionStart(new Date(academyRoomWorkShopSessionRequest.getSessionStart()));
            }
            if (academyRoomWorkShopSessionRequest.getSessionEnd() != null) {
                workshopSession.setSessionEnd(new Date(academyRoomWorkShopSessionRequest.getSessionEnd()));
            }
            workshopSession.setWillOnline(academyRoomWorkShopSessionRequest.getWillOnline() != null
                && !academyRoomWorkShopSessionRequest.getWillOnline().equals(false));
            workshopSession.setMeetingRoomOrLink(academyRoomWorkShopSessionRequest.getMeetingRoomOrLink());
            workshopSession = workshopSessionRepository.save(workshopSession);
            return ResponseWrapper.response(workshopSessionMapper.toWorkshopSessionManagementDto(workshopSession));
        }
        return ResponseWrapper.response400("invalid academyRoomId", "academyRoomId");
    }

    @Override
    @Transactional
    public Object getManagementAcademyRoomWorkShopSession(CurrentUserObject currentUserObject, Long academyRoomId,
                                                          Long workshopSessionId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getWillManagement()) {
            WorkshopSession workshopSession = workshopSessionRepository
                .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
            if (workshopSession == null) {
                return "academyRoomId and workshopSessionId combination not found";
            }
            Map<String, Object> obj = new HashMap<>();
            obj.put("academyRoomId", academyRoomId);
            obj.put("workshopSession", workshopSessionMapper.toWorkshopSessionManagementDto(workshopSession));
            return obj;
        }
        return null;
    }

    @Override
    @Transactional
    public List<WorkshopSessionSubmissionManagementDto> getManagementWorkShopSessionsAllSubmissions(
        CurrentUserObject currentUserObject, Long academyRoomId, Long workshopSessionId, Pageable pageable,
        String filterBy, String searchBy) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<AcademyRoom> academyRoom = academyRoomRepository.findById(academyRoomId);
        List<WorkshopSessionSubmissionManagementDto> list = Collections.emptyList();
        if (user.getWillManagement() && academyRoom.isPresent()) {
            Page<WorkshopSessionSubmissions> workshopSessionSubmissionsList = workshopSessionSubmissionsRepository
                .findAll(WorkShopSpecification.getWorkshopSessionsSubmisionLogs(user, workshopSessionId,
                    filterBy, searchBy, academyRoomId), pageable);
            list = workshopSessionSubmissionsMapper.toWorkshopSessionSubmissionManagementDtoList(workshopSessionSubmissionsList);
        }
        return list;
    }

    @Override
    @Transactional
    public Object deleteManagementAcademyRoomWorkShopSession(CurrentUserObject currentUserObject, Long academyRoomId,
                                                             Long workshopSessionId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<AcademyRoom> academyRoom = academyRoomRepository.findById(academyRoomId);
        if (user.getWillManagement() && academyRoom.isPresent()) {
            WorkshopSession workshopSession = workshopSessionRepository
                .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
            if (workshopSession != null && workshopSession.getStatusPublish() != null
                && !workshopSession.getStatusPublish().equals(Constant.PUBLISHED.toString())) {
                // check reference workshop session and do deletion if no reference exists
                List<WorkshopSession> refWorkshopSessions = workshopSessionRepository
                    .findByRefWorkshopSession_Id(workshopSessionId);
                if ((refWorkshopSessions.isEmpty()) && (workshopSession.getRefWorkshopSession() == null)
                    && (workshopSession.getStartup() == null)) {
                    try {

                        // delete entries from training material
                        // delete entries from assignment
                        // delete entries from survey
                        // delete entries from feedback
                        // delete entries from userresourcepermissions
                        // delete entries from workshopsession
                        List<UserResourcePermission> workshopSessionPermissions = userResourcePermissionRepository
                            .findByResourceAndResourceId(ResourceUtil.mws, workshopSessionId);
                        if (!workshopSessionPermissions.isEmpty()) {
                            List<Long> sharedUserIds = workshopSessionPermissions.stream()
                                .map(UserResourcePermission::getUserId).collect(Collectors.toList());
                            List<UserResourcePermission> academyRoomPartialPermissions = userResourcePermissionRepository
                                .getPartialPermissions(ResourceUtil.mar, academyRoomId, PermissionUtil.all,
                                    sharedUserIds);
                            if (!academyRoomPartialPermissions.isEmpty()) {
                                List<UserResourcePermission> removePartialPermissions = new ArrayList<UserResourcePermission>();
                                // check whether user has any other workshop session permission before removing
                                // academy room partial permission
                                List<UserResourcePermission> otherWorshopSessionPermissions = userResourcePermissionRepository
                                    .findByResourceAndParentResourceIdAndResourceIdNotAndUserIdIn(ResourceUtil.mws,
                                        academyRoomId, workshopSessionId, sharedUserIds);

                                removePartialPermissions = academyRoomPartialPermissions.stream()
                                    .filter(e -> otherWorshopSessionPermissions.stream()
                                        .map(UserResourcePermission::getUserId)
                                        .noneMatch(uid -> uid.equals(e.getUserId())))
                                    .collect(Collectors.toList());
                                if (!removePartialPermissions.isEmpty()) {
                                    userResourcePermissionRepository.deleteAll(removePartialPermissions);
                                }
                            }
                            userResourcePermissionRepository.deleteAll(workshopSessionPermissions);
                        }
                        workshopSessionRepository.delete(workshopSession);
                        return workshopSessionId;
                    } catch (Exception ex) {
                        return "Workshop session deletion failed";// + ex.getMessage();
                    }
                }
            }
            return "cannot delete published workshopSession";
        }
        return "invalid workshopSessionId";
    }

    @Override
    @Transactional
    public ResponseEntity<?> publishManagementAcademyRoomWorkShopSession(CurrentUserObject currentUserObject,
                                                                         Long academyRoomId, Long workshopSessionId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository.findByIdAndStartupIdIsNull(workshopSessionId);
        if (workshopSession != null) {
            workshopSession.setStatus(Constant.NEW.toString());
            workshopSession.setStatusPublish(Constant.PUBLISHED.toString());
            workshopSession = workshopSessionRepository.save(workshopSession);
            TrainingSession ts = getTrainingSession(workshopSession);
            ts = trainingSessionRepository.save(ts);
            CalendarEvent calendarEvent = new CalendarEvent();
            calendarEvent.setSessionStart(workshopSession.getSessionStart());
            calendarEvent.setSessionEnd(workshopSession.getSessionEnd());
            calendarEvent.setIsRecurring(true);
            calendarEvent.setSessionStartTime("00.00");
            calendarEvent.setSessionEndTime("23.59");
            calendarEvent.setTrainingSession(ts);
            calendarEventRepository.save(calendarEvent);
            notificationService.acadamicRoomsPublishedNotification(user.getId(), true);
            return ResponseWrapper.response(null);
        }
        return ResponseWrapper.response400("Invalid WorkshopSessionId", "workshopSessionId");
    }

    @NotNull
    private static TrainingSession getTrainingSession(WorkshopSession workshopSession) {
        TrainingSession ts = new TrainingSession();
        ts.setIntakeProgram(workshopSession.getAcademyRoom().getIntakeProgram());
        ts.setDescription(workshopSession.getDescription());
        ts.setMeetingName(workshopSession.getName());
        ts.setMeetingRoomOrLink(workshopSession.getMeetingRoomOrLink());
        ts.setSessionStart(workshopSession.getSessionStart());
        ts.setSessionEnd(workshopSession.getSessionEnd());
        ts.setWorkshopSession(workshopSession);
        ts.setCreatedUser(workshopSession.getCreatedUser());
        ts.setSessionStartTime("00.00");
        ts.setSessionEndTime("23.59");
        ts.setIsRecurring(true);
        ts.setWillOnline(workshopSession.getWillOnline());
        return ts;
    }

    @Override
    @Transactional
    public ResponseEntity<?> shareAcademyRoomWorkShopSession(CurrentUserObject currentUserObject, Long academyRoomId,
                                                             Long workshopSessionId, List<PutAcademyRoomShareDto> putAcademyRoomShareDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
        if (workshopSession == null) {
            return ResponseWrapper.response400("Invalid WorkshopSession", "workshopSessionId");
        }
        if (workshopSession.getStatusPublish().equals(Constant.PUBLISHED.toString())) {
            return ResponseWrapper.response400("Published WorkshopSession Cannot Share", "workshopSessionId");
        }
        List<Long> sharedUserIds = new ArrayList<>();
        for (PutAcademyRoomShareDto memberShared : putAcademyRoomShareDto) {
            Optional<User> userShared = userRepository.findById(
                memberShared.getShareMemberId() != null ? memberShared.getShareMemberId() : (long) 0);
            if (userShared.isPresent() && userShared.get().getWillManagement()) {
                sharedUserIds.add(userShared.get().getId());
            }
        }
        if (!sharedUserIds.isEmpty()) {
            resourcePermissionService.shareWorkshopSession(academyRoomId, workshopSessionId, user.getId(),
                sharedUserIds);
        }
        return ResponseWrapper.response(null, "Workshopsession Shared");
    }

    @Override
    @Transactional
    public List<Object> dropdownWorkshopSessionShareMembers(CurrentUserObject currentUserObject, Long academyRoomId,
                                                            Long workshopSessionId, List<String> roles) {
        List<Object> list = new ArrayList<>();
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
        if (user.getWillManagement() && workshopSession != null) {
            List<UserResourcePermission> asharedUsers = userResourcePermissionRepository
                .findByResourceAndResourceIdAndUserIdNot(ResourceUtil.mar, academyRoomId,
                    currentUserObject.getUserId());
            List<UserResourcePermission> wsharedUsers = userResourcePermissionRepository
                .findByResourceAndResourceIdAndUserIdNot(ResourceUtil.mws, academyRoomId,
                    currentUserObject.getUserId());
            userRepository.findByUserRoles(roles, Constant.REGISTERED.toString()).forEach(t -> {
                if (!Objects.equals(t.getId(), workshopSession.getCreatedUser().getId())) {
                    SharedMemberDto sh = new SharedMemberDto();
                    sh.setName(t.getAlias());
                    sh.setId(t.getId());
                    sh.setSharedStatus(
                        asharedUsers.stream().anyMatch(su -> su.getUserId().equals(t.getId()))
                            || wsharedUsers.stream().anyMatch(su -> su.getUserId().equals(t.getId())));
                    if (t.getRole() != null) {
                        sh.setRoleName(t.getRole().getRoleAlias());
                    }
                    list.add(sh);
                }
            });
        }
        return list;
    }

    @Override
    @Transactional
    public List<TrainingMaterialManagementDto> getManagementWorkShopSessionTrainingMaterials(
        CurrentUserObject currentUserObject, Long academyRoomId, Long workshopSessionId, Pageable paging,
        String filterBy, String searchBy) {
        List<TrainingMaterialManagementDto> list = Collections.emptyList();
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
        if (workshopSession != null && user.getWillManagement()) {
            Page<TrainingMaterial> trainingMaterialList = trainingMaterialRepository.findAll(WorkShopSpecification
                    .getWorkshopSessionsTrainings(user, workshopSessionId, filterBy, searchBy, academyRoomId),
                paging);
            list = trainingMaterialMapper.toTrainingMaterialManagementDtoList(trainingMaterialList);
        }
        return list;
    }

    @Override
    @Transactional
    public Object createManagementWorkShopSessionTrainingMaterials(CurrentUserObject currentUserObject,
                                                                   PostTrainingMaterialDto trainingMaterialRequest, Long academyRoomId, Long workshopSessionId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
        Optional<AcademyRoom> ar = academyRoomRepository.findById(academyRoomId);
        if (workshopSession != null && user.getWillManagement() && ar.isPresent()
            && ar.get().getIntakeProgram() != null) {
            // save file
            TrainingMaterial trainingMaterial = new TrainingMaterial();
            String filePath = fileAdapter.saveTrainingFile(ar.get().getIntakeProgram().getId(), academyRoomId,
                workshopSessionId, trainingMaterialRequest.getTrainingfile());
            if (Objects.nonNull(filePath) && !filePath.isEmpty()) {
                trainingMaterial.setName(trainingMaterialRequest.getName());
                if (trainingMaterialRequest.getDescription() != null) {
                    trainingMaterial.setDescription(trainingMaterialRequest.getDescription());
                }
                trainingMaterial.setCreatedUser(user);
                trainingMaterial.setWorkshopSession(workshopSession);
                trainingMaterial.setStatus(Constant.SUBMITTED.toString());
                trainingMaterial.setPath(filePath);
                trainingMaterialRepository.save(trainingMaterial);
            }
            return trainingMaterial;
        }
        return null;
    }

    @Override
    @Transactional
    public Object publishManagementWorkShopSessionTrainingMaterials(CurrentUserObject currentUserObject,
                                                                    Long academyRoomId, Long workshopSessionId, Long trainingMaterialId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<TrainingMaterial> trainingMaterial = trainingMaterialRepository.findById(trainingMaterialId);
        if (trainingMaterial.isPresent() && user.getWillManagement()
            && Objects.equals(trainingMaterial.get().getWorkshopSession().getId(), workshopSessionId)
            && Objects.equals(trainingMaterial.get().getWorkshopSession().getAcademyRoom().getId(), academyRoomId)
            && trainingMaterial.get().getWorkshopSession().getStartup() == null) {
            trainingMaterial.get().setStatus(Constant.PUBLISHED.toString());
            trainingMaterialRepository.save(trainingMaterial.get());
            notificationService.trainingMaterialsUploadNotification(user);
            return trainingMaterial.get();
        }
        return null;
    }

    // management- WorkShop sessions -assignments
    @Override
    @Transactional
    public Object getManagementWorkShopSessionAssignments(CurrentUserObject currentUserObject, Long academyRoomId,
                                                          Long workshopSessionId, Pageable paging, String filterBy, String searchBy) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
        if (user.getWillManagement() && workshopSession != null) {
            List<AssignmentManagementDto> assignments = assignmentRepositoryCustom.getAssignments(user,
                academyRoomId, workshopSessionId, paging, filterBy, searchBy);
            if (assignments.size() > 0) {
                Map<String, Object> obj = new HashMap<>();
                obj.put("startupCount", startupRepository.count());
                obj.put("assignments", assignments);
                return obj;
            }
        }
        return null;
    }

    @Override
    @Transactional
    public AssignmentManagementDto getManagementWorkShopSessionAssignment(CurrentUserObject currentUserObject,
                                                                          Long academyRoomId, Long workshopSessionId, Long assignmentId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
        if (user.getWillManagement() && workshopSession != null) {
            Assignment assignment = assignmentRepository
                .findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNull(
                    assignmentId, workshopSessionId, academyRoomId);
            if (assignment != null) {
                Long submittedCount = assignmentRepository
                    .countByRefAssignment_IdAndRefAssignment_WorkshopSession_IdAndRefAssignment_WorkshopSession_AcademyRoom_Id(
                        assignmentId, workshopSessionId, academyRoomId);
                // assignment related files
                List<AssignmentFile> ls1 = assignmentFileRepository.findByAssignment_Id(assignmentId);
                List<GetAssignmentFileDto> ls = assignmentFileMapper.toGetAssignmentFileDtoList(ls1);
                return assignmentMapper.toAssignmentManagementDtoWithSubmitCount(assignment, submittedCount, ls);
            }
        }
        return null;
    }

    @Override
    @Transactional
    public List<AssignmentManagementDto> getWorkShopSessionAssignmentsSubmitted(CurrentUserObject currentUserObject,
                                                                                Long academyRoomId, Long workshopSessionId, Long assignmentId, Pageable paging, String filterBy,
                                                                                String searchBy) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        List<AssignmentManagementDto> list = Collections.emptyList();
        if (user.getWillManagement()) {
            Page<Assignment> submittedAssignmentList = assignmentRepository
                .findAll(WorkShopSpecification.getWorkshopSessionsAssignmentsSubmitted(user, assignmentId,
                    workshopSessionId, filterBy, searchBy, academyRoomId), paging);
            list = assignmentMapper.toAssignmentManagementDtoList(submittedAssignmentList);
        }
        return list;
    }

    @Override
    @Transactional
    public ResponseEntity<?> createManagementWorkShopSessionAssignment(CurrentUserObject currentUserObject,
                                                                       PostAssignmentManagementDto workShopSessionAssignmentRequest, Long academyRoomId, Long workshopSessionId,
                                                                       MultipartFile[] files) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
        Optional<AcademyRoom> ar = academyRoomRepository.findById(academyRoomId);
        if (user.getWillManagement() && ar.isPresent()) {
            if (workShopSessionAssignmentRequest.getDueDate() > workshopSession.getSessionEnd().toInstant()
                .toEpochMilli()) {
                return ResponseWrapper.response400("assignment due date should not exceed workshop session end date",
                    "sessionEnd");
            }
            Assignment assignment = new Assignment();
            assignment.setCreatedUser(user);
            assignment.setDueDate(new Date(workShopSessionAssignmentRequest.getDueDate()));
            assignment.setStatus(Constant.DRAFT.toString());
            assignment.setSubmittedUser(user);
            assignment.setName(workShopSessionAssignmentRequest.getName());
            assignment.setWorkshopSession(workshopSession);
            assignment.setCreatedUser(user);
            assignment.setSubmitDate(new Date());
            assignment = assignmentRepository.save(assignment);
            List<AssignmentFile> assignmentFileList = new ArrayList<>();
            // save files
            for (MultipartFile multipartFile : files) {
                AssignmentFile assignmentFile = new AssignmentFile();
                String filePath = fileAdapter.saveAssignmentFile(ar.get().getIntakeProgram().getId(), academyRoomId,
                    workshopSessionId, assignment.getId(), multipartFile);
                if (Objects.nonNull(filePath) && !filePath.isEmpty()) {
                    assignmentFile.setPath(filePath);
                    assignmentFile.setName(multipartFile.getOriginalFilename());
                    assignmentFile.setWillManagement(true);
                    assignmentFile.setAssignment(assignment);
                    assignmentFileList.add(assignmentFile);
                }
            }
            if (!assignmentFileList.isEmpty()) {
                assignmentFileRepository.saveAll(assignmentFileList);
            }
            return ResponseWrapper.response(null, "assignments created");
        }
        return ResponseWrapper.response400("invalid userId", "userId");
    }

    @Override
    @Transactional
    public Object reviewManagementWorkShopSessionAssignment(CurrentUserObject currentUserObject,
                                                            PutAssignmentManagementDto workShopSessionAssignmentRequest, Long academyRoomId, Long workshopSessionId,
                                                            Long assignmentId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Assignment assignment = assignmentRepository
            .findByIdAndRefAssignment_WorkshopSession_IdAndRefAssignment_WorkshopSession_AcademyRoom_Id(
                assignmentId, workshopSessionId, academyRoomId).orElse(null);
        if (Objects.isNull(assignment))
            return null;
        boolean reviewerFlag = false;
        if (user.getWillManagement() && assignment.getStatus().equals(Constant.SUBMITTED.toString())
            && assignment.getSubmittedStartup() != null
            && (workShopSessionAssignmentRequest.getStatus().equals(Constant.APPROVED.toString())
            || workShopSessionAssignmentRequest.getStatus().equals(Constant.RESUBMIT.toString()))) {
            // already 2 reviewers approved cannot be reviewed again
            if (!assignment.getReview2Status().equals(Constant.APPROVED.toString())) {
                // 1st reviewer should be coach or trainer
                if (!assignment.getReview1Status().equals(Constant.APPROVED.toString())
                    && !user.getRole().getRoleName().equals(RoleName.ROLE_COACHES_AND_TRAINERS)) {
                    return "Coach or trainer should be the first approver";
                }
                if (!assignment.getReview1Status().equals(Constant.APPROVED.toString())
                    && user.getRole().getRoleName().equals(RoleName.ROLE_COACHES_AND_TRAINERS)) {
                    assignment.setReviwed1On(new Date());
                    assignment.setReview1User(user);
                    assignment.setReview1Status(workShopSessionAssignmentRequest.getStatus());
                    assignment.setReview1Comment(workShopSessionAssignmentRequest.getComments());
                    reviewerFlag = true;
                }
                // same reviewer cannot review twice
                // 2nd reviewer should be management user
                if (assignment.getReview1Status().equals(Constant.APPROVED.toString()) && !reviewerFlag
                    && user.getRole().getRoleName().equals(RoleName.ROLE_COACHES_AND_TRAINERS)) {
                    return "Coach or trainer cannot be the second approver";
                }
                if (assignment.getReview1Status().equals(Constant.APPROVED.toString()) && !reviewerFlag
                    // && !assignment.get().getReview2Status().equals(Constant.APPROVED.toString())
                    && !Objects.equals(assignment.getReview1User().getId(), user.getId())
                    && !user.getRole().getRoleName().equals(RoleName.ROLE_COACHES_AND_TRAINERS)) {
                    assignment.setReviwed2On(new Date());
                    assignment.setReview2User(user);
                    assignment.setReview2Status(workShopSessionAssignmentRequest.getStatus());
                    assignment.setReview2Comment(workShopSessionAssignmentRequest.getComments());
                    reviewerFlag = true;
                }
                if (reviewerFlag) {
                    Optional<WorkshopSessionSubmissions> wss = workshopSessionSubmissionsRepository
                        .findByFileTypeAndMetaDataIdAndWorkshopSession_Id(Constant.ASSIGNMENT.toString(),
                            assignment.getId(), assignment.getWorkshopSession().getId());
                    // check entry in workshop submission log
                    if (wss.isPresent()) {
                        if (workShopSessionAssignmentRequest.getStatus().equals(Constant.RESUBMIT.toString())) {
                            // re-submit due date
                            assignment.setDueDate(new Date(workShopSessionAssignmentRequest.getDueDate()));
                            wss.get().setStatus(Constant.RESUBMIT.toString());
                            assignment.setStatus(Constant.RESUBMIT.toString());
                        }
                        if (assignment.getReview1Status().equals(Constant.APPROVED.toString())
                            && assignment.getReview2Status().equals(Constant.APPROVED.toString())) {
                            wss.get().setStatus(Constant.APPROVED.toString());
                            assignment.setStatus(Constant.APPROVED.toString());
                        }
                        assignmentRepository.save(assignment);
                        workshopSessionSubmissionsRepository.save(wss.get());
                        notificationService.assignmentReviewNotification(user, wss.get().getStartup(),
                            workShopSessionAssignmentRequest.getStatus());
                        return assignment;
                    }
                }
            } else {
                return "already approved by both approvers";
            }
        }
        return null;
    }

    @Override
    @Transactional
    public Object getManagementWorkShopSessionSubmittedAssignment(CurrentUserObject currentUserObject,
                                                                  Long academyRoomId, Long workshopSessionId, Long submittedAssignmentId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Map<String, Object> obj = null;
        if (user.getWillManagement()) {
            Assignment submittedAssignment = assignmentRepository
                .findByIdAndRefAssignment_WorkshopSession_IdAndRefAssignment_WorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNotNull(
                    submittedAssignmentId, workshopSessionId, academyRoomId);
            if (submittedAssignment != null) {
                Assignment assignment = assignmentRepository
                    .findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNull(
                        submittedAssignment.getRefAssignment().getId(), workshopSessionId, academyRoomId);
                if (assignment != null) {
                    Long submittedCount = assignmentRepository
                        .countByRefAssignment_IdAndRefAssignment_WorkshopSession_IdAndRefAssignment_WorkshopSession_AcademyRoom_Id(
                            submittedAssignment.getRefAssignment().getId(), workshopSessionId, academyRoomId);
                    // assignment related files
                    List<AssignmentFile> assignmentFiles = assignmentFileRepository
                        .findByAssignment_Id(assignment.getId());
                    List<GetAssignmentFileDto> assignmentFilesDto = assignmentFileMapper.toGetAssignmentFileDtoList(assignmentFiles);
                    // submitted assignment related files
                    List<AssignmentFile> submittedFiles = assignmentFileRepository
                        .findByAssignment_Id(submittedAssignment.getId());
                    List<GetAssignmentFileDto> submittedFilesDto = assignmentFileMapper.toGetAssignmentFileDtoList(submittedFiles);
                    obj = new HashMap<>();
                    obj.put("assignment", assignmentMapper.toAssignmentManagementDtoWithSubmitCount(assignment, submittedCount,
                        assignmentFilesDto));
                    obj.put("submittedAssignment",
                        assignmentMapper.assignmentAndFileToAssignmentManagementDto(submittedAssignment, submittedFilesDto));
                }
            }
        }
        return obj;
    }

    @Transactional
    @Override
    public Object publishManagementWorkShopSessionAssignment(CurrentUserObject currentUserObject, Long academyRoomId,
                                                             Long workshopSessionId, Long assignmentId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Assignment assignment = assignmentRepository
            .findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNull(assignmentId,
                workshopSessionId, academyRoomId);
        if (assignment != null
            && assignment.getStatus().equals(Constant.DRAFT.toString())) {
            assignment.setStatus(Constant.PUBLISHED.toString());
            assignmentRepository.save(assignment);
            notificationService.assignmentUploadNotification(user);
            return assignment.getId();
        }
        return null;
    }

    @Override
    @Transactional
    public Object deleteManagementWorkShopSessionAssignment(CurrentUserObject currentUserObject, Long academyRoomId,
                                                            Long workshopSessionId, Long assignmentId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Assignment assignment = assignmentRepository
            .findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNull(assignmentId,
                workshopSessionId, academyRoomId);
        if (assignment != null && !assignment.getStatus().equals(Constant.PUBLISHED.toString())) {
            assignmentFileRepository.deleteByAssignment(assignmentId);
            assignmentRepository.removeById(assignmentId);
            return assignment.getId();
        }
        return "cannot delete published assignment";
    }

    // management- WorkShop sessions -feedbackforms

    @Override
    @Transactional
    public Object getManagementWorkShopSessionFeedbackForms(CurrentUserObject currentUserObject, Long academyRoomId,
                                                            Long workshopSessionId, Pageable paging, String filterBy, String searchBy) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
        if (user.getWillManagement() && workshopSession != null) {
            List<FeedbackFormManagementDto> feedbacks = feedbackRepositoryCustom.getFeedbacks(user, academyRoomId,
                workshopSessionId, paging, filterBy, searchBy);
            if (!feedbacks.isEmpty()) {
                Map<String, Object> obj = new HashMap<>();
                obj.put("startupCount", startupRepository.count());
                obj.put("feedbacks", feedbacks);
                return obj;
            }
        }
        return null;

    }

    @Override
    @Transactional
    public Object createManagementWorkShopSessionFeedbackFormTemplates(CurrentUserObject currentUserObject,
                                                                       PostFeedbackFormTemplateManagementDto feedbackTemplateRequest, Long workshopSessionId, Long academyRoomId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
        Optional<Feedback> existingFeedbackTemplate = feedbackRepository
            .findById(feedbackTemplateRequest.getFormTemplateId() != null && !feedbackTemplateRequest.getIsNew()
                ? feedbackTemplateRequest.getFormTemplateId()
                : (long) 0);
        if (workshopSession != null && user.getWillManagement()) {
            Feedback feedback = new Feedback();
            if (!feedbackTemplateRequest.getIsNew() && existingFeedbackTemplate.isPresent()) {
                feedback.setRefFormTemplate(existingFeedbackTemplate.get());
            }
            feedback.setJsonForm(feedbackTemplateRequest.getJsonForm());
            feedback.setName(feedbackTemplateRequest.getName());
            feedback.setWorkshopSession(workshopSession);
            feedback.setStatus(Constant.DRAFT.toString());
            feedback.setCreatedUser(user);
            feedback = feedbackRepository.save(feedback);
            return feedback;
        }
        return null;
    }

    @Override
    @Transactional
    public Object publishManagementWorkShopSessionFeedbackFormTemplates(CurrentUserObject currentUserObject,
                                                                        Long workshopSessionId, Long academyRoomId, Long feedbackId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Feedback feedback = feedbackRepository
            .findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndForStartupIsNull(feedbackId,
                workshopSessionId, academyRoomId);
        if (feedback != null && user.getWillManagement()
            && !feedback.getStatus().equals(Constant.PUBLISHED.toString())) {
            feedback.setStatus(Constant.PUBLISHED.toString());
            feedbackRepository.save(feedback);
            return feedback;
        }
        return null;
    }

    @Override
    @Transactional
    public Object deleteManagementWorkShopSessionFeedbackFormTemplates(CurrentUserObject currentUserObject,
                                                                       Long workshopSessionId, Long academyRoomId, Long feedbackId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Feedback feedback = feedbackRepository
            .findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndForStartupIsNull(feedbackId,
                workshopSessionId, academyRoomId);
        if (feedback != null && user.getWillManagement()
            && !feedback.getStatus().equals(Constant.PUBLISHED.toString())) {
            feedbackRepository.delete(feedback);
            return feedbackId;
        }
        return null;
    }

    @Override
    @Transactional
    public FeedbackFormManagementDto getManagementWorkShopSessionFeedback(CurrentUserObject currentUserObject,
                                                                          Long academyRoomId, Long workshopSessionId, Long feedbackId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
        if (user.getWillManagement() && workshopSession != null) {
            Feedback feedback = feedbackRepository
                .findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndForStartupIsNull(feedbackId,
                    workshopSessionId, academyRoomId);
            if (feedback != null) {
                Long submittedCount = feedbackRepository
                    .countByRefFeedback_IdAndRefFeedback_WorkshopSession_IdAndRefFeedback_WorkshopSession_AcademyRoom_Id(
                        feedbackId, workshopSessionId, academyRoomId);
                return feedbackMapper.toFeedbackFormManagementDtoWithSubmitCount(feedback, submittedCount);
            }
        }
        return null;
    }

    @Override
    @Transactional
    public List<StartupFeedbackFormsDto> getWorkShopSessionFeedbacksSubmitted(CurrentUserObject currentUserObject,
                                                                              Long academyRoomId, Long workshopSessionId, Long feedbackId, Pageable paging, String filterBy,
                                                                              String filterKeyword, String sortData) {

        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<AcademyRoom> ar = academyRoomRepository.findById(academyRoomId);
        Optional<Feedback> f = feedbackRepository.findById(feedbackId);
        List<StartupFeedbackFormsDto> list = new ArrayList<>();
        if (user.getWillManagement() && ar.isPresent() && f.isPresent()) {
            List<Startup> startups;
            List<Feedback> feedbacks;
            if (!filterKeyword.isEmpty()) {
                if (filterBy.equals("Name")) {
                    startups = startupRepository.findByStartupNameContainingIgnoreCaseAndIntakeProgram_IdAndIsRealTrue(
                        filterKeyword, ar.get().getIntakeProgram().getId());
                    feedbacks = feedbackRepository.getPRByKeyword(user.getId(), filterKeyword, feedbackId);
                } else {
                    startups = startupRepository
                        .findByIntakeProgram_IdAndIsRealTrue(ar.get().getIntakeProgram().getId());
                    feedbacks = feedbackRepository.findBySubmittedUser_IdAndRefFeedback_Id(user.getId(),
                        feedbackId);
                }
            } else {
                startups = startupRepository.findByIntakeProgram_IdAndIsRealTrue(ar.get().getIntakeProgram().getId());
                feedbacks = feedbackRepository.findBySubmittedUser_IdAndRefFeedback_Id(user.getId(), feedbackId);
            }
            for (Startup s : startups) {
                StartupFeedbackFormsDto sfd = new StartupFeedbackFormsDto();
                sfd.setFeedbackId(feedbackId);
                sfd.setFeedbackName(f.get().getName());
                sfd.setJsonForm(f.get().getJsonForm());
                sfd.setStartupId(s.getId());
                sfd.setStartupMembers(Collections.emptyList());
                sfd.setStartupName(s.getStartupName());
                sfd.setIntakeProgramId(s.getIntakeProgram().getId());
                sfd.setStartupProfileInfoJson(s.getProfileInfoJson());
                List<Feedback> result = feedbacks.stream()
                    .filter(item -> item.getForStartup().getId().equals(s.getId())).collect(Collectors.toList());
                if (result.size() > 0) {
                    sfd.setStatus(Constant.SUBMITTED.toString());
                    sfd.setSubmittedOn(result.get(0).getSubmitDate().toInstant().toEpochMilli());
                    sfd.setJsonForm(result.get(0).getJsonForm());
                } else {
                    sfd.setStatus(Constant.NOT_SUBMITTED.toString());
                    sfd.setSubmittedOn(null);
                }
                list.add(sfd);
            }
            if (sortData.equalsIgnoreCase("nameasc")) {
                list.sort(Comparator.comparing(StartupFeedbackFormsDto::getStartupName));
            } else if (sortData.equalsIgnoreCase("namedesc")) {
                list.sort(Comparator.comparing(StartupFeedbackFormsDto::getStartupName).reversed());
            } else if (sortData.equalsIgnoreCase("submitDateasc")) {
                list.sort(Comparator.comparing(StartupFeedbackFormsDto::getSubmittedOn,
                    Comparator.nullsFirst(Comparator.naturalOrder())));
            } else if (sortData.equalsIgnoreCase("submitDatedesc")) {
                list.sort(Comparator.comparing(StartupFeedbackFormsDto::getSubmittedOn,
                    Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
            }
        }
        return list;
    }

    // ==========================================================================================

    @Override
    @Transactional
    public List<GetAssignmentFileDto> getWorksopSessionAssignmentDownloadableMaterials(
        CurrentUserObject currentUserObject, Long workshopSessionId, Long assignmentId) {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public Map<String, Object> putStartupAcademyRoom(CurrentUserObject currentUserObject, Long academyRoomId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            Map<String, Object> finalObj = new HashMap<>();
            academyRoomRepository.findByIdAndStatusPublish(academyRoomId,
                Constant.PUBLISHED.toString()).ifPresent(academyRoom -> {
                if (academyRoom.getStatus().equals(Constant.NEW.toString())) {
                    academyRoomRepository
                        .findByStartup_IdAndRefAcademyRoom_Id(user.getStartup().getId(), academyRoomId).ifPresent(academyRoom1 -> {
                            AcademyRoom ar = new AcademyRoom();
                            ar.setDescription(academyRoom.getDescription());
                            ar.setName(academyRoom.getName());
                            ar.setStatus(Constant.IN_PROGRESS.toString());
                            ar.setStartup(user.getStartup());
                            ar.setRefAcademyRoom(academyRoom);
                            ar.setSessionEnd(academyRoom.getSessionEnd());
                            ar.setSessionStart(academyRoom.getSessionStart());
                            academyRoomRepository.save(ar);
                            finalObj.put("academyRoomId", ar.getId());
                            finalObj.put("academyRoomDescription", ar.getDescription());
                            finalObj.put("academyRoomName", ar.getName());
                        });
                }
            });
            return finalObj;
        }
        return null;
    }

    @Transactional
    @Override
    public Object putStartupAcademyRoomWorksopSession(CurrentUserObject currentUserObject, Long academyRoomId,
                                                      Long workshopSessionId) {
        Map<String, Object> obj = null;
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            Optional<AcademyRoom> academyRoomValidity = academyRoomRepository.findByIdAndStartup_Id(academyRoomId,
                user.getStartup().getId());
            if (academyRoomValidity.isPresent()) {
                WorkshopSession workshopSessionValidity = workshopSessionRepository
                    .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId,
                        academyRoomValidity.get().getRefAcademyRoom().getId());
                if (workshopSessionValidity == null) {
                    return "academyRoomId and workshopSessionId combination not found";
                } else {
                    List<WorkshopSession> entry = workshopSessionRepository
                        .findByStartup_IdAndAcademyRoom_IdAndRefWorkshopSession_Id(user.getStartup().getId(),
                            academyRoomId, workshopSessionId);

                    if (entry.size() == 0) {
                        WorkshopSession ws = new WorkshopSession();

                        ws.setAcademyRoom(academyRoomValidity.get());
                        ws.setStartup(user.getStartup());
                        ws.setRefWorkshopSession(workshopSessionValidity);
                        ws.setDescription(workshopSessionValidity.getDescription());
                        ws.setName(workshopSessionValidity.getName());
                        ws.setPercentageFinish(5);
                        ws.setSessionEnd(workshopSessionValidity.getSessionEnd());
                        ws.setSessionStart(workshopSessionValidity.getSessionStart());

                        workshopSessionRepository.save(ws);

                        obj = new HashMap<>();

                        obj.put("startupId", user.getStartup().getId());
                        obj.put("academyRoomId", academyRoomId);
                        obj.put("workshopSessionId", ws.getId());
                        obj.put("workshopSessionName", ws.getName());
                        obj.put("workshopSessionDescription", ws.getDescription());

                    }
                }
            }

        }

        return obj;
    }

    @Transactional
    @Override
    public Object putWorksopSessionAssignments(CurrentUserObject currentUserObject, Long workshopSessionId,
                                               Long assignmentId, MultipartFile[] documents) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            Assignment assignment = assignmentRepository.findByIdAndWorkshopSession_IdAndSubmittedStartup_Id(
                assignmentId, workshopSessionId, user.getStartup().getId());
            if (assignment != null) {
                for (MultipartFile multipartFile : documents) {
                    putWorksopSessionAssignmentsTrans(user, assignment, multipartFile);
                }
                assignment.setStatus(Constant.SUBMITTED.toString());
                assignment.setSubmitDate(new Date());
                assignmentRepository.save(assignment);
                // create workshop submission logs
                // check already log exists
                Optional<WorkshopSessionSubmissions> submissions = workshopSessionSubmissionsRepository
                    .findByFileTypeAndMetaDataIdAndWorkshopSession_Id(Constant.ASSIGNMENT.toString(),
                        assignment.getId(), assignment.getWorkshopSession().getId());
                WorkshopSessionSubmissions wss = new WorkshopSessionSubmissions();
                if (submissions.isPresent()) {
                    wss = submissions.get();
                }

                wss.setCreatedUser(assignment.getCreatedUser());
                wss.setFileType(Constant.ASSIGNMENT.toString());
                wss.setMetaDataId(assignment.getId());
                wss.setCreatedOn(assignment.getCreatedOn());
                wss.setSubmittedUser(assignment.getSubmittedUser());
                wss.setStartup(assignment.getSubmittedStartup());
                wss.setStatus(Constant.SUBMITTED.toString());
                wss.setSubmittedOn(assignment.getSubmitDate());
                wss.setSubmittedFileName(assignment.getName());
                wss.setWorkshopSession(assignment.getWorkshopSession());
                wss.setIsLate(assignment.getSubmitDate().compareTo(assignment.getDueDate()) > 0);
                wss.setMetaDataParentId(assignment.getRefAssignment().getId());
                workshopSessionSubmissionsRepository.save(wss);
                notificationService.assignmentSubmitNotification(user, true, wss.getIsLate(),
                    wss.getCreatedUser());
            }
        }
        return user;
    }

    @Transactional
    void putWorksopSessionAssignmentsTrans(User user, Assignment assignment, MultipartFile multipartFile) {
        String filePath = "";
        try {
            if (multipartFile != null) {
                Map<String, Object> data = fileAdapter.updateAssignments(user.getStartup().getIntakeProgram().getId(),
                    user.getStartup().getId(), assignment.getId(), multipartFile);
                AssignmentFile af = new AssignmentFile();
                af.setAssignment(assignment);
                af.setName((String) data.get("fileName"));
                af.setPath((String) data.get("filePath"));
                af.setWillManagement(false);
                assignmentFileRepository.save(af);
            }
        } catch (Exception e1) {
            LOGGER.error(e1.getMessage());
        }
    }

    @Override
    @Transactional
    public Object cloneWorksopSessionAssignment(CurrentUserObject currentUserObject, Long workshopSessionId,
                                                Long assignmentId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            Assignment assignment = assignmentRepository
                .findByRefAssignment_IdAndWorkshopSession_IdAndSubmittedStartup_Id(assignmentId, workshopSessionId,
                    user.getStartup().getId());
            if (assignment == null) {
                Optional<WorkshopSession> ws = workshopSessionRepository.findById(workshopSessionId);
                Optional<Assignment> ref = assignmentRepository.findById(assignmentId);
                if (ws.isPresent() & ref.isPresent()) {
                    Assignment e = new Assignment();
                    e.setDescription(ref.get().getDescription());
                    e.setDueDate(ref.get().getDueDate());
                    e.setName(ref.get().getName());
                    e.setRefAssignment(ref.get());
                    e.setStatus(Constant.NOT_SUBMITTED.toString());
                    e.setSubmittedStartup(user.getStartup());
                    e.setSubmittedUser(user);
                    e.setWorkshopSession(ws.get());
                    assignmentRepository.save(e);
                    Map<String, Object> obj = new HashMap<>();
                    obj.put("id", e.getId());
                    obj.put("description", e.getDescription());
                    obj.put("dueDate", e.getDueDate());
                    obj.put("name", e.getName());
                    obj.put("workshopSessionId", workshopSessionId);
                    return obj;
                }
            }
        }
        return null;
    }

    @Override
    @Transactional
    public Map<String, Object> getWorksopSessionAssignment(CurrentUserObject currentUserObject, Long workshopSessionId,
                                                           Long assignmentId) {
        Map<String, Object> obj = null;
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            Assignment assignment = assignmentRepository.findByIdAndWorkshopSession_IdAndSubmittedStartup_Id(
                assignmentId, workshopSessionId, user.getStartup().getId());
            if (assignment != null) {
                List<ProjectIdNamePathAssignment> downloadableMaterials = assignmentFileRepository
                    .findByAssignment_IdAndWillManagementTrue(assignment.getRefAssignment().getId());
                List<ProjectIdNamePathAssignment> uploadedAssignments = assignmentFileRepository
                    .findByAssignment_IdAndWillManagementFalse(assignment.getId());
                obj = new HashMap<>();
                obj.put("id", assignment.getId());
                obj.put("description", assignment.getDescription());
                obj.put("dueDate", assignment.getDueDate().toInstant().toEpochMilli());
                obj.put("name", assignment.getName());
                obj.put("status", assignment.getStatus());
                if (assignment.getCreatedUser() != null) {
                    obj.put("createdUser", assignment.getCreatedUser().getAlias());
                }
                if (assignment.getSubmittedUser() != null) {
                    obj.put("submittedUser", assignment.getSubmittedUser().getAlias());
                }
                if (assignment.getSubmittedStartup() != null) {
                    obj.put("submittedStartup", assignment.getSubmittedStartup().getStartupName());
                }
                obj.put("downloadableMaterials", downloadableMaterials);
                obj.put("uploadedAssignments", uploadedAssignments);
                obj.put("review1Status", assignment.getReview1Status());
                if (!assignment.getReview1Status().equals(Constant.PENDING.toString())) {
                    obj.put("review1Comment", assignment.getReview1Comment());
                    obj.put("review1By", assignment.getReview1User().getAlias());
                    obj.put("review1ById", assignment.getReview1User().getId());
                }
                obj.put("review2Status", assignment.getReview2Status());
                if (!assignment.getReview2Status().equals(Constant.PENDING.toString())) {
                    obj.put("review2Comment", assignment.getReview2Comment());
                    obj.put("review2By", assignment.getReview2User().getAlias());
                    obj.put("review2ById", assignment.getReview2User().getId());
                }
                return obj;
            }
        }
        return obj;
    }

    // #Management- workshopsessions -survey

    @Override
    @Transactional
    public Object getManagementWorkShopSessionSurveys(CurrentUserObject currentUserObject, Long academyRoomId,
                                                      Long workshopSessionId, Pageable paging, String filterBy, String searchBy) {
        List<SurveyManagementDto> list = Collections.emptyList();
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
        if (workshopSession != null && user.getWillManagement()) {
            List<SurveyManagementDto> surveys = surveyRepositoryCustom.getSurveys(user, academyRoomId,
                workshopSessionId, paging, filterBy, searchBy);
            if (surveys.size() > 0) {
                Map<String, Object> obj = new HashMap<>();
                obj.put("startupCount", startupRepository.count());
                obj.put("surveys", surveys);
                return obj;
            }
        }
        return list;
    }

    @Override
    @Transactional
    public Object createManagementWorkShopSessionSurveyTemplates(CurrentUserObject currentUserObject,
                                                                 PostSurveyTemplateManagementDto surveyTemplateRequest, Long workshopSessionId, Long academyRoomId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);
        Optional<Survey> existingSurveyTemplate = surveyRepository
            .findById(surveyTemplateRequest.getFormTemplateId() != null && !surveyTemplateRequest.getIsNew()
                ? surveyTemplateRequest.getFormTemplateId()
                : (long) 0);
        if (workshopSession != null && user.getWillManagement()) {
            Survey s = new Survey();
            if (!surveyTemplateRequest.getIsNew() && existingSurveyTemplate.isPresent()) {
                s.setRefFormTemplate(existingSurveyTemplate.get());
            }
            s.setJsonForm(surveyTemplateRequest.getJsonForm());
            s.setName(surveyTemplateRequest.getName());
            s.setWorkshopSession(workshopSession);
            s.setStatus(Constant.DRAFT.toString());
            s.setCreatedUser(user);
            if (surveyTemplateRequest.getDueDate() != null) {
                s.setDueDate(new Date(surveyTemplateRequest.getDueDate()));
            }
            s = surveyRepository.save(s);
            return s;
        }
        return null;
    }

    @Override
    @Transactional
    public Object publishManagementWorkShopSessionSurveyTemplates(CurrentUserObject currentUserObject,
                                                                  Long workshopSessionId, Long academyRoomId, Long surveyId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Survey survey = surveyRepository
            .findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNull(surveyId,
                workshopSessionId, academyRoomId);
        if (survey != null && user.getWillManagement()) {
            survey.setStatus(Constant.PUBLISHED.toString());
            surveyRepository.save(survey);
            return survey;
        }
        return null;
    }

    @Override
    @Transactional
    public Object deleteManagementWorkShopSessionSurveyTemplates(CurrentUserObject currentUserObject,
                                                                 Long workshopSessionId, Long academyRoomId, Long surveyId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Survey survey = surveyRepository
            .findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNull(surveyId,
                workshopSessionId, academyRoomId);
        if (survey != null && !survey.getStatus().equals(Constant.PUBLISHED.toString())) {
            surveyRepository.removeById(surveyId);
            return surveyId;
        }
        return "cannot delete published survey";

    }

    @Override
    @Transactional
    public List<SurveyManagementDto> getWorkShopSessionSurveysSubmitted(CurrentUserObject currentUserObject,
                                                                        Long academyRoomId, Long workshopSessionId, Long surveyId, Pageable paging, String filterBy,
                                                                        String searchBy) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        List<SurveyManagementDto> list = Collections.emptyList();
        if (user.getWillManagement()) {
            Page<Survey> submittedAssignmentList = surveyRepository
                .findAll(WorkShopSpecification.getWorkshopSessionsSurveysSubmitted(user, surveyId,
                    workshopSessionId, filterBy, searchBy, academyRoomId), paging);
            list = surveyMapper.toSurveyManagementDtoList(submittedAssignmentList);
        }
        return list;
    }

    @Override
    @Transactional
    public SurveyManagementDto getManagementWorkShopSessionSurvey(CurrentUserObject currentUserObject,
                                                                  Long academyRoomId, Long workshopSessionId, Long surveyId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Survey survey = surveyRepository
            .findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNull(surveyId,
                workshopSessionId, academyRoomId);
        if (user.getWillManagement() && survey != null) {
            Long submittedCount = surveyRepository
                .countByRefSurvey_IdAndRefSurvey_WorkshopSession_IdAndRefSurvey_WorkshopSession_AcademyRoom_Id(
                    surveyId, workshopSessionId, academyRoomId);
            return surveyMapper.toSurveyManagementDtoWithSubmitCount(survey, submittedCount);
        }
        return null;
    }

    @Override
    @Transactional
    public Object getManagementWorkShopSessionSubmittedSurvey(CurrentUserObject currentUserObject, Long academyRoomId,
                                                              Long workshopSessionId, Long submittedSurveyId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Map<String, Object> obj = null;
        if (user.getWillManagement()) {
            Survey submittedSurvey = surveyRepository
                .findByIdAndRefSurvey_WorkshopSession_IdAndRefSurvey_WorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNotNull(
                    submittedSurveyId, workshopSessionId, academyRoomId);
            if (submittedSurvey != null) {
                Survey survey = surveyRepository
                    .findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNull(
                        submittedSurvey.getRefSurvey().getId(), workshopSessionId, academyRoomId);
                if (survey != null) {
                    Long submittedCount = surveyRepository
                        .countByRefSurvey_IdAndRefSurvey_WorkshopSession_IdAndRefSurvey_WorkshopSession_AcademyRoom_Id(
                            submittedSurvey.getRefSurvey().getId(), workshopSessionId, academyRoomId);
                    obj = new HashMap<>();
                    obj.put("survey", surveyMapper.toSurveyManagementDtoWithSubmitCount(survey, submittedCount));
                    obj.put("submittedSurvey", surveyMapper.toSurveyManagementDto(submittedSurvey));
                }
            }
        }
        return obj;
    }

    @Override
    @Transactional
    public Object reviewManagementWorkShopSessionSurvey(CurrentUserObject currentUserObject,
                                                        PutSurveyManagementDto workShopSessionSurveyRequest, Long academyRoomId, Long workshopSessionId,
                                                        Long surveyId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Survey survey = surveyRepository
            .findByIdAndRefSurvey_WorkshopSession_IdAndRefSurvey_WorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNotNull(
                surveyId, workshopSessionId, academyRoomId);
        boolean reviewerFlag = false;
        if (survey != null && survey.getStatus().equals(Constant.SUBMITTED.toString())
            && (workShopSessionSurveyRequest.getStatus().equals(Constant.APPROVED.toString())
            || workShopSessionSurveyRequest.getStatus().equals(Constant.RESUBMIT.toString()))) {
            // already 2 reviewers approved cannot be reviewed again
            if (!survey.getReview2Status().equals(Constant.APPROVED.toString())) {

                // 1st reviewer should be coach or trainer
                if (!survey.getReview1Status().equals(Constant.APPROVED.toString())
                    && !user.getRole().getRoleName().equals(RoleName.ROLE_COACHES_AND_TRAINERS)) {
                    return "Coach or trainer should be the first approver";
                }
                if (!survey.getReview1Status().equals(Constant.APPROVED.toString())
                    && user.getRole().getRoleName().equals(RoleName.ROLE_COACHES_AND_TRAINERS)) {
                    survey.setReview1User(user);
                    survey.setReview1Status(workShopSessionSurveyRequest.getStatus());
                    survey.setReview1Comment(workShopSessionSurveyRequest.getComments());
                    reviewerFlag = true;
                }

                // same reviewer cannot review twice
                // 2nd reviewer should be management user
                if (survey.getReview1Status().equals(Constant.APPROVED.toString()) && !reviewerFlag
                    && user.getRole().getRoleName().equals(RoleName.ROLE_COACHES_AND_TRAINERS)) {
                    return "Coach or trainer cannot be the second approver";
                }
                if (survey.getReview1Status().equals(Constant.APPROVED.toString()) && !reviewerFlag
                    && !Objects.equals(survey.getReview1User().getId(), user.getId())
                    && !user.getRole().getRoleName().equals(RoleName.ROLE_COACHES_AND_TRAINERS)) {
                    survey.setReview2User(user);
                    survey.setReview2Status(workShopSessionSurveyRequest.getStatus());
                    survey.setReview2Comment(workShopSessionSurveyRequest.getComments());
                    reviewerFlag = true;
                }

                if (reviewerFlag) {
                    Optional<WorkshopSessionSubmissions> wss = workshopSessionSubmissionsRepository
                        .findByFileTypeAndMetaDataIdAndWorkshopSession_Id(Constant.SURVEY.toString(),
                            survey.getId(), survey.getWorkshopSession().getId());
                    // check entry in workshop submission log
                    if (wss.isPresent()) {

                        if (workShopSessionSurveyRequest.getStatus().equals(Constant.RESUBMIT.toString())) {
                            // re-submit due date
                            survey.setDueDate(new Date(workShopSessionSurveyRequest.getDueDate()));
                            wss.get().setStatus(Constant.RESUBMIT.toString());
                            survey.setStatus(Constant.RESUBMIT.toString());
                        }

                        if (survey.getReview1Status().equals(Constant.APPROVED.toString())
                            && survey.getReview2Status().equals(Constant.APPROVED.toString())) {
                            wss.get().setStatus(Constant.APPROVED.toString());
                            survey.setStatus(Constant.APPROVED.toString());
                        }
                        surveyRepository.save(survey);
                        workshopSessionSubmissionsRepository.save(wss.get());

                        return survey;
                    }
                }
            } else {
                return "already approved by both approvers";
            }
        }
        return null;
    }

    @Override
    @Transactional
    public Object unShareAcademyRoom(CurrentUserObject currentUserObject, Long academyRoomId,
                                     PutAcademyRoomShareDto putAcademyRoomShareDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<AcademyRoom> academyRoom = academyRoomRepository.findById(academyRoomId);
        if (user.getWillManagement() && academyRoom.isPresent()) {
            if (academyRoom.get().getStatusPublish().equals(Constant.PUBLISHED.toString())) {
                return "academyRoomId is published";
            }
            AcademyRoom academyRoomShared = academyRoom.get();
            // check shared member is a management user or not
            Optional<User> userUnShared = userRepository.findById(
                putAcademyRoomShareDto.getShareMemberId() != null ? putAcademyRoomShareDto.getShareMemberId()
                    : (long) 0);
            if (userUnShared.isPresent() && userUnShared.get().getWillManagement()) {
                resourcePermissionService.unShareAcademyRoom(academyRoomShared.getId(), userUnShared.get().getId());
                return academyRoomShared;
            }
        }
        return "Invalid academy room shared user";
    }

    @Override
    @Transactional
    public Object unShareAcademyRoomWorkShopSession(CurrentUserObject currentUserObject, Long academyRoomId,
                                                    Long workshopSessionId, PutAcademyRoomShareDto putAcademyRoomShareDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession workshopSession = workshopSessionRepository
            .findByIdAndAcademyRoom_IdAndStartupIdIsNull(workshopSessionId, academyRoomId);

        if (user.getWillManagement() && workshopSession != null) {

            if (workshopSession.getStatusPublish().equals(Constant.PUBLISHED.toString())) {
                return "workshopSessionId is published";
            }
            Optional<User> userUnShared = userRepository.findById(
                putAcademyRoomShareDto.getShareMemberId() != null ? putAcademyRoomShareDto.getShareMemberId()
                    : (long) 0);

            if (userUnShared.isPresent() && userUnShared.get().getWillManagement()) {
                resourcePermissionService.unShareWorkshopSession(academyRoomId, workshopSessionId,
                    userUnShared.get().getId());
                return workshopSession;
            }

        }

        return "Invalid workshop session shared user";
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteWorksopSessionAssignment(CurrentUserObject currentUserObject, Long workshopSessionId,
                                                            Long assignmentId, Long documentId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        assignmentFileRepository.deleteWorksopSessionAssignment(assignmentId, documentId);
        return ResponseWrapper.response(null, "assignment file removed");
    }

    @Override
    public ResponseEntity<?> listFeedbackCoaches(CurrentUserObject currentUserObject, Long academyRoomId,
                                                 Long workshopSessionId, Pageable paging) {
        Set<Long> usrList1 = userResourcePermissionRepository.getUsersByResource(academyRoomId, ResourceUtil.mar);
        Set<Long> usrList2 = userResourcePermissionRepository.getUsersByResource(workshopSessionId, ResourceUtil.mws);
        usrList2.addAll(usrList1);
        Page<User> ls = userRepository.findByIdInAndRole_RoleName(usrList2,
            RoleName.ROLE_COACHES_AND_TRAINERS, paging);
        return ResponseWrapper.response(ls.map(userMapper::toGetUserDto));
    }

    @Override
    @Transactional
    public List<StartupFeedbackFormsDto> getManagementWorkShopSessionFeedbackSubmissionsByCoach(
        CurrentUserObject currentUserObject, Long academyRoomId, Long workshopSessionId, Long feedbackId,
        Long coachId, Pageable paging, String filterBy, String filterKeyword, String sortData) {
        Optional<User> user = userRepository.findById(coachId);
        Optional<AcademyRoom> ar = academyRoomRepository.findById(academyRoomId);
        Optional<Feedback> f = feedbackRepository.findById(feedbackId);
        List<Feedback> feedbacks;
        List<StartupFeedbackFormsDto> list = new ArrayList<>();
        if (user.isPresent() && user.get().getWillManagement() && ar.isPresent() && f.isPresent()) {
            List<Startup> startups;
            if (!filterKeyword.isEmpty()) {
                if (filterBy.equals("Name")) {
                    startups = startupRepository.findByStartupNameContainingIgnoreCaseAndIntakeProgram_IdAndIsRealTrue(
                        filterKeyword, ar.get().getIntakeProgram().getId());
                    feedbacks = feedbackRepository.getPRByKeyword(user.get().getId(), filterKeyword, feedbackId);
                } else {
                    startups = startupRepository
                        .findByIntakeProgram_IdAndIsRealTrue(ar.get().getIntakeProgram().getId());
                    feedbacks = feedbackRepository.findBySubmittedUser_IdAndRefFeedback_Id(user.get().getId(),
                        feedbackId);
                }
            } else {
                startups = startupRepository.findByIntakeProgram_IdAndIsRealTrue(ar.get().getIntakeProgram().getId());
                feedbacks = feedbackRepository.findBySubmittedUser_IdAndRefFeedback_Id(user.get().getId(), feedbackId);
            }
            for (Startup s : startups) {
                StartupFeedbackFormsDto sfd = new StartupFeedbackFormsDto();
                sfd.setFeedbackId(feedbackId);
                sfd.setFeedbackName(f.get().getName());
                sfd.setJsonForm(f.get().getJsonForm());
                sfd.setStartupId(s.getId());
                sfd.setStartupMembers(Collections.emptyList());
                sfd.setStartupName(s.getStartupName());
                sfd.setIntakeProgramId(s.getIntakeProgram().getId());
                sfd.setStartupProfileInfoJson(s.getProfileInfoJson());
                List<Feedback> result = feedbacks.stream()
                    .filter(item -> item.getForStartup().getId().equals(s.getId())).collect(Collectors.toList());
                if (result.size() > 0) {
                    sfd.setStatus(Constant.SUBMITTED.toString());
                    sfd.setSubmittedOn(result.get(0).getSubmitDate().toInstant().toEpochMilli());
                    sfd.setJsonForm(result.get(0).getJsonForm());
                } else {
                    sfd.setStatus(Constant.NOT_SUBMITTED.toString());
                    sfd.setSubmittedOn(null);
                }
                list.add(sfd);
            }
            if (sortData.equalsIgnoreCase("nameasc")) {
                list.sort(Comparator.comparing(StartupFeedbackFormsDto::getStartupName));
            } else if (sortData.equalsIgnoreCase("namedesc")) {
                list.sort(Comparator.comparing(StartupFeedbackFormsDto::getStartupName).reversed());
            } else if (sortData.equalsIgnoreCase("submitDateasc")) {
                list.sort(Comparator.comparing(StartupFeedbackFormsDto::getSubmittedOn,
                    Comparator.nullsFirst(Comparator.naturalOrder())));
            } else if (sortData.equalsIgnoreCase("submitDatedesc")) {
                list.sort(Comparator.comparing(StartupFeedbackFormsDto::getSubmittedOn,
                    Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
            }
        }
        return list;
    }

}
