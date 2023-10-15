package com.stc.inspireu.controllers;

import com.stc.inspireu.annotations.Authorize;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.models.OneToOneMeeting;
import com.stc.inspireu.models.Role;
import com.stc.inspireu.services.ManagementService;
import com.stc.inspireu.services.RoleService;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/${api.version}/management")
@RequiredArgsConstructor
public class ManagementController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final RoleService roleService;

    private final ManagementService managementService;

    @PostMapping("registration")
    public ResponseEntity<Object> managementRegistration(
        @Valid @RequestBody ManagementRegistrationDto managementRegistrationDto, BindingResult bindingResult) {
        LOGGER.debug("managementRegistration");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {

            if (managementRegistrationDto.getPassword().equals(managementRegistrationDto.getConfirmPassword())) {

                return managementService._managementRegistration(managementRegistrationDto);

            } else {
                return ResponseWrapper.response400(
                    managementRegistrationDto.getConfirmPassword() + " should be equal to password",
                    "confirmPassword");
            }

        }
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PostMapping("inviteUser")
    public ResponseEntity<Object> inviteUser(@Valid @RequestBody InviteUserDto inviteUserDto,
                                             BindingResult bindingResult) throws MessagingException {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            Object obj = managementService.inviteUser(inviteUserDto);
            if (obj != null) {
                return ResponseWrapper.response(null,
                    "invitaion send to " + inviteUserDto.getRegistratedEmailAddress());
            } else {
                return ResponseWrapper.response(inviteUserDto.getRegistratedEmailAddress() + " already registered",
                    HttpStatus.CONFLICT);
            }
        }

    }

    @GetMapping("inviteUser/roles")
    public ResponseEntity<Object> getRoles(HttpServletRequest httpServletRequest) {
        List<String> excludedRoles = new ArrayList<String>();
        excludedRoles.add(RoleName.ROLE_SUPER_ADMIN);
        excludedRoles.add(RoleName.ROLE_STARTUPS_MEMBER);
        excludedRoles.add(RoleName.ROLE_STARTUPS_ADMIN);
        List<Role> ls = roleService.findRolesExcludedGivenList(excludedRoles);
        return ResponseWrapper.response(ls);
    }

    @PostMapping("feedbacks/{feedbackId}")
    public ResponseEntity<Object> createFeedbacks(HttpServletRequest httpServletRequest,
                                                  @PathVariable Long feedbackId,
                                                  @Valid @RequestBody PostFeedbackDto postFeedbackDto,
                                                  BindingResult bindingResult) throws MessagingException {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");

            Object obj = managementService.createFeedbacks(currentUserObject, postFeedbackDto, feedbackId);
            if (obj != null) {
                return ResponseWrapper.response("feedback created", HttpStatus.OK);
            }
            return ResponseWrapper.response("invalid workshopSessionId", "workshopSessionId", HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("feedbacks/notifyCoach")
    public ResponseEntity<?> notifyCoach(HttpServletRequest httpServletRequest,
                                         @Valid @RequestBody FeedbackNotifyCoachDto feedbackNotifyCoachDto,
                                         BindingResult bindingResult) throws MessagingException {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return managementService.notifyCoach(currentUserObject, feedbackNotifyCoachDto);
    }

    @PutMapping("feedbacks/{feedbackId}")
    public ResponseEntity<?> updateFeedbacks(HttpServletRequest httpServletRequest,
                                             @PathVariable Long feedbackId,
                                             @Valid @RequestBody PutFeedbackDto putFeedbackDto,
                                             BindingResult bindingResult) throws MessagingException {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");

            return managementService.updateFeedbacks(currentUserObject, putFeedbackDto, feedbackId);
        }

    }

    @GetMapping("startups/{startupId}/workshopSessions/{workshopSessionId}/feedbacks/{feedbackId}")
    public ResponseEntity<Object> getStartupFeedback(HttpServletRequest httpServletRequest,
                                                     @PathVariable Long startupId,
                                                     @PathVariable Long workshopSessionId,
                                                     @PathVariable Long feedbackId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object obj = managementService.getStartupFeedback(currentUserObject, startupId, workshopSessionId, feedbackId);

        return ResponseWrapper.response(obj);

    }


    @PostMapping("meetings/one2One")
    public ResponseEntity<Object> createOne2OneMeeting(HttpServletRequest httpServletRequest,
                                                       @Valid @RequestBody PostOne2OneMeetingManagementDto one2OneMeetingDto,
                                                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");

            if (Boolean.TRUE.equals(!one2OneMeetingDto.getWillOnline())
                && (one2OneMeetingDto.getMeetingRoom() == null || one2OneMeetingDto.getMeetingRoom().isEmpty()
                || one2OneMeetingDto.getMeetingRoom().trim().isEmpty())) {
                return ResponseWrapper.response("invalid meetingRoom", "meetingRoom", HttpStatus.BAD_REQUEST);
            }

            return managementService.createOne2OneMeeting(currentUserObject, one2OneMeetingDto);

        }
    }

    @PutMapping("meetings/{meetingId}/one2One")
    public ResponseEntity<Object> updateOne2OneMeeting(HttpServletRequest httpServletRequest,
                                                       @PathVariable Long meetingId,
                                                       @Valid @RequestBody PutOne2OneMeetingManagementDto one2OneMeetingDto,
                                                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {

            if (one2OneMeetingDto.getIsAccept() && one2OneMeetingDto.getIsNewTiming()) {
                return ResponseWrapper.response("invalid parameters", "acceptAndNewTiming", HttpStatus.BAD_REQUEST);
            }

            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");

            Object d = managementService.updateOne2OneMeeting(currentUserObject, one2OneMeetingDto, meetingId);
            if (d instanceof String) {
                return ResponseWrapper.response400((String) d, "sessionStart");
            }

            OneToOneMeeting oneToOneMeeting = (OneToOneMeeting) d;

            Map<String, Object> data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("oneToOneMeetingId", oneToOneMeeting.getId());
                    put("meetingLink", oneToOneMeeting.getMeetingLink());
                    put("sessionEnd", oneToOneMeeting.getSessionEnd().toInstant().toEpochMilli());
                    put("sessionStart", oneToOneMeeting.getSessionStart().toInstant().toEpochMilli());
                    put("meetingName", oneToOneMeeting.getMeetingName());
                    put("trainer", oneToOneMeeting.getTrainer().getAlias());
                    put("traineId", oneToOneMeeting.getTrainer().getId());
                    put("status", oneToOneMeeting.getInvitationStatus());
                }
            };

            return ResponseWrapper.response(data);
        }
    }

    @GetMapping("meetings/{meetingId}/one2One")
    public ResponseEntity<Object> getOne2OneMeeting(HttpServletRequest httpServletRequest,
                                                    @PathVariable Long meetingId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = managementService.getOne2OneMeeting(currentUserObject, meetingId);

        return ResponseWrapper.response(data);
    }

    @DeleteMapping("meetings/{meetingId}/one2One")
    public ResponseEntity<?> deleteOne2OneMeeting(HttpServletRequest httpServletRequest, @PathVariable Long meetingId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return managementService.deleteOne2OneMeeting(currentUserObject, meetingId);

    }

    @GetMapping("{intakeProgramId}/meetings/one2One")
    public ResponseEntity<Object> getAllOne2OneMeeting(HttpServletRequest httpServletRequest,
                                                       @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = managementService.getAllOne2OneMeeting(currentUserObject, intakeProgramId);

        return ResponseWrapper.response(data);
    }

    @PostMapping("meetings/trainingSessions")
    public ResponseEntity<?> createTrainingSession(HttpServletRequest httpServletRequest,
                                                   @Valid @RequestBody PostTrainingSessionDto postTrainingSession,
                                                   BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return managementService.createTrainingSession(currentUserObject, postTrainingSession);
    }


    @GetMapping("meetings/trainingSessions/{trainingSessionId}")
    public ResponseEntity<?> getTrainingSession(HttpServletRequest httpServletRequest,
                                                @PathVariable Long trainingSessionId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return managementService.getTrainingSession(currentUserObject, trainingSessionId);

    }

    @DeleteMapping("meetings/trainingSessions/{trainingSessionId}")
    public ResponseEntity<?> cancelTrainingSession(HttpServletRequest httpServletRequest,
                                                   @PathVariable Long trainingSessionId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return managementService.cancelTrainingSession(currentUserObject, trainingSessionId);

    }


    @GetMapping("meetings/trainingSessions/intakePrograms/{intakeProgramId}/academyRooms")
    public ResponseEntity<?> getTrainingSessionAcademyRooms(HttpServletRequest httpServletRequest,
                                                            @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return managementService.getTrainingSessionAcademyRooms(currentUserObject, intakeProgramId);

    }

    @GetMapping("meetings/trainingSessions/academyRooms/{academyRoomId}/workshopSessions")
    public ResponseEntity<?> getTrainingSessionWorkshopSessions(HttpServletRequest httpServletRequest,
                                                                @PathVariable Long academyRoomId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return managementService.getTrainingSessionWorkshopSessions(currentUserObject, academyRoomId);

    }

}
