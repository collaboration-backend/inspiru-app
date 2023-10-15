package com.stc.inspireu.services.impl;

import com.stc.inspireu.authorization.Roles;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.beans.MailMetadata;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.mappers.*;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.ManagementService;
import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.utils.JwtUtil;
import com.stc.inspireu.utils.PasswordUtil;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ManagementServiceImpl implements ManagementService {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;

    @Value("${ui.url}")
    private String uiUrl;

    @Value("${ui.userInvitationPath}")
    private String userInvitationPath;

    @Value("${ui.startupMemberInvitationPath}")
    private String startupMemberInvitationPath;
    private final FeedbackRepository feedbackRepository;
    private final WorkshopSessionRepository workshopSessionRepository;
    private final StartupRepository startupRepository;
    private final OneToOneMeetingRepository oneToOneMeetingRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final IntakeProgramRepository intakeProgramRepository;
    private final AttendanceRepository attendanceRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final WorkshopSessionSubmissionsRepository workshopSessionSubmissionsRepository;
    private final AcademyRoomRepository academyRoomRepository;
    private final AcademyRoomMapper academyRoomMapper;
    private final WorkshopSessionMapper workshopSessionMapper;
    private final FeedbackMapper feedbackMapper;
    private final IntakeProgramMapper intakeProgramMapper;
    private final AttendanceMapper attendanceMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Object inviteUser(InviteUserDto inviteUserDto) {
        return roleRepository.findById(inviteUserDto.getRoleId())
            .map(role -> {
                IntakeProgram intakeProgram = intakeProgramRepository.findById(inviteUserDto.getIntakeNumber())
                    .orElse(new IntakeProgram());
                {
                    List<String> excludedRoles = new ArrayList<>();
                    excludedRoles.add(Roles.ROLE_SUPER_ADMIN.name());
                    excludedRoles.add(Roles.ROLE_STARTUPS_MEMBER.name());
                    excludedRoles.add(Roles.ROLE_STARTUPS_ADMIN.name());
                    if (excludedRoles.contains(role.getRoleName()))
                        throw new CustomRunTimeException("You can not invite a " + role.getRoleName(), HttpStatus.BAD_REQUEST);
                    User user = new User();
                    User user1 = userRepository.findByEmail(inviteUserDto.getRegistratedEmailAddress());
                    Map<String, Object> claims = null;
                    String token = null;
                    if (user1 != null) {
                        if (user1.getInvitationStatus().equals(Constant.INVITAION_SEND.toString())) {
                            claims = new HashMap<String, Object>() {
                                private static final long serialVersionUID = 1L;

                                {
                                    if (role.getRoleName().equals(RoleName.Value.ROLE_STARTUPS_ADMIN.toString()) ||
                                        role.getRoleName().equals(RoleName.Value.ROLE_STARTUPS_MEMBER.toString())) {
                                        put("isStartup", true);
                                    } else {
                                        put("isManagement", true);
                                    }
                                    put("email", inviteUserDto.getRegistratedEmailAddress());
                                    put("roleName", role.getRoleName());
                                    put("roleId", role.getId());
                                    put("message", Constant.USER_INVITATION.toString());
                                    put("programName", intakeProgram.getProgramName());
                                    put("intakeNumber", inviteUserDto.getIntakeNumber());
                                }
                            };
                            token = jwtUtil.genericJwtToken(claims);
                            user1.setEmail(inviteUserDto.getRegistratedEmailAddress());
                            user1.setRole(role);
                            user1.setInvitationStatus(Constant.INVITAION_SEND.toString());
                            user1.setInviteToken(token);
                            user1.setAlias(inviteUserDto.getName());
                            user1.setPhoneNumber(inviteUserDto.getPhoneNumber());
                            user1.setPhoneDialCode(inviteUserDto.getPhoneDialCode());
                            user1.setPhoneCountryCodeIso2(inviteUserDto.getPhoneCountryCodeIso2());
                            userRepository.save(user1);
                        }
                    } else {
                        claims = new HashMap<String, Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                if (role.getRoleName().equals(RoleName.Value.ROLE_STARTUPS_ADMIN.toString())
                                    || role.getRoleName().equals(RoleName.Value.ROLE_STARTUPS_MEMBER.toString())) {
                                    put("isStartup", true);
                                } else {
                                    put("isManagement", true);
                                }
                                put("email", inviteUserDto.getRegistratedEmailAddress());
                                put("roleName", role.getRoleName());
                                put("roleId", role.getId());
                                put("message", Constant.USER_INVITATION.toString());
                                put("programName", intakeProgram.getProgramName());
                                put("intakeNumber", inviteUserDto.getIntakeNumber());
                            }
                        };
                        token = jwtUtil.genericJwtToken(claims);
                        user.setEmail(inviteUserDto.getRegistratedEmailAddress());
                        user.setRole(role);
                        user.setInvitationStatus(Constant.INVITAION_SEND.toString());
                        user.setInviteToken(token);
                        user.setAlias(inviteUserDto.getName());
                        user.setPhoneNumber(inviteUserDto.getPhoneNumber());
                        user.setPhoneDialCode(inviteUserDto.getPhoneDialCode());
                        user.setPhoneCountryCodeIso2(inviteUserDto.getPhoneCountryCodeIso2());
                        userRepository.save(user);
                    }
                    if (Objects.nonNull(claims)) {
                        MailMetadata mailMetadata = new MailMetadata();
                        Map<String, Object> props = new HashMap<>();
                        String link = "";
                        if (role.getRoleName().equals(RoleName.Value.ROLE_STARTUPS_ADMIN.toString())
                            || role.getRoleName().equals(RoleName.Value.ROLE_STARTUPS_MEMBER.toString())) {
                            link = uiUrl + startupMemberInvitationPath + "/" + token;
                        } else {
                            link = uiUrl + userInvitationPath + "/" + token;
                        }
                        props.put("inviteLink", link);
                        props.put("toMail", inviteUserDto.getRegistratedEmailAddress());
                        mailMetadata.setFrom("");
                        mailMetadata.setTo(inviteUserDto.getRegistratedEmailAddress());
                        mailMetadata.setProps(props);
                        mailMetadata.setSubject("User invitation mail");
                        mailMetadata.setTemplateFile("user-invitation-mail");
                        notificationService.sendInviteNotification(mailMetadata, "", role.getRoleName());
                    }
                    return claims;
                }
            }).orElseThrow(() -> new CustomRunTimeException("Role not found"));
    }

    @Override
    @Transactional
    public Object createFeedbacks(CurrentUserObject currentUserObject, @Valid PostFeedbackDto postFeedbackDto,
                                  Long refFeedbackId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<WorkshopSession> ws = workshopSessionRepository.findById(postFeedbackDto.getWorkshopSessionId());
        Optional<Startup> startup = startupRepository.findById(postFeedbackDto.getStartupId());
        Optional<Feedback> feedback = feedbackRepository.findById(refFeedbackId);
        Optional<Feedback> feedbackValidity = feedbackRepository
            .findByForStartup_IdAndWorkshopSession_IdAndRefFeedback_Id(postFeedbackDto.getStartupId(),
                postFeedbackDto.getWorkshopSessionId(), refFeedbackId);
        if (user.getWillManagement() && ws.isPresent() && startup.isPresent()
            && feedback.isPresent()
            && (user.getId().equals(feedback.get().getWorkshopSession().getCreatedUser().getId()))) {
            if (feedbackValidity.isPresent()) {
                return feedbackMapper.toFeedbackFormManagementDto(feedbackValidity.get());
            }
            Feedback f = new Feedback();
            f.setJsonForm(feedback.get().getJsonForm());
            f.setName(postFeedbackDto.getName());
            f.setWorkshopSession(ws.get());
            f.setForStartup(startup.get());
            f.setStatus(Constant.DRAFT.toString());
            f.setRefFeedback(feedback.get());
            f.setCreatedUser(user);
            f = feedbackRepository.save(f);
            return feedbackMapper.toFeedbackFormManagementDto(f);
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateFeedbacks(CurrentUserObject currentUserObject, PutFeedbackDto putFeedbackDto,
                                             Long refFeedbackId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<Feedback> feedback = feedbackRepository
            .findByForStartup_IdAndWorkshopSession_IdAndRefFeedback_IdAndSubmittedUser_Id(
                putFeedbackDto.getStartupId(), putFeedbackDto.getWorkshopSessionId(), refFeedbackId,
                user.getId());
        if (!feedback.isPresent()) {
            Optional<Feedback> reFeedback = feedbackRepository.findById(refFeedbackId);
            Optional<Startup> startup = startupRepository.findById(putFeedbackDto.getStartupId());
            Optional<WorkshopSession> workshopSession = workshopSessionRepository
                .findById(putFeedbackDto.getWorkshopSessionId());
            if (reFeedback.isPresent() && startup.isPresent() && workshopSession.isPresent()) {
                Feedback f = new Feedback();
                f.setCreatedUser(reFeedback.get().getCreatedUser());
                f.setForStartup(startup.get());
                f.setJsonForm(putFeedbackDto.getJsonForm());
                f.setName(reFeedback.get().getName() + "_startup_" + startup.get().getId() + "_coach_"
                    + user.getId());
                f.setRefFeedback(reFeedback.get());
                f.setRefFormTemplate(reFeedback.get().getRefFormTemplate());
                f.setStatus(Constant.SUBMITTED.toString());
                f.setSubmitDate(new Date());
                f.setSubmittedUser(user);
                f.setWorkshopSession(workshopSession.get());
                Feedback f1 = feedbackRepository.save(f);
                if (putFeedbackDto.getSubmitStatus()) {
                    WorkshopSessionSubmissions wss = new WorkshopSessionSubmissions();
                    wss.setCreatedUser(f1.getCreatedUser());
                    wss.setFileType(Constant.FEEDBACK.toString());
                    wss.setMetaDataId(f1.getId());
                    wss.setCreatedOn(f1.getCreatedOn());
                    wss.setSubmittedUser(user);
                    wss.setStartup(f1.getForStartup());
                    wss.setStatus(f1.getStatus());
                    wss.setSubmittedOn(f1.getSubmitDate());
                    wss.setSubmittedFileName(f1.getName());
                    wss.setWorkshopSession(f1.getWorkshopSession());
                    wss.setMetaDataParentId(f1.getRefFeedback().getId());
                    workshopSessionSubmissionsRepository.save(wss);
                    return ResponseWrapper.response(feedbackMapper.toFeedbackFormManagementDto(f1));
                }
            }
            return ResponseWrapper.response400("invalid workshopSessionId", "workshopSessionId");
        }
        return ResponseWrapper.response400("already submitted", "userId");
    }

    @Override
    @Transactional
    public Object getStartupFeedback(CurrentUserObject currentUserObject, Long startupId, Long workshopSessionId,
                                     Long refFeedbackId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getWillManagement()) {
            Optional<Feedback> feedback = feedbackRepository.findByForStartup_IdAndWorkshopSession_IdAndRefFeedback_Id(
                startupId, workshopSessionId, refFeedbackId);
            if (feedback.isPresent()) {
                Feedback f = feedback.get();
                return feedbackMapper.toFeedbackFormManagementDto(f);
            }
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity<Object> createOne2OneMeeting(CurrentUserObject currentUserObject,
                                                       PostOne2OneMeetingManagementDto one2OneMeetingDto) {

        return userRepository.findById(currentUserObject.getUserId())
            .map(coachOrTrainer -> {
                Startup startup = startupRepository.findById(
                    one2OneMeetingDto.getStartupId() != null ? one2OneMeetingDto.getStartupId() : (long) 0).orElseThrow(() -> new CustomRunTimeException("Startup not found"));
                List<OneToOneMeeting> startupMeetings;
                List<OneToOneMeeting> trainerMeetings;
                startupMeetings = oneToOneMeetingRepository.getByStartupScheduledMeetings(startup.getId(),
                    new Date(one2OneMeetingDto.getSessionStart()),
                    new Date(one2OneMeetingDto.getSessionEnd()));

                if (!startupMeetings.isEmpty()) {
                    return ResponseWrapper.response400(
                        "trying to schedule duplicate one2one meeting for the selected startup", "oneToOneMeetingId");
                } else {
                    trainerMeetings = oneToOneMeetingRepository.getByTrainerScheduledMeetings(coachOrTrainer.getId(),
                        new Date(one2OneMeetingDto.getSessionStart()),
                        new Date(one2OneMeetingDto.getSessionEnd()));
                    if (!trainerMeetings.isEmpty()) {
                        return ResponseWrapper.response400(
                            "trainer is not available for the selected one2one meeting session", "oneToOneMeetingId");
                    }
                }
                OneToOneMeeting oneToOneMeeting = new OneToOneMeeting();
                oneToOneMeeting.setDescription(one2OneMeetingDto.getDescription());
                oneToOneMeeting.setMeetingName(one2OneMeetingDto.getMeetingName());
                oneToOneMeeting.setSessionStart(new Date(one2OneMeetingDto.getSessionStart()));
                oneToOneMeeting.setSessionEnd(new Date(one2OneMeetingDto.getSessionEnd()));
                oneToOneMeeting.setTrainer(coachOrTrainer);
                oneToOneMeeting.setStartup(startup);
                oneToOneMeeting.setUser(coachOrTrainer);
                // meeting status will be accept if coach creates the meeting
                oneToOneMeeting.setInvitationStatus(Constant.ACCEPTED.toString());
                if (one2OneMeetingDto.getWillOnline() == null || one2OneMeetingDto.getWillOnline().equals(false)) {
                    oneToOneMeeting.setMeetingRoom(one2OneMeetingDto.getMeetingRoom());
                    oneToOneMeeting.setWillOnline(false);
                } else {
                    oneToOneMeeting.setMeetingLink(one2OneMeetingDto.getMeetingRoom());
                    oneToOneMeeting.setWillOnline(true);
                }
                oneToOneMeetingRepository.save(oneToOneMeeting);
                CalendarEvent calendarEvent = new CalendarEvent();
                calendarEvent.setSessionStart(new Date(one2OneMeetingDto.getSessionStart()));
                calendarEvent.setSessionEnd(new Date(one2OneMeetingDto.getSessionEnd()));
                calendarEvent.setStartup(startup);
                calendarEvent.setOneToOneMeeting(oneToOneMeeting);
                calendarEventRepository.save(calendarEvent);
                notificationService.create121MeetingNotificationByCoach(coachOrTrainer, startup,
                    one2OneMeetingDto.getSessionStart());
                Map<String, Object> data = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("oneToOneMeetingId", oneToOneMeeting.getId());
                        put("sessionEnd", oneToOneMeeting.getSessionEnd().toInstant().toEpochMilli());
                        put("sessionStart", oneToOneMeeting.getSessionStart().toInstant().toEpochMilli());
                        put("meetingName", oneToOneMeeting.getMeetingName());
                        put("status", oneToOneMeeting.getInvitationStatus());
                        if (one2OneMeetingDto.getWillOnline() == null || one2OneMeetingDto.getWillOnline().equals(false)) {
                            put("meetingRoom", one2OneMeetingDto.getMeetingRoom());
                            put("meetingMode", false);
                        } else {
                            put("meetingLink", one2OneMeetingDto.getMeetingRoom());
                            put("meetingMode", true);
                        }
                        put("startupName",
                            oneToOneMeeting.getStartup() != null ? oneToOneMeeting.getStartup().getStartupName() : "");
                    }
                };
                return ResponseWrapper.response(data);
            }).orElseThrow(() -> ItemNotFoundException.builder("User").build());
    }

    @Override
    @Transactional
    public Object updateOne2OneMeeting(CurrentUserObject currentUserObject,
                                       PutOne2OneMeetingManagementDto one2OneMeetingDto, Long meetingId) {
        return userRepository
            .findById(currentUserObject.getUserId() != null ? currentUserObject.getUserId() : (long) 0)
            .map(coachOrTrainer -> {
                Startup startup = startupRepository.findById(
                        one2OneMeetingDto.getStartupId() != null ? one2OneMeetingDto.getStartupId() : (long) 0)
                    .orElseThrow(() -> new CustomRunTimeException("Startup not found"));
                OneToOneMeeting proposedMeeting = oneToOneMeetingRepository
                    .findById(meetingId != null ? meetingId : (long) 0)
                    .orElseThrow(() -> new CustomRunTimeException("Meeting not found"));
                boolean oneToOneMeetingUpdated = false;
                if (Objects.equals(proposedMeeting.getStartup().getId(), startup.getId())
                    && coachOrTrainer.getWillManagement()) {
                    if (!Objects.equals(proposedMeeting.getTrainer().getId(), coachOrTrainer.getId())) {
                        if (!coachOrTrainer.getRole().getRoleName().equals(RoleName.ROLE_SUPER_ADMIN)) {
                            return "Invalid trainer";
                        }
                    }
                    // allows assigned trainer to accept or update the timings
                    if (one2OneMeetingDto.getIsAccept() && !one2OneMeetingDto.getIsNewTiming()
                        && !proposedMeeting.getInvitationStatus().equals(Constant.ACCEPTED.toString())) {
                        proposedMeeting.setInvitationStatus(Constant.ACCEPTED.toString());
                        oneToOneMeetingRepository.save(proposedMeeting);
                        oneToOneMeetingUpdated = true;
                        notificationService.conformationNotificationByCoach(coachOrTrainer, proposedMeeting.getStartup(),
                            proposedMeeting.getSessionStart().getTime(), Constant.ACCEPTED.toString());
                    }
                    // already accepted meeting cannot be updated with new timing
                    if (one2OneMeetingDto.getIsNewTiming()) {
                        if (one2OneMeetingDto.getIsAccept()) {
                            // either user can only propose new timing or accept,but both at a time
                            return "Invalid data provided";
                        }
                        if (one2OneMeetingDto.getProposedSessionStart() == null
                            && one2OneMeetingDto.getProposedSessionEnd() == null) {
                            return "Invalid proposed timings";
                        }
                        if (proposedMeeting.getInvitationStatus().equals(Constant.ACCEPTED.toString())) {
                            return "Meeting already accepted";
                        }
                        // Update propsed timing only once
                        // check whether previous session timing is null,if so update proposed timing
                        Optional<CalendarEvent> ce = calendarEventRepository
                            .findByStartup_IdAndOneToOneMeeting_IdAndSessionStartAndSessionEnd(startup.getId(),
                                proposedMeeting.getId(), proposedMeeting.getSessionStart(),
                                proposedMeeting.getSessionEnd());
                        CalendarEvent proposedEvent;
                        if (proposedMeeting.getSessionPreviousStart() == null && proposedMeeting.getSessionPreviousEnd() == null
                            && ce.isPresent()) {
                            List<OneToOneMeeting> startupMeetings;
                            List<OneToOneMeeting> trainerMeetings;
                            proposedEvent = ce.get();
                            // check startup's oneToOneMeeting for selected date range
                            startupMeetings = oneToOneMeetingRepository.getByStartupScheduledMeetingsForNewProposal(
                                startup.getId(), new Date(one2OneMeetingDto.getProposedSessionStart()),
                                new Date(one2OneMeetingDto.getProposedSessionEnd()), proposedMeeting.getId());
                            if (startupMeetings.size() > 0) {
                                return "trying to schedule duplicate one2one meeting for the selected startup";
                            } else {
                                // check trainer's oneToOneMeeting for selected date range
                                trainerMeetings = oneToOneMeetingRepository.getByTrainerScheduledMeetingsForNewProposal(
                                    coachOrTrainer.getId(),
                                    new Date(one2OneMeetingDto.getProposedSessionStart()),
                                    new Date(one2OneMeetingDto.getProposedSessionEnd()),
                                    proposedMeeting.getId());
                                if (trainerMeetings.size() > 0) {
                                    return "trainer is not available for the selected one2one meeting session";
                                }
                            }
                            proposedMeeting.setSessionPreviousStart(proposedMeeting.getSessionStart());
                            proposedMeeting.setSessionPreviousEnd(proposedMeeting.getSessionEnd());
                            proposedMeeting.setSessionStart(new Date(one2OneMeetingDto.getProposedSessionStart()));
                            proposedMeeting.setSessionEnd(new Date(one2OneMeetingDto.getProposedSessionEnd()));
                            // while updating new timings update the status as ACCEPTED
                            proposedMeeting.setInvitationStatus(Constant.ACCEPTED.toString());
                            oneToOneMeetingRepository.save(proposedMeeting);
                            // new zoom meeting link for the updated time
                            // update calendar event for the propsed timing
                            proposedEvent.setSessionStart(new Date(one2OneMeetingDto.getProposedSessionStart()));
                            proposedEvent.setSessionEnd(new Date(one2OneMeetingDto.getProposedSessionEnd()));
                            calendarEventRepository.save(proposedEvent);
                            oneToOneMeetingUpdated = true;
                            notificationService.conformationNotificationByCoach(coachOrTrainer,
                                proposedMeeting.getStartup(), one2OneMeetingDto.getProposedSessionStart(),
                                Constant.POSTPONED.toString());
                        }
                    }
                }
                if (oneToOneMeetingUpdated) {
                    return proposedMeeting;
                }
                throw new CustomRunTimeException("Invalid meeting");
            }).orElseThrow(() -> new CustomRunTimeException("coachOrTrainer not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteOne2OneMeeting(CurrentUserObject currentUserObject, Long meetingId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        OneToOneMeeting oneToOneMeeting = oneToOneMeetingRepository.findById(meetingId)
            .orElseThrow(() -> new CustomRunTimeException("Meeting not found"));
        oneToOneMeetingRepository.delete(oneToOneMeeting);
        return ResponseWrapper.response("deleted", HttpStatus.OK);
    }

    @Transactional
    @Override
    public Object getOne2OneMeeting(CurrentUserObject currentUserObject, Long meetingId) {
        return userRepository.findById(currentUserObject.getUserId()).map(coachOrTrainer -> {
            Map<String, Object> data = null;
            OneToOneMeeting oneToOneMeeting = oneToOneMeetingRepository
                .findById(meetingId).orElseThrow(() -> new CustomRunTimeException("Meeting not found"));
            if (oneToOneMeeting.getTrainer().getId().equals(coachOrTrainer.getId())
                && coachOrTrainer.getWillManagement()) {
                List<Attendance> attendancesMarked = attendanceRepository.findByOneToOneMeeting_IdAndAttendanceDate(
                    oneToOneMeeting.getId(), oneToOneMeeting.getSessionStart());
                data = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("oneToOneMeetingId", oneToOneMeeting.getId());
                        put("sessionEnd", oneToOneMeeting.getSessionEnd().toInstant().toEpochMilli());
                        put("sessionStart", oneToOneMeeting.getSessionStart().toInstant().toEpochMilli());
                        put("meetingName", oneToOneMeeting.getMeetingName());
                        put("trainer", userMapper.toGetUserDto(oneToOneMeeting.getTrainer()));
                        put("status", oneToOneMeeting.getInvitationStatus());
                        put("startupId", oneToOneMeeting.getStartup().getId());
                        put("startupName", oneToOneMeeting.getStartup().getStartupName());
                        put("startupProfileInfoJson", oneToOneMeeting.getStartup().getProfileInfoJson());
                        put("meetingMode", oneToOneMeeting.getInvitationStatus());
                        put("intakeProgramId", oneToOneMeeting.getStartup().getIntakeProgram().getId());
                        if (oneToOneMeeting.getWillOnline()) {
                            put("meetingLink", oneToOneMeeting.getMeetingLink());
                        } else {
                            put("meetingRoom", oneToOneMeeting.getMeetingRoom());
                        }
                        put("attendanceMarked", attendanceMapper.toUserAttendanceDtoList(attendancesMarked));
                    }
                };
            }
            return data;
        }).orElseThrow(() -> ItemNotFoundException.builder("User").build());
    }

    @Transactional
    @Override
    public Object getAllOne2OneMeeting(CurrentUserObject currentUserObject, Long IntakeProgram) {
        return userRepository
            .findByIdAndWillManagementTrue(currentUserObject.getUserId()).map(user -> {
                List<OneToOneMeeting> oneToOneMeetings = oneToOneMeetingRepository.findAll();
                if (!oneToOneMeetings.isEmpty()) {
                    List<Map<String, Object>> onetoOneList = new ArrayList<>();
                    for (OneToOneMeeting item : oneToOneMeetings) {
                        List<Attendance> attendancesMarked = attendanceRepository
                            .findByOneToOneMeeting_IdAndAttendanceDate(item.getId(), item.getSessionStart());
                        Map<String, Object> data = new HashMap<String, Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                put("oneToOneMeetingId", item.getId());
                                put("sessionEnd", item.getSessionEnd().toInstant().toEpochMilli());
                                put("sessionStart", item.getSessionStart().toInstant().toEpochMilli());
                                put("meetingName", item.getMeetingName());
                                put("trainer", userMapper.toGetUserDto(item.getTrainer()));
                                put("status", item.getInvitationStatus());
                                put("startupId", item.getStartup().getId());
                                put("startupName", item.getStartup().getStartupName());
                                put("meetingMode", item.getInvitationStatus());
                                put("meetingType", item.getWillOnline() ? "Online" : "Offline");
                                if (item.getWillOnline()) {
                                    put("meetingLink", item.getMeetingLink());
                                } else {
                                    put("meetingRoom", item.getMeetingRoom());
                                }
                                put("attendanceMarked", attendanceMapper.toUserAttendanceDtoList(attendancesMarked));
                            }
                        };
                        onetoOneList.add(data);
                    }
                    return onetoOneList;
                }
                return Collections.emptyList();
            }).orElse(Collections.emptyList());
    }

    @Override
    @Transactional
    public Object managementRegistration(ManagementRegistrationDto managementRegistrationDto,
                                         Map<String, Object> claims, User user) {
        Role role = roleRepository.findByRoleName(claims.get("roleName").toString());
        user.setRole(role);
        user.setAlias(managementRegistrationDto.getName());
        user.setPhoneNumber(managementRegistrationDto.getPhoneNumber());
        user.setJobTitle(managementRegistrationDto.getJobTitle());
        user.setEmail(managementRegistrationDto.getRegistratedEmailAddress());
        user.setAlias(managementRegistrationDto.getName());
        user.setInvitationStatus(Constant.REGISTERED.toString());
        user.setInviteToken(null);
        user.setPhoneCountryCodeIso2(managementRegistrationDto.getIso2CountryCode());
        user.setPhoneDialCode(managementRegistrationDto.getPhoneDialCode());
        if (!role.getRoleName().contains(Constant.STARTUP.toString())) {
            user.setWillManagement(true);
        }
        String hashedPassword = passwordUtil.getHashedPassword(managementRegistrationDto.getPassword());
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public ResponseEntity<Object> _managementRegistration(ManagementRegistrationDto managementRegistrationDto) {
        User user = userRepository.findByEmail(managementRegistrationDto.getRegistratedEmailAddress());
        if (Objects.isNull(user)) {
            return ResponseWrapper.response400("Invitation link expired", "expired");
        }
        if (user.getInvitationStatus().equals(Constant.INVITAION_SEND.toString())) {
            Map<String, Object> claims = jwtUtil.getClaimsFromGenericToken(managementRegistrationDto.getInviteToken());
            if (claims != null && managementRegistrationDto.getRegistratedEmailAddress().equals(claims.get("email"))) {
                Role role = roleRepository.findByRoleName(claims.get("roleName").toString());
                user.setRole(role);
                user.setAlias(managementRegistrationDto.getName());
                user.setPhoneNumber(managementRegistrationDto.getPhoneNumber());
                user.setJobTitle(managementRegistrationDto.getJobTitle());
                user.setEmail(managementRegistrationDto.getRegistratedEmailAddress());
                user.setAlias(managementRegistrationDto.getName());
                user.setInvitationStatus(Constant.REGISTERED.toString());
                user.setInviteToken(null);
                user.setPhoneCountryCodeIso2(managementRegistrationDto.getIso2CountryCode());
                user.setPhoneDialCode(managementRegistrationDto.getPhoneDialCode());
                if (role.getRoleName().equals(RoleName.ROLE_EXISTING_STARTUPS)) {
                    Startup startup = new Startup();
                    startup.setIsReal(true);
                    startup.setStartupName("");
                    List<IntakeProgram> ips = intakeProgramRepository
                        .findByProgramName(Constant.EXISTING_INTAKE.toString());
                    if (!ips.isEmpty()) {
                        startup.setIntakeProgram(ips.get(0));
                    }
                    Startup startup1 = startupRepository.save(startup);
                    user.setStartup(startup1);
                }
                user.setEnableEmail(true);
                user.setEnableWeb(true);
                if (!role.getRoleName().contains(Constant.STARTUP.toString())) {
                    user.setWillManagement(true);
                }
                String hashedPassword = passwordUtil.getHashedPassword(managementRegistrationDto.getPassword());
                user.setPassword(hashedPassword);
                userRepository.save(user);
                Map<String, Object> data = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("name", managementRegistrationDto.getName());
                        put("phoneNumber", managementRegistrationDto.getPhoneNumber());
                        put("intakeNumber",
                            managementRegistrationDto.getIntakeNumber() != null
                                ? managementRegistrationDto.getIntakeNumber()
                                : 0);
                        put("registratedEmailAddress", managementRegistrationDto.getRegistratedEmailAddress());
                    }
                };
                MailMetadata mailMetadata = new MailMetadata();
                Map<String, Object> props = new HashMap<>();
                props.put("toMail", managementRegistrationDto.getRegistratedEmailAddress());
                mailMetadata.setFrom("");
                mailMetadata.setTo(managementRegistrationDto.getRegistratedEmailAddress());
                mailMetadata.setProps(props);
                mailMetadata.setSubject("User invitation mail");
                mailMetadata.setTemplateFile("user-invitation-mail");
                notificationService.sendWelcomeNotification(mailMetadata, role.getRoleName());
                notificationService.registrationNotifications(user, user.getRole().getRoleAlias());
                return ResponseWrapper.response(data, "management registration success");
            } else {
                return ResponseWrapper.response400("invalid inviteToken", "inviteToken");
            }
        } else {
            return ResponseWrapper.response(
                managementRegistrationDto.getRegistratedEmailAddress() + " already registered",
                HttpStatus.CONFLICT);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> createTrainingSession(CurrentUserObject currentUserObject,
                                                   PostTrainingSessionDto postTrainingSession) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        WorkshopSession ws = workshopSessionRepository.findById(postTrainingSession.getWorkshopSessionId())
            .orElseThrow(() -> new CustomRunTimeException("Session not found"));
        TrainingSession ts = new TrainingSession();
        intakeProgramRepository.findById(postTrainingSession.getIntakeProgramId()).ifPresent(ts::setIntakeProgram);
        ts.setDescription(postTrainingSession.getDescription());
        ts.setMeetingName(postTrainingSession.getMeetingName());
        ts.setMeetingRoomOrLink(postTrainingSession.getMeetingRoomOrLink());
        ts.setSessionStart(new Date(postTrainingSession.getSessionStartDate()));
        ts.setSessionEnd(new Date(postTrainingSession.getSessionEndDate()));
        ts.setWorkshopSession(ws);
        ts.setCreatedUser(user);
        ts.setSessionStartTime(postTrainingSession.getSessionStartTime());
        ts.setSessionEndTime(postTrainingSession.getSessionEndTime());
        ts.setIsRecurring(true);
        ts.setWillOnline(postTrainingSession.getWillOnline() != null && !postTrainingSession.getWillOnline().equals(false));
        TrainingSession ts1 = trainingSessionRepository.save(ts);
        CalendarEvent calendarEvent = new CalendarEvent();
        calendarEvent.setSessionStart(new Date(postTrainingSession.getSessionStartDate()));
        calendarEvent.setSessionEnd(new Date(postTrainingSession.getSessionEndDate()));
        calendarEvent.setIsRecurring(true);
        calendarEvent.setSessionStartTime(postTrainingSession.getSessionStartTime());
        calendarEvent.setSessionEndTime(postTrainingSession.getSessionEndTime());
        calendarEvent.setTrainingSession(ts1);
        calendarEventRepository.save(calendarEvent);
        Map<String, Object> data = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("trainingSessionsId", ts1.getId());
                put("sessionEndDate", ts1.getSessionEnd().toInstant().toEpochMilli());
                put("sessionStartDate", ts1.getSessionStart().toInstant().toEpochMilli());
                put("sessionEndTime", postTrainingSession.getSessionEndTime());
                put("sessionStartTime", postTrainingSession.getSessionStartTime());
                put("meetingName", ts1.getMeetingName());
                put("workshopSessionId", ws.getId());
                put("meetingRoomOrLink", postTrainingSession.getMeetingRoomOrLink());
                if (postTrainingSession.getWillOnline() == null || postTrainingSession.getWillOnline().equals(false)) {
                    put("isOnline", false);
                } else {
                    put("isOnline", true);
                }
            }
        };
        return ResponseWrapper.response(data);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getTrainingSession(CurrentUserObject currentUserObject, Long trainingSessionId) {
        return userRepository.findById(currentUserObject.getUserId())
            .map(user -> {
                Map<String, Object> data = null;
                TrainingSession trainingSession = trainingSessionRepository.findById(trainingSessionId)
                    .orElseThrow(() -> new CustomRunTimeException("Session not found"));
                data = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("trainingSessionsId", trainingSessionId);
                        put("sessionEndDate", trainingSession.getSessionEnd().toInstant().toEpochMilli());
                        put("sessionStartDate", trainingSession.getSessionStart().toInstant().toEpochMilli());
                        put("sessionEndTime", trainingSession.getSessionEndTime());
                        put("sessionStartTime", trainingSession.getSessionStartTime());
                        put("meetingName", trainingSession.getMeetingName());
                        put("isOnline", trainingSession.getWillOnline());
                        put("workshopSessionId", trainingSession.getWorkshopSession().getId());
                        put("workshopSessionName", trainingSession.getWorkshopSession().getName());
                    }
                };
                return ResponseWrapper.response(data);
            }).orElseThrow(() -> ItemNotFoundException.builder("User").build());
    }

    @Transactional
    @Override
    public ResponseEntity<?> cancelTrainingSession(CurrentUserObject currentUserObject, Long trainingSessionId) {
        return userRepository.findById(currentUserObject.getUserId())
            .map(user -> {
                Map<String, Object> data = new HashMap<>();
                CalendarEvent calendarEvent = calendarEventRepository.findById(trainingSessionId).orElseThrow(() -> new CustomRunTimeException("Event not found"));
                if (calendarEvent.getTrainingSession() != null) {
                    TrainingSession trainingSession = calendarEvent.getTrainingSession();
                    data = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("sessionEndDate", trainingSession.getSessionEnd().toInstant().toEpochMilli());
                            put("sessionStartDate", trainingSession.getSessionStart().toInstant().toEpochMilli());
                            put("sessionEndTime", trainingSession.getSessionEndTime());
                            put("sessionStartTime", trainingSession.getSessionStartTime());
                            put("meetingName", trainingSession.getMeetingName());
                            put("isOnline", trainingSession.getWillOnline());
                            put("workshopSessionId", trainingSession.getWorkshopSession().getId());
                            put("workshopSessionName", trainingSession.getWorkshopSession().getName());
                        }
                    };
                    calendarEventRepository.deleteById(trainingSessionId);
                }
                return ResponseWrapper.response(data, "trainingSession canceld");
            }).orElseThrow(() -> ItemNotFoundException.builder("User").build());
    }

    @Override
    public ResponseEntity<?> getTrainingSessionIntakePrograms(CurrentUserObject currentUserObject) {
        return userRepository.findById(currentUserObject.getUserId())
            .map(user -> {
                Pageable paging = PageRequest.of(0, 1000);
                Page<IntakeProgram> ls = intakeProgramRepository
                    .findByStatusAndPeriodEndBefore(Constant.PUBLISHED.toString(), new Date(), paging);
                Page<GetIntakeProgramDto> list = ls.map(intakeProgramMapper::toGetIntakeProgramDto);
                return ResponseWrapper.response(list);
            }).orElseThrow(() -> ItemNotFoundException.builder("User").build());
    }

    @Override
    public ResponseEntity<?> getTrainingSessionAcademyRooms(CurrentUserObject currentUserObject, Long intakeProgramId) {
        return userRepository.findById(currentUserObject.getUserId())
            .map(user -> {
                Pageable paging = PageRequest.of(0, 1000);
                Page<AcademyRoom> ls = academyRoomRepository.findByIntakeProgram_IdAndStatusNotAndSessionEndAfter(
                    intakeProgramId, Constant.DRAFT.toString(), new Date(), paging);
                Page<AcademyRoomManagementDto> list = ls.map(academyRoomMapper::toAcademyRoomManagementDto);
                return ResponseWrapper.response(list);
            }).orElseThrow(() -> ItemNotFoundException.builder("User").build());
    }

    @Override
    public ResponseEntity<?> getTrainingSessionWorkshopSessions(CurrentUserObject currentUserObject,
                                                                Long academyRoomId) {
        return userRepository.findById(currentUserObject.getUserId())
            .map(user -> {
                Pageable paging = PageRequest.of(0, 1000);
                Page<WorkshopSession> ls = workshopSessionRepository.findByAcademyRoom_IdAndStatusNot(academyRoomId,
                    Constant.DRAFT.toString(), paging);
                Page<WorkshopSessionManagementDto> list = ls.map(workshopSessionMapper::toWorkshopSessionManagementDto);
                return ResponseWrapper.response(list);
            }).orElseThrow(() -> ItemNotFoundException.builder("User").build());
    }

    @Override
    public ResponseEntity<?> notifyCoach(CurrentUserObject currentUserObject,
                                         FeedbackNotifyCoachDto feedbackNotifyCoachDto) {
        return userRepository.findById(currentUserObject.getUserId())
            .map(user -> {
                notificationService.notifyCoach(user, feedbackNotifyCoachDto);
                return ResponseWrapper.response("Notified");
            }).orElseThrow(() -> ItemNotFoundException.builder("User").build());
    }
}
