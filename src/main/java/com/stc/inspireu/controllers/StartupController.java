package com.stc.inspireu.controllers;

import com.stc.inspireu.annotations.Authorize;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.models.OneToOneMeeting;
import com.stc.inspireu.models.Slot;
import com.stc.inspireu.models.User;
import com.stc.inspireu.services.StartupService;
import com.stc.inspireu.services.UserService;
import com.stc.inspireu.utils.JwtUtil;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/${api.version}/startups")
@RequiredArgsConstructor
public class StartupController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final StartupService startupService;

    private final UserService userService;

    private final JwtUtil jwtUtil;

    private final Utility utility;

    @PostMapping("registration")
    public ResponseEntity<Object> startupsRegistration(
        @Valid @RequestBody StartupsRegistrationDto startupsRegistrationDto, BindingResult bindingResult) {
        LOGGER.debug("startupsRegistration");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {

            if (startupsRegistrationDto.getPassword().equals(startupsRegistrationDto.getConfirmPassword())) {

                Map<String, Object> claims = jwtUtil
                    .getClaimsFromGenericToken(startupsRegistrationDto.getInviteToken());

                if (claims != null
                    && startupsRegistrationDto.getRegistratedEmailAddress().equals(claims.get("email"))) {

                    User user = userService.findByEmailAndInvitationStatus(
                        startupsRegistrationDto.getRegistratedEmailAddress(), Constant.INVITAION_SEND.toString());

                    if (user != null) {

                        if (user.getInviteToken().equals(startupsRegistrationDto.getInviteToken())) {

                            startupService.startupsRegistration(startupsRegistrationDto, claims, user);

                            Map<String, String> data = new HashMap<String, String>() {
                                private static final long serialVersionUID = 1L;

                                {
                                    put("startupName", startupsRegistrationDto.getStartupName());
                                    put("memberName", startupsRegistrationDto.getMemberName());
                                    put("jobTitle", startupsRegistrationDto.getJobTitle());
                                    put("phoneNumber", startupsRegistrationDto.getPhoneNumber());
                                    put("programIncubating", startupsRegistrationDto.getProgramIncubating());
                                    put("registratedEmailAddress",
                                        startupsRegistrationDto.getRegistratedEmailAddress());
                                }
                            };

                            return ResponseWrapper.response(data, "startups registration success");
                        } else {
                            return ResponseWrapper.response400("invalid startup registration link", "inviteToken");
                        }

                    } else {
                        return ResponseWrapper.response(
                            startupsRegistrationDto.getRegistratedEmailAddress() + " already registred",
                            HttpStatus.CONFLICT);
                    }

                } else {
                    return ResponseWrapper.response400("invalid startup registration link", "inviteToken");
                }

            } else {
                return ResponseWrapper.response400(
                    startupsRegistrationDto.getConfirmPassword() + " should be equal to password",
                    "confirmPassword");
            }

        }
    }

    @Authorize(roles = {RoleName.ROLE_STARTUPS_ADMIN})
    @PostMapping("inviteMembers")
    public ResponseEntity<Object> inviteMembers(HttpServletRequest httpServletRequest,
                                                @Valid @RequestBody InviteStartupMembersDto inviteStartupMembersDto,
                                                BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");

            startupService.inviteMembers(currentUserObject, inviteStartupMembersDto);
            return ResponseWrapper.response(null, "invitaion send");
        }

    }

    @GetMapping("roles")
    public ResponseEntity<Object> getStartupRoles(HttpServletRequest httpServletRequest) {
        List<RoleDto> roleDtos = startupService.getRoles(false);
        return ResponseWrapper.response(roleDtos);
    }

    @GetMapping("members")
    public ResponseEntity<Object> getMembers(HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        List<UserDto> userDtos = startupService.getMembers(currentUserObject);
        return ResponseWrapper.response(userDtos);
    }

    @GetMapping("members/{memberId}")
    public ResponseEntity<Object> getMember(HttpServletRequest httpServletRequest, @PathVariable Long memberId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        UserDto userDto = startupService.getMember(currentUserObject, memberId);
        return ResponseWrapper.response(userDto);
    }

    @Authorize(roles = {RoleName.ROLE_STARTUPS_ADMIN})
    @DeleteMapping("members/{memberId}")
    public ResponseEntity<?> deleteMember(HttpServletRequest httpServletRequest, @PathVariable Long memberId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        return startupService.deleteMember(currentUserObject, memberId);

    }

    @PutMapping("members/{memberId}")
    public ResponseEntity<Object> updateMember(HttpServletRequest httpServletRequest,
                                               @RequestBody EditStartupMemberDto editStartupMemberDto,
                                               @PathVariable Long memberId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        User usr = startupService.updateMember(currentUserObject, editStartupMemberDto, memberId);

        if (usr != null) {
            return ResponseWrapper.response(null, "member updated " + memberId);
        }

        return ResponseWrapper.response("invalid startup member", "memberId", HttpStatus.NOT_FOUND);

    }

    @GetMapping("myProfile")
    public ResponseEntity<Object> getMyProfile(HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        GetStartupProfileDto getStartupProfileDto = startupService.getMyProfile(currentUserObject);
        return ResponseWrapper.response(getStartupProfileDto);
    }

    @PutMapping(value = "myProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateMyProfile(HttpServletRequest httpServletRequest,
                                                  @Valid @ModelAttribute StartupProfileDto startupProfileDto,
                                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {

            Map<String, Object> fileCheck = utility.checkFile(startupProfileDto.getProfilePic());

            if (!(boolean) fileCheck.get("isAllow")) {
                return ResponseWrapper.response400((String) fileCheck.get("error"), "files");
            }

            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");

            User user = startupService.updateMyProfile(currentUserObject, startupProfileDto);

            if (user == null) {
                return ResponseWrapper.response400("email is already registered", "email");
            }

            Map<String, Object> claims = new HashMap<String, Object>();
            claims.put("userId", user.getId());
            claims.put("email", user.getEmail());

            String jwt = jwtUtil.generateAuthToken(claims);

            Map<String, String> data = new HashMap<String, String>() {
                private static final long serialVersionUID = 1L;

                {
                    put("startupName", startupProfileDto.getStartupName());
                    put("memberName", startupProfileDto.getMemberName());
                    put("phoneNumber", startupProfileDto.getPhoneNumber());
                    put("programIncubating", startupProfileDto.getProgramIncubating());
                    put("registratedEmailAddress", startupProfileDto.getRegistratedEmailAddress());
                }
            };

            if (startupProfileDto.getRegistratedEmailAddress() != null) {
                data.put("newAuthToken", jwt);
            }

            return ResponseWrapper.response(data, "startup my profile update success");

        }

    }

    @PutMapping(value = "companyProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateCompanyProfile(HttpServletRequest httpServletRequest,
                                                       @ModelAttribute StartupCompanyProfileDto startupCompanyProfileDto,
                                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {

            Map<String, Object> fileCheck = utility.checkFile(startupCompanyProfileDto.getCompanyPic());

            if (!(boolean) fileCheck.get("isAllow")) {
                return ResponseWrapper.response400((String) fileCheck.get("error"), "files");
            }

            return ResponseWrapper.response("profile card updated");
        }
    }

    @GetMapping("companyProfile")
    public ResponseEntity<Object> getCompanyProfile(HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        StartupCompanyProfileDto startupCompanyProfileDto = startupService.getCompanyProfile(currentUserObject);
        return ResponseWrapper.response(startupCompanyProfileDto);
    }

    @GetMapping("calendarEvents/{year}/{month}")
    public ResponseEntity<Object> getCalendarEvents(HttpServletRequest httpServletRequest,
                                                    @PathVariable Integer month,
                                                    @PathVariable Integer year,
                                                    @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<CalendarEventDto> calendarEventDtos = startupService
            .getCalendarEventsByStartupIdAndMonth(currentUserObject, month, year, timezone);
        return ResponseWrapper.response(calendarEventDtos);

    }

    @GetMapping("calendarEvents/{year}/{month}/{day}")
    public ResponseEntity<Object> getCalendarEventsByDate(HttpServletRequest httpServletRequest,
                                                          @PathVariable Integer day,
                                                          @PathVariable Integer month,
                                                          @PathVariable Integer year,
                                                          @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<CalendarEventDto> calendarEventDtos = startupService.getCalendarEventsByStartupIdAndDay(currentUserObject,
            day, month, year, timezone);
        return ResponseWrapper.response(calendarEventDtos);

    }

    @PostMapping("slots")
    public ResponseEntity<Object> bookSlot(HttpServletRequest httpServletRequest,
                                           @Valid @RequestBody BookSlotDto bookSlotDto,
                                           BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");

            Object d = startupService.bookSlot(currentUserObject, bookSlotDto);

            if (d instanceof String) {
                return ResponseWrapper.response400((String) d, "sessionStart");
            }

            Slot slot = (Slot) d;

            Map<String, Object> data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("slotId", slot.getId());
                    put("qrCodeId", slot.getQrCodeId());
                    put("sessionEnd", slot.getSessionEnd().toInstant().toEpochMilli());
                    put("sessionStart", slot.getSessionStart().toInstant().toEpochMilli());
                    put("reason", slot.getReason());
                }
            };

            return ResponseWrapper.response(data);
        }
    }

    @DeleteMapping("slots/{slotId}")
    public ResponseEntity<Object> cancelSlot(HttpServletRequest httpServletRequest, @PathVariable Long slotId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Slot slot = startupService.cancelSlot(currentUserObject, slotId);

        if (slot != null) {

            Map<String, Object> data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("slotId", slot.getId());
                }
            };

            return ResponseWrapper.response(data);
        } else {
            return ResponseWrapper.response(slotId + " not found", "slotId", HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping("meetings/one2One")
    public ResponseEntity<Object> createOne2OneMeeting(HttpServletRequest httpServletRequest,
                                                       @Valid @RequestBody PostOne2OneMeetingDto one2OneMeetingDto,
                                                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");

            Object d = startupService.createOne2OneMeeting(currentUserObject, one2OneMeetingDto);

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

}
