package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.MarkCardStartupDto;
import com.stc.inspireu.dtos.PostMarkCardNotificationDto;
import com.stc.inspireu.dtos.PostMarkCardSummaryDto;
import com.stc.inspireu.dtos.PutGenerateMarkcardDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.jpa.projections.ProjectId;
import com.stc.inspireu.jpa.projections.ProjectStartupIdAndname;
import com.stc.inspireu.mappers.AcademyRoomMapper;
import com.stc.inspireu.mappers.MarkCard2022Mapper;
import com.stc.inspireu.mappers.MarkCardSummary2022Mapper;
import com.stc.inspireu.mappers.ProgressReportMapper;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.MarkCardService;
import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarkCardServiceImpl implements MarkCardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Attendance2022Repository attendance2022Repository;
    private final WorkshopSessionRepository workshopSessionRepository;
    private final UserRepository userRepository;
    private final MarkCard2022Repository markCard2022Repository;
    private final IntakeProgramRepository intakeProgramRepository;
    private final AcademyRoomRepository academyRoomRepository;
    private final MarkCardSummaryRepository markCardSummaryRepository;
    private final NotificationService notificationService;
    private final ProgessReportRepository progessReportRepository;
    private final StartupRepository startupRepository;
    private final AssignmentRepository assignmentRepository;
    private final SurveyRepository surveyRepository;
    private final AcademyRoomMapper academyRoomMapper;
    private final ProgressReportMapper progressReportMapper;
    private final MarkCard2022Mapper markCard2022Mapper;
    private final MarkCardSummary2022Mapper markCardSummary2022Mapper;

    @Transactional
    @Override
    public ResponseEntity<?> getMarkCardList(CurrentUserObject currentUserObject, Pageable paging, String filterBy) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<MarkCard2022> markCard2022;
        if (filterBy.equals(Constant.INSPIREU.toString())) {
            markCard2022 = markCard2022Repository.getByKeyword(Constant.INSPIREU.toString(), paging);
        } else {
            markCard2022 = markCard2022Repository.getByKeyword(Constant.INPACTU.toString(), paging);
        }
        return ResponseWrapper.response(markCard2022.map(markCard2022Mapper::toGetMarkCard2022Dto));
    }

    @Transactional
    @Override
    public ResponseEntity<?> getOneMarkCard(CurrentUserObject currentUserObject, Long markCardId) {
        return ResponseWrapper.response(null);
    }

    @Transactional
    @Override
    public ResponseEntity<?> markCardStartupList(CurrentUserObject currentUserObject, Long markCardId,
                                                 Long academyRoomId, Pageable pageable, String filterKeyWord) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return markCard2022Repository.findById(markCardId)
            .map(markCard2022 -> {
                Long ip = markCard2022.getIntakeProgram().getId();
                Page<ProjectStartupIdAndname> startups;
                if (!filterKeyWord.isEmpty()) {
                    startups = startupRepository
                        .findByStartupNameContainingIgnoreCaseAndIntakeProgram_IdAndIsRealTrue(filterKeyWord,
                            ip, pageable);
                } else {
                    startups = startupRepository.getRealStartupByIntake(ip, pageable);
                }
                List<Long> ids = new ArrayList<>();
                for (int j = 0; j < startups.getContent().size(); j++) {
                    ids.add(startups.getContent().get(j).getId());
                }
                List<MarkCardSummary2022> mcss = markCardSummaryRepository
                    .findByMarkCard_IdAndAcademyRoom_IdAndStartup_IdIn(markCardId, academyRoomId, ids);
                List<MarkCardStartupDto> ll = startups.getContent().stream().map(e -> {
                    MarkCardSummary2022 mcs = mcss.stream()
                        .filter(i -> Objects.equals(i.getStartup().getId(), e.getId())).findFirst()
                        .orElse(null);
                    if (mcs != null) {
                        return new MarkCardStartupDto(ip, e.getId(), e.getStartupName(), e.getProfileInfoJson(),
                            mcs.getUpdatedUser().getId(), mcs.getUpdatedUser().getAlias(),
                            ZonedDateTime.of(mcs.getModifiedOn(), ZoneId.systemDefault()).toInstant().toEpochMilli(), "Paid", mcs.getAmountPaid());
                    } else {
                        return new MarkCardStartupDto(ip, e.getId(), e.getStartupName(), e.getProfileInfoJson(),
                            null, null, null, "Pending", null);
                    }
                }).collect(Collectors.toList());
                Page<MarkCardStartupDto> ls = new PageImpl<>(ll, pageable,
                    startups.getTotalElements());
                return ResponseWrapper.response(ls);
            }).orElseThrow(() -> new CustomRunTimeException("Markcard not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> marCardSummary(CurrentUserObject currentUserObject, Long intakeProgramId,
                                            Pageable pageable, String filterKeyWord) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return null;
    }

    @Transactional
    @Override
    public ResponseEntity<?> getStartupMarkCardAcademyRoomDetails(CurrentUserObject currentUserObject,
                                                                  Long intakeProgramId, Long startupId, Long academyRoomId) {
        return ResponseWrapper.response(null);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getAcademyRoomsList(CurrentUserObject currentUserObject, Pageable paging, String
        filterBy, Long markCardId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return markCard2022Repository.findById(markCardId)
            .map(markCard2022 -> {
                Page<AcademyRoom> academyRooms = academyRoomRepository.findByIntakeProgramIdAndStatusPublish(
                    markCard2022.getIntakeProgram().getId(), Constant.PUBLISHED.toString(), paging);
                return ResponseWrapper.response(academyRooms.map(academyRoomMapper::toAcademyRoomDTO));
            }).orElseThrow(() -> new CustomRunTimeException("Mark card not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> saveMarkCardSummary(CurrentUserObject currentUserObject,
                                                 PostMarkCardSummaryDto markCardSummaryDto) {
        return ResponseWrapper.response400("user is not exist", "bad request");
    }

    @Transactional
    @Override
    public ResponseEntity<?> updateMarkCardSummary(CurrentUserObject currentUserObject,
                                                   PostMarkCardSummaryDto markCardSummaryDto, Long markCardSummaryId) {
        return ResponseWrapper.response400("user is not exist", "bad request");
    }

    @Transactional
    @Override
    public ResponseEntity<?> getMarkCardByStartup(CurrentUserObject currentUserObject, Long startupId,
                                                  Long academyRoomId) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent()) {
            Optional<Startup> startup = startupRepository.findById(startupId);
            Optional<AcademyRoom> academyRoom = academyRoomRepository.findById(academyRoomId);
            if (startup.isPresent() && academyRoom.isPresent()) {
                Optional<MarkCardSummary2022> markCardSummary = markCardSummaryRepository
                    .findByAcademyRoomIdAndStartupId(academyRoomId, startupId);
                if (markCardSummary.isPresent()) {
                    return ResponseWrapper.response(markCardSummary2022Mapper.toGetMarkCardStartupsDto(markCardSummary.get()));
                }
                Map<String, List> result = new HashMap<>();
                result.put("content", Collections.singletonList("no Data"));
                return ResponseWrapper.response(result);
            }
            return ResponseWrapper.response400(startup.isPresent() ? "startup  is not exist" : "academy room not exist",
                "bad request");

        }
        return ResponseWrapper.response400("user not found", "bad request");

    }

    @Transactional
    @Override
    public ResponseEntity<?> getParticipationPercentage(CurrentUserObject currentUserObject, Long startupId,
                                                        Long academyRoomId) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent()) {
            Optional<Startup> startup = startupRepository.findById(startupId);
            Optional<AcademyRoom> academyRoom = academyRoomRepository.findById(academyRoomId);
            if (startup.isPresent() && academyRoom.isPresent()) {
                long totalAssignments = assignmentRepository.countByTotalAssignments(academyRoomId);
                long startupSubmittedAssignments = assignmentRepository
                    .countByTotalAssignmentsAndStartupId(academyRoomId, startupId);
                long assignmentsPercentage = (startupSubmittedAssignments / totalAssignments) * 100;
                Map<String, Long> response = new HashMap<>();
                response.put("attendance", 80L);
                response.put("assignments", assignmentsPercentage);
                response.put("surveys", 70L);
                return ResponseWrapper.response(response);
            }
            return ResponseWrapper.response400(startup.isPresent() ? "startup  is not exist" : "academy room not exist",
                "bad request");
        }
        return ResponseWrapper.response400("user not found", "bad request");

    }

    @Transactional
    @Override
    public ResponseEntity<?> sendMarkCardNotification(CurrentUserObject currentUserObject, Long startupId,
                                                      Long academyRoomId, PostMarkCardNotificationDto postMarkCardNotificationDto) {
        return ResponseWrapper.response400("user not found", "bad request");

    }

    @Transactional
    @Async("asyncExecutor")
    void updateMarkCardWithStartup(MarkCardSummary2022 markCardSummaryResult, Long intakeProgramId) throws
        Exception {
    }

    @Transactional
    boolean checkProgressReportStatus(AcademyRoom academyRoom, Startup startup) throws Exception {
        return false;
    }

    @Transactional
    @Override
    public ResponseEntity<?> checkProgressCardReport(CurrentUserObject currentUserObject, Long intakeProgramId) {
        return ResponseWrapper.response(null);
    }

    @Transactional
    @Override
    public ResponseEntity<?> markCardAcademRommStatus(CurrentUserObject currentUserObject, Long markCardId,
                                                      Long academyRoomId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Map<String, Object> data = new HashMap<>();
        data.put("isMarkCardCreated", false);
        data.put("academyRoom", null);
        data.put("MarkCardSummary", null);
        academyRoomRepository.findById(academyRoomId)
            .ifPresent(room -> data.put("academyRoom", academyRoomMapper.toAcademyRoomDTO(room)));
        MarkCardSummary2022 mc = markCardSummaryRepository
            .findByMarkCard_IdAndAcademyRoom_IdAndStartupIsNull(markCardId, academyRoomId);
        if (mc != null) {
            data.put("MarkCardSummary", markCardSummary2022Mapper.toGetMarkCardStartupsDto(mc));
            data.put("isMarkCardCreated", mc.getIsMarkCardGenerated());
        }
        return ResponseEntity.ok(data);
    }

    @Transactional
    @Override
    public ResponseEntity<?> generateMarkcard(CurrentUserObject currentUserObject, Long markCardId, Long
        academyRoomId, PutGenerateMarkcardDto putGenerateMarkcardDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        MarkCard2022 mc = markCard2022Repository.findById(markCardId).orElseThrow(() -> new CustomRunTimeException("Mark card not found"));
        AcademyRoom academyRoom = academyRoomRepository.findById(academyRoomId).orElseThrow(() -> new CustomRunTimeException("Academy room not found"));
        MarkCardSummary2022 mcs = markCardSummaryRepository
            .findByMarkCard_IdAndAcademyRoom_IdAndStartupIsNull(markCardId, academyRoomId);
        if (mcs == null) {
            MarkCardSummary2022 e = new MarkCardSummary2022();
            e.setAcademyRoom(academyRoom);
            e.setCreatedUser(user);
            e.setMarkCard(mc);
            e.setUpdatedUser(user);
            e.setJsonMarkCard(putGenerateMarkcardDto.getJsonMarkCard());
            e.setIsMarkCardGenerated(true);
            markCardSummaryRepository.save(e);
        } else {
            mcs.setUpdatedUser(user);
            mcs.setIsMarkCardGenerated(true);
            markCardSummaryRepository.save(mcs);
        }
        return ResponseWrapper.response(null, "mark card created");
    }

    @Transactional
    @Override
    public ResponseEntity<?> getMarkCardAcademRoomStartupInfo(CurrentUserObject currentUserObject, Long markCardId,
                                                              Long academyRoomId, Long startupId, String timezone) {
        Map<String, Object> data = new HashMap<>();
        data.put("startupProfileJson", null);
        data.put("startupName", "");
        data.put("intakeProgramId", 0);
        data.put("markCardTemplate", null);
        data.put("submittedMarkCard", null);
        data.put("attendanceSubmitted", 0);
        data.put("assigmentsSubmitted", 0);
        data.put("surveysSubmitted", 0);
        data.put("progressReportSubmitted", 0);
        data.put("attendanceTotal", 1);
        data.put("assigmentsTotal", 1);
        data.put("surveysTotal", 1);
        data.put("progressReportTotal", 1);
        data.put("anyAttendance", false);
        data.put("anyAssigments", false);
        data.put("anySurveys", false);
        data.put("anyProgressReport", false);
        data.put("lastProgressReport", null);
        data.put("progressReportMonth", 0);
        data.put("progressReportYear", 0);
        data.put("isProgressReportSubmitted", false);
        Optional<AcademyRoom> academyRoom = academyRoomRepository.findById(academyRoomId);
        Optional<Startup> startup = startupRepository.findById(startupId);
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (academyRoom.isPresent() && startup.isPresent() && user.isPresent()) {
            data.put("startupProfileJson", startup.get().getProfileInfoJson());
            data.put("startupName", startup.get().getStartupName());
            data.put("intakeProgramId", startup.get().getIntakeProgram().getId());
            MarkCardSummary2022 mcs = markCardSummaryRepository
                .findByMarkCard_IdAndAcademyRoom_IdAndStartupIsNull(markCardId, academyRoomId);
            if (mcs != null) {
                data.put("markCardTemplate", mcs.getJsonMarkCard());
                MarkCardSummary2022 mcs1 = markCardSummaryRepository
                    .findByMarkCard_IdAndAcademyRoom_IdAndStartup_Id(markCardId, academyRoomId, startupId);
                if (mcs1 != null) {
                    Map<String, Object> d = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("jsonMarkCards", mcs1.getJsonMarkCard());
                            put("progressReportId", mcs1.getProgressReportId());
                            put("payableAmount", mcs1.getAmountPaid());
                        }
                    };
                    data.put("submittedMarkCard", d);
                } else {
                    Set<ProjectId> attendanceTotal = workshopSessionRepository
                        .findByAcademyRoom_IdAndStatusPublishAndStartupIsNull(academyRoomId,
                            Constant.PUBLISHED.toString());
                    if (attendanceTotal.size() > 0) {
                        data.put("attendanceTotal", 100);
                        data.put("anyAttendance", true);
                        Set<Long> arIds = academyRoomRepository
                            .getAllAcademyRoomIdsByRefAcademyRoomIdAndStartup(academyRoomId, startupId);
                        if (arIds.size() > 0) {
                            List<Attendance2022> als = attendance2022Repository
                                .findByIntakeProgram_IdAndStartup_IdAndAcademyRoom_Id(
                                    startup.get().getIntakeProgram().getId(), startup.get().getId(),
                                    arIds.iterator().next());

                            if (als.size() > 0) {
                                data.put("attendanceSubmitted",
                                    als.stream().map(Attendance2022::getPercentage).reduce(0, Integer::sum)
                                        / als.size());
                            }
                        }
                    }
                    Set<Long> wsIds = workshopSessionRepository.getAllWorkshopSessionIdsByAcademyRoomId(academyRoomId,
                        Constant.NEW.toString(), Constant.PUBLISHED.toString());
                    if (wsIds == null) {
                        wsIds = new HashSet<>();
                    }
                    Set<ProjectId> surveysTotal = surveyRepository
                        .findByWorkshopSessionIdInAndStatusAndRefSurveyIsNull(wsIds, Constant.PUBLISHED.toString());
                    if (surveysTotal == null) {
                        surveysTotal = new HashSet<>();
                    }
                    data.put("surveysTotal", surveysTotal.size() == 0 ? 1 : surveysTotal.size());
                    data.put("anySurveys", surveysTotal.size() != 0);
                    Set<ProjectId> assigmentsTotal = assignmentRepository
                        .findByWorkshopSessionIdInAndStatusAndSubmittedStartupIsNull(wsIds,
                            Constant.PUBLISHED.toString());
                    if (assigmentsTotal == null) {
                        assigmentsTotal = new HashSet<>();
                    }
                    data.put("assigmentsTotal", assigmentsTotal.size() == 0 ? 1 : assigmentsTotal.size());
                    data.put("anyAssigments", assigmentsTotal.size() != 0);
                    long surveysSubmitted = surveyRepository.countByRefSurveyIdInAndStatusAndSubmittedStartup_Id(
                        surveysTotal.stream().map(ProjectId::getId).collect(Collectors.toSet()),
                        Constant.SUBMITTED.toString(), startup.get().getId());
                    data.put("surveysSubmitted", surveysSubmitted);
                    long assigmentsSubmitted = assignmentRepository
                        .countByRefAssignmentIdInAndReview1StatusAndReview2StatusAndSubmittedStartup_Id(
                            assigmentsTotal.stream().map(ProjectId::getId).collect(Collectors.toSet()),
                            Constant.APPROVED.toString(), Constant.APPROVED.toString(), startup.get().getId());
                    data.put("assigmentsSubmitted", assigmentsSubmitted);
                    Date date = new Date();
                    LocalDate nowdate = date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
                    int month = nowdate.getMonthValue();
                    int year = nowdate.getYear();
                    data.put("progressReportYear", year);
                    Pageable top2 = PageRequest.of(0, 1);
                    List<ProgressReport> prs = progessReportRepository
                        .findByIntakeProgram_IdAndStartup_IdAndYearAndStatusOrderByCreatedOnDesc(
                            startup.get().getIntakeProgram().getId(), startup.get().getId(), year,
                            Constant.SUBMITTED.toString(), top2);
                    if (prs.size() > 0) {
                        ProgressReport pr = prs.get(0);
                        data.put("lastProgressReport", progressReportMapper.toProgressReportDto(pr));
                        data.put("progressReportMonth", pr.getMonth());
                        if (pr.getMonth().equals(month)) {
                            data.put("progressReportSubmitted", 1);
                            List<MarkCardSummary2022> mcs11 = markCardSummaryRepository
                                .getByStartupIdAndProgressReportId(startup.get().getId(), pr.getId());
                            if (mcs11.size() > 0) {
                                data.put("isProgressReportSubmitted", true);
                            }

                        }
                    }

                }

            }

        }
        return ResponseWrapper.response(data);

    }

    @Transactional
    @Override
    public ResponseEntity<?> notifyStartup(CurrentUserObject currentUserObject, Long markCardId, Long
        academyRoomId, Long startupId, PostMarkCardNotificationDto postMarkCardNotificationDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        MarkCard2022 mc = markCard2022Repository.findById(markCardId).orElseThrow(() -> new CustomRunTimeException("Mark card not found"));
        AcademyRoom academyRoom = academyRoomRepository.findById(academyRoomId).orElseThrow(() -> new CustomRunTimeException("Academy room not found"));
        Startup startup = startupRepository.findById(startupId).orElseThrow(() -> new CustomRunTimeException("Startup not found"));
        MarkCardSummary2022 mcs1 = markCardSummaryRepository
            .findByMarkCard_IdAndAcademyRoom_IdAndStartup_Id(markCardId, academyRoomId, startupId);
        if (Objects.isNull(mcs1)) {
            MarkCardSummary2022 e = new MarkCardSummary2022();
            e.setAcademyRoom(academyRoom);
            e.setCreatedUser(user);
            e.setMarkCard(mc);
            e.setAmountPaid(postMarkCardNotificationDto.getPayableAmount());
            e.setStartup(startup);
            e.setUpdatedUser(user);
            e.setProgressReportId(postMarkCardNotificationDto.getProgressReportId());
            e.setJsonMarkCard(postMarkCardNotificationDto.getJsonMarkCards());
            e.setIsMarkCardGenerated(true);
            markCardSummaryRepository.save(e);
            notificationService.markCardNotification(user, startup, postMarkCardNotificationDto);
            long totalStatups = startupRepository.getCountRealStartupByIntake(academyRoom.getId());
            long submittedStartups = markCardSummaryRepository.getCountSubmittedStartups(markCardId,
                academyRoomId);
            if (totalStatups == submittedStartups) {
                academyRoom.setMarkCardNotified(true);
                academyRoomRepository.save(academyRoom);
            }
            return ResponseWrapper.response(null, "startup notified");
        }
        return ResponseWrapper.response(null, "startup already notified");
    }

    @Transactional
    @Override
    public ResponseEntity<?> saveTemplate(CurrentUserObject currentUserObject, Long markCardId, Long academyRoomId,
                                          PutGenerateMarkcardDto putGenerateMarkcardDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        MarkCard2022 mc = markCard2022Repository.findById(markCardId).orElseThrow(() -> new CustomRunTimeException("MarkCard not found"));
        AcademyRoom academyRoom = academyRoomRepository.findById(academyRoomId).orElseThrow(() -> new CustomRunTimeException("Academy room not found"));
        MarkCardSummary2022 mcs = markCardSummaryRepository
            .findByMarkCard_IdAndAcademyRoom_IdAndStartupIsNull(markCardId, academyRoomId);
        if (Objects.isNull(mcs)) {
            MarkCardSummary2022 e = new MarkCardSummary2022();
            e.setAcademyRoom(academyRoom);
            e.setCreatedUser(user);
            e.setMarkCard(mc);
            e.setUpdatedUser(user);
            e.setJsonMarkCard(putGenerateMarkcardDto.getJsonMarkCard());
            e.setIsMarkCardGenerated(false);
            markCardSummaryRepository.save(e);
            return ResponseWrapper.response(null, "mark card template created");
        } else {
            mcs.setUpdatedUser(user);
            mcs.setJsonMarkCard(putGenerateMarkcardDto.getJsonMarkCard());
            markCardSummaryRepository.save(mcs);
            return ResponseWrapper.response(null, "mark card template updated");
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> alertStartup(CurrentUserObject currentUserObject, Long markCardId, Long academyRoomId,
                                          Long startupId, PostMarkCardNotificationDto postMarkCardNotificationDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        MarkCard2022 mc = markCard2022Repository.findById(markCardId).orElseThrow(() -> new CustomRunTimeException("Mark card not found"));
        AcademyRoom academyRoom = academyRoomRepository.findById(academyRoomId).orElseThrow(() -> new CustomRunTimeException("Academy room not found"));
        Startup startup = startupRepository.findById(startupId).orElseThrow(() -> new CustomRunTimeException("Startup not found"));
        notificationService.alertStartup(user, startup, academyRoom, mc, postMarkCardNotificationDto);
        return ResponseWrapper.response(null, "startup informed");
    }

    @Transactional
    @Override
    public ResponseEntity<?> getSummaryColumsList(CurrentUserObject currentUserObject, Long markCardId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        markCard2022Repository.findById(markCardId).orElseThrow(() -> new CustomRunTimeException("Mark card not found"));
        List<MarkCardSummary2022> mcss = markCardSummaryRepository
            .findByMarkCard_IdAndStartupIsNullAndIsMarkCardGeneratedTrue(markCardId);
        List<Map<String, Object>> ll = mcss.stream().map(i -> {
            Map<String, Object> e = new HashedMap<>();
            e.put("jsonMarkCard", i.getJsonMarkCard());
            e.put("academyRoomName", i.getAcademyRoom().getName());
            e.put("academyRoomId", i.getAcademyRoom().getId());
            return e;
        }).collect(Collectors.toList());
        return ResponseWrapper.response(ll);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getSummaryStartups(CurrentUserObject currentUserObject, Long markCardId, String
        filterBy, String filterKeyword, Pageable paging) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        MarkCard2022 mc = markCard2022Repository.findById(markCardId).orElseThrow(() -> new CustomRunTimeException("Mark card not found"));
        Page<Startup> startups = startupRepository.getSummaryStartups(mc.getIntakeProgram().getId(), filterBy,
            filterKeyword, paging);
        Set<Long> startupIds = startups.getContent().stream().map(BaseEntity::getId).collect(Collectors.toSet());
        if (startupIds.isEmpty()) {
            startupIds.add(0L);
        }
        List<MarkCardSummary2022> ll = markCardSummaryRepository.findByMarkCard_IdAndStartup_IdIn(markCardId,
            startupIds);
        Page<Map<String, Object>> ls = new PageImpl<>(startups.getContent().stream().map(i -> {
            Map<String, Object> e = new HashedMap<>();
            e.put("startupId", i.getId());
            e.put("startupName", i.getStartupName());
            e.put("startupProfileInfoJson", i.getProfileInfoJson());
            List<MarkCardSummary2022> l = ll.stream().filter(c -> i.getId().equals(c.getStartup().getId()))
                .collect(Collectors.toList());
            Map<String, Object> ar = new HashedMap<>();
            for (MarkCardSummary2022 markCardSummary2022 : l) {
                ar.put(markCardSummary2022.getAcademyRoom().getId().toString(),
                    markCardSummary2022.getJsonMarkCard());
            }
            e.put("academyRooms", ar);
            ll.removeIf(c -> i.getId().equals(c.getStartup().getId()));
            return e;
        }).collect(Collectors.toList()), paging, startups.getTotalElements());
        return ResponseWrapper.response(ls);
    }

}
