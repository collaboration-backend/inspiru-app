package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.beans.MailMetadata;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.mappers.CalendarEventMapper;
import com.stc.inspireu.mappers.RoleMapper;
import com.stc.inspireu.mappers.UserMapper;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.services.StartupService;
import com.stc.inspireu.utils.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StartupServiceImpl implements StartupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${inspireu.fileDir}")
    private String inspireuFileDir;

    @Value("${ui.url}")
    private String uiUrl;

    @Value("${ui.qrCodePath}")
    private String uiQrCodePath;

    @Value("${ui.startupMemberInvitationPath}")
    private String startupMemberInvitationPath;
    private final KeyValueRepository keyValueRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final RoleRepository roleRepository;
    private final StartupRepository startupRepository;
    private final OneToOneMeetingRepository oneToOneMeetingRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;
    private final Utility utility;
    private final SlotRepository slotRepository;
    private final FileAdapter fileAdapter;
    private final IntakeProgramRepository intakeProgramRepository;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final CalendarEventMapper calendarEventMapper;

    @Override
    @Transactional
    public Object startupsRegistration(StartupsRegistrationDto startupsRegistrationDto, Map<String, Object> claims,
                                       User user) {
        Role role = roleRepository.findByRoleName(claims.get("roleName").toString());
        if (claims.containsKey("invitedStartup")) {
            setValuesToUser(user, startupsRegistrationDto, role);
            Optional<Startup> s = startupRepository.findById(((Number) claims.get("invitedStartup")).longValue());
            if (s.isPresent()) {
                s.get().setIsReal(true);
                startupRepository.save(s.get());
                KeyValue totalStartups = keyValueRepository
                    .findByKeyName(Constant.TOTAL_STARTUPS_INCUBATED.toString());
                if (totalStartups != null) {
                    String count = totalStartups.getValueName();
                    int c = Integer.parseInt(count);
                    c = c + 1;
                    totalStartups.setValueName(String.valueOf(c));
                    keyValueRepository.save(totalStartups);
                }
            }
            userRepository.save(user);
        } else {
            Startup startup = new Startup();
            startup.setIsReal(true);
            KeyValue totalStartups = keyValueRepository.findByKeyName(Constant.TOTAL_STARTUPS_INCUBATED.toString());
            if (totalStartups != null) {
                String count = totalStartups.getValueName();
                int c = Integer.parseInt(count);
                c = c + 1;
                totalStartups.setValueName(String.valueOf(c));
                keyValueRepository.save(totalStartups);
            }
            startup.setStartupName(startupsRegistrationDto.getStartupName());
            if (Objects.nonNull(startupsRegistrationDto.getIntakeNumber()) && startupsRegistrationDto.getIntakeNumber() != 0) {
                Optional<IntakeProgram> ip = intakeProgramRepository
                    .findById(Long.valueOf(startupsRegistrationDto.getIntakeNumber()));
                ip.ifPresent(startup::setIntakeProgram);
            }
            startupRepository.save(startup);
            setValuesToUser(user, startupsRegistrationDto, role);
            userRepository.save(user);
        }
        MailMetadata mailMetadata = new MailMetadata();
        Map<String, Object> props = new HashMap<>();
        props.put("toMail", startupsRegistrationDto.getRegistratedEmailAddress());
        mailMetadata.setFrom("");
        mailMetadata.setTo(startupsRegistrationDto.getRegistratedEmailAddress());
        mailMetadata.setProps(props);
        mailMetadata.setSubject("User invitation mail");
        mailMetadata.setTemplateFile("user-invitation-mail");
        notificationService.sendWelcomeNotification(mailMetadata, role.getRoleName());
        notificationService.registrationNotifications(user, role.getRoleAlias());
        return null;
    }

    private void setValuesToUser(User user, StartupsRegistrationDto startupsRegistrationDto, Role role) {
        user.setPhoneCountryCodeIso2(startupsRegistrationDto.getIso2CountryCode());
        user.setPhoneDialCode(startupsRegistrationDto.getPhoneDialCode());
        user.setPhoneNumber(startupsRegistrationDto.getPhoneNumber());
        user.setJobTitle(startupsRegistrationDto.getJobTitle());
        user.setEmail(startupsRegistrationDto.getRegistratedEmailAddress());
        user.setAlias(startupsRegistrationDto.getMemberName());
        user.setRole(role);
        user.setInvitationStatus(Constant.REGISTERED.toString());
        user.setInviteToken(null);
        user.setEnableEmail(true);
        user.setEnableWeb(true);
        String hashedPassword = passwordUtil.getHashedPassword(startupsRegistrationDto.getPassword());
        user.setPassword(hashedPassword);
        if (user.getIsRemovable() == null) {
            user.setIsRemovable(true);
        }
    }

    @Override
    @Transactional
    public Object inviteMembers(CurrentUserObject currentUserObject, InviteStartupMembersDto inviteStartupMembersDto) {
        Optional<User> invitor1 = userRepository.findById(currentUserObject.getUserId());
        List<InviteStartupMemberDto> inviteStartupMembers = inviteStartupMembersDto.getMembers();
        if (invitor1.isPresent()) {
            for (InviteStartupMemberDto inviteStartupMember : inviteStartupMembers) {
                String email = inviteStartupMember.getEmail();
                Role role3 = null;
                Role rl2 = roleRepository.findByRoleName(
                    inviteStartupMember.getWillAdmin() ? RoleName.Value.ROLE_STARTUPS_ADMIN.toString()
                        : RoleName.Value.ROLE_STARTUPS_MEMBER.toString());
                if (rl2 != null) {
                    role3 = rl2;
                }
                if (role3 != null) {
                    User user = new User();
                    User user1 = userRepository.findByEmail(email);
                    Map<String, Object> claims = null;
                    String token = null;
                    if (user1 != null) {
                        if (user1.getInvitationStatus().equals(Constant.INVITAION_SEND.toString())) {
                            claims = new HashMap<>();
                            if (invitor1.get().getStartup() != null) {
                                claims.put("invitedStartup", invitor1.get().getStartup().getId());
                                claims.put("startupName", invitor1.get().getStartup().getStartupName());
                                claims.put("programName",
                                    invitor1.get().getStartup().getIntakeProgram().getProgramName());
                                claims.put("intakeNumber", invitor1.get().getStartup().getIntakeProgram().getId());
                            }
                            claims.put("email", email);
                            claims.put("roleName", role3.getRoleName());
                            claims.put("roleId", role3.getId());
                            claims.put("message", Constant.STARTUP_INVITATION.toString());
                            token = jwtUtil.genericJwtToken(claims);
                            user1.setEmail(email);
                            user1.setRole(role3);
                            user1.setInvitationStatus(Constant.INVITAION_SEND.toString());
                            user1.setInviteToken(token);
                            user1.setStartup(invitor1.get().getStartup());
                            userRepository.save(user1);
                        }
                    } else {
                        claims = new HashMap<>();
                        if (invitor1.get().getStartup() != null) {
                            claims.put("invitedStartup", invitor1.get().getStartup().getId());
                            claims.put("startupName", invitor1.get().getStartup().getStartupName());
                            claims.put("programName", invitor1.get().getStartup().getIntakeProgram().getProgramName());
                            claims.put("intakeNumber", invitor1.get().getStartup().getIntakeProgram().getId());
                        }
                        claims.put("email", email);
                        claims.put("roleName", role3.getRoleName());
                        claims.put("roleId", role3.getId());
                        claims.put("message", Constant.STARTUP_INVITATION.toString());
                        token = jwtUtil.genericJwtToken(claims);
                        user.setEmail(email);
                        user.setRole(role3);
                        user.setInvitationStatus(Constant.INVITAION_SEND.toString());
                        user.setInviteToken(token);
                        user.setStartup(invitor1.get().getStartup());
                        userRepository.save(user);
                    }
                    if (claims != null) {
                        sendInviteNotification(email, inviteStartupMembersDto.getInviteMessage(), role3.getRoleName(), token);
                    }
                }
            }
        }
        return inviteStartupMembers;
    }

    private void sendInviteNotification(String email, String message, String roleName, String token) {
        MailMetadata mailMetadata = new MailMetadata();
        Map<String, Object> props = new HashMap<>();
        String link = uiUrl + startupMemberInvitationPath + "/" + token;
        props.put("inviteLink", link);
        props.put("toMail", email);
        mailMetadata.setFrom("");
        mailMetadata.setTo(email);
        mailMetadata.setProps(props);
        mailMetadata.setSubject("User invitation mail");
        mailMetadata.setTemplateFile("user-invitation-mail");
        try {
            notificationService.sendInviteNotification(mailMetadata,
                message, roleName);
            // emailUtil.sendEmail(mailMetadata);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Transactional
    @Override
    public GetStartupProfileDto getMyProfile(CurrentUserObject currentUserObject) {
        return userRepository.findById(currentUserObject.getUserId())
            .map(user -> {
                GetStartupProfileDto getStartupProfileDto = new GetStartupProfileDto();
                if (user.getStartup() != null) {
                    getStartupProfileDto.setStartupName(user.getStartup().getStartupName());
                    if (user.getStartup().getIntakeProgram() != null) {
                        getStartupProfileDto.setIntakeProgramId(user.getStartup().getIntakeProgram().getId());
                    }
                } else {
                    getStartupProfileDto.setStartupName("");
                    getStartupProfileDto.setIntakeProgramId(0L);
                }
                getStartupProfileDto.setId(user.getId());
                getStartupProfileDto.setMemberName(user.getAlias());
                getStartupProfileDto.setRegistratedEmailAddress(user.getEmail());
                getStartupProfileDto.setPhoneNumber(user.getPhoneNumber());
                getStartupProfileDto.setPhoneCountryCodeIso2(user.getPhoneCountryCodeIso2());
                getStartupProfileDto.setPhoneDialCode(user.getPhoneDialCode());
                getStartupProfileDto.setMemberName(user.getAlias());
                getStartupProfileDto.setProfilePic(user.getProfilePic());
                return getStartupProfileDto;
            }).orElse(null);
    }

    @Override
    @Transactional
    public User updateMyProfile(CurrentUserObject currentUserObject, StartupProfileDto startupProfileDto) {
        return userRepository.findById(currentUserObject.getUserId()).map(user -> {
            String profilePicPath = "";
            if (!user.getEmail().equals(startupProfileDto.getRegistratedEmailAddress())) {
                User u = userRepository.findByEmail(startupProfileDto.getRegistratedEmailAddress());
                if (u != null) {
                    return null;
                }
            }
            if (startupProfileDto.getProfilePic() != null) {
                profilePicPath = fileAdapter.saveProfilePic(currentUserObject.getUserId(),
                    startupProfileDto.getProfilePic());
            }
            if (user.getStartup() != null) {
                Startup ele = user.getStartup();
                ele.setStartupName(startupProfileDto.getStartupName());
                startupRepository.save(ele);
                user.setStartup(ele);
            }
            user.setEmail(startupProfileDto.getRegistratedEmailAddress() != null
                ? startupProfileDto.getRegistratedEmailAddress()
                : currentUserObject.getEmail());
            user.setAlias(startupProfileDto.getMemberName());
            user.setPhoneNumber(startupProfileDto.getPhoneNumber());
            user.setPhoneCountryCodeIso2(startupProfileDto.getPhoneCountryCodeIso2());
            user.setPhoneDialCode(startupProfileDto.getPhoneDialCode());
            user.setAlias(startupProfileDto.getMemberName());
            if (Objects.nonNull(profilePicPath) && !profilePicPath.isEmpty() && startupProfileDto.getProfilePic() != null) {
                user.setProfilePic(profilePicPath);
            }
            userRepository.save(user);
            notificationService.updateProfileCardNotification(user);
            return user;
        }).orElseThrow(() -> ItemNotFoundException.builder("User").build());
    }

    @Transactional
    @Override
    public List<UserDto> getMembers(CurrentUserObject currentUserObject) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null) {
            List<User> users = userRepository.getMembers(user.get().getStartup().getId());
            return userMapper.toUserDTOList(users);
        } else {
            return Collections.emptyList();
        }
    }

    @Transactional
    @Override
    public UserDto getMember(CurrentUserObject currentUserObject, Long memberId) {
        Optional<User> user = userRepository.findById((long) currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null) {
            User user1 = userRepository.getMember(user.get().getStartup().getId(), memberId);
            return userMapper.toUserDTO(user1);
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteMember(CurrentUserObject currentUserObject, Long memberId) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null) {
            Optional<User> usr = userRepository.findById(memberId);
            if (usr.isPresent()) {
                usr.get()
                    .setInvitationStatus(usr.get().getInvitationStatus().equals(Constant.BLOCKED.toString())
                        ? Constant.REGISTERED.toString()
                        : Constant.BLOCKED.toString());
                userRepository.save(usr.get());
                return ResponseWrapper.response(null,
                    usr.get().getInvitationStatus().equals(Constant.BLOCKED.toString()) ? "Successfully Blocked"
                        : "Successfully Unblocked");
            }
        }
        return ResponseWrapper.response400("Invalid user", "UserId");
    }

    @Transactional
    @Override
    public User updateMember(CurrentUserObject currentUserObject, EditStartupMemberDto editStartupMemberDto,
                             Long memberId) {
        return userRepository.findById(currentUserObject.getUserId())
            .map(usr -> {
                if (Objects.nonNull(usr.getStartup())) {
                    User user = userRepository.getMember(usr.getStartup().getId(), memberId);
                    Role role = null;
                    Optional<Role> rl = roleRepository.findById(editStartupMemberDto.getRoleId());
                    if (rl.isPresent()) {
                        role = rl.get();
                    }
                    if (role != null) {
                        user.setAlias(editStartupMemberDto.getAlias());
                        user.setEmail(editStartupMemberDto.getEmail());
                        user.setPhoneNumber(editStartupMemberDto.getPhoneNumber());
                        user.setPhoneDialCode(editStartupMemberDto.getPhoneDialCode());
                        user.setPhoneCountryCodeIso2(editStartupMemberDto.getPhoneCountryCodeIso2());
                        user.setRole(role);
                        userRepository.save(user);
                    }
                }
                return usr;
            }).orElse(null);
    }

    @Override
    @Transactional
    public StartupCompanyProfileDto getCompanyProfile(CurrentUserObject currentUserObject) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(()->ItemNotFoundException.builder("User").build());
        StartupCompanyProfileDto startupCompanyProfileDto = new StartupCompanyProfileDto();
        if (user.getStartup() != null) {
            startupCompanyProfileDto.setStartupName(user.getStartup().getStartupName());
            startupCompanyProfileDto.setCompanyProfile(user.getStartup().getCompanyProfile());
            startupCompanyProfileDto.setCompanyDescription(user.getStartup().getCompanyDescription());
            startupCompanyProfileDto.setRevenueModel(user.getStartup().getRevenueModel());
            startupCompanyProfileDto.setSegment(user.getStartup().getSegment());
            startupCompanyProfileDto.setStatus(user.getStartup().getStatus());
            startupCompanyProfileDto.setCompetitor(user.getStartup().getCompetitor());
            startupCompanyProfileDto.setRegistartionJsonForm(user.getStartup().getRegistrationJsonForm());
            startupCompanyProfileDto.setProfileCardJsonForm(user.getStartup().getProfileCardJsonForm());
            startupCompanyProfileDto.setProfileInfoJson(user.getStartup().getProfileInfoJson());
        }
        return startupCompanyProfileDto;
    }

    @Override
    @Transactional
    public Object bookSlot(CurrentUserObject currentUserObject, BookSlotDto bookSlotDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(()->ItemNotFoundException.builder("User").build());
        Slot slot = new Slot();
        if (user.getStartup() != null) {
//            List <ProjectId> ls = slotRepository.findByStartup_IdAndSessionEndGreaterThanEqual(user.get().getStartup().getId(), new Date(bookSlotDto.getSessionStart().longValue()));
            List<Slot> ls = slotRepository.getStartupScheduledSlots(user.getStartup().getId(),
                new Date(bookSlotDto.getSessionStart()),
                new Date(bookSlotDto.getSessionEnd()));
            if (ls.size() > 0) {
                return "already a slot booked in this time period";
            }
//            String qrCode = uiUrl + uiQrCodePath + "/" + utility.qrAlphaNumeric();
            String qrCode = utility.qrAlphaNumeric();
            slot.setReason(bookSlotDto.getReason());
            slot.setQrCodeId(qrCode);
            slot.setSessionStart(new Date(bookSlotDto.getSessionStart()));
            slot.setSessionEnd(new Date(bookSlotDto.getSessionEnd()));
            slot.setUser(user);
            slot.setStartup(user.getStartup());
            slot.setStatus(Constant.BOOKED.toString());
            slot.setVenue("");
            Slot slt = slotRepository.save(slot);
            CalendarEvent calendarEvent = new CalendarEvent();
            calendarEvent.setSessionStart(new Date(bookSlotDto.getSessionStart()));
            calendarEvent.setSessionEnd(new Date(bookSlotDto.getSessionEnd()));
            calendarEvent.setStartup(user.getStartup());
            calendarEvent.setSlot(slt);
            calendarEventRepository.save(calendarEvent);
        }
        return slot;
    }

    @Override
    @Transactional
    public Slot cancelSlot(CurrentUserObject currentUserObject, Long slotId) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null) {
            Slot slt = slotRepository.findByStartupIdAndSlotId(user.get().getStartup().getId(), slotId);
            if (slt != null) {
                calendarEventRepository.removeBySlotIdAndStartup(slotId, user.get().getStartup().getId());
                slotRepository.removeBySlotIdAndStartup(slotId, user.get().getStartup().getId());
            }
            return slt;
        } else {
            return null;
        }

    }

    @Override
    @Transactional
    public Object createOne2OneMeeting(CurrentUserObject currentUserObject, PostOne2OneMeetingDto one2OneMeetingDto) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        OneToOneMeeting oneToOneMeeting = new OneToOneMeeting();
        Optional<User> trainer = userRepository.findById(
            one2OneMeetingDto.getTrainerId() != null ? one2OneMeetingDto.getTrainerId() : (long) 0);
        if (user.isPresent() && user.get().getStartup() != null && trainer.isPresent()) {
            List<OneToOneMeeting> ls;
            List<OneToOneMeeting> ls2;
            ls = oneToOneMeetingRepository.getByStartupScheduledMeetings(user.get().getStartup().getId(),
                new Date(one2OneMeetingDto.getSessionStart()),
                new Date(one2OneMeetingDto.getSessionEnd()));
            if (ls.size() > 0) {
                return "trying to schedule duplicate one2one";
            } else {
                // check trainer's oneToOneMeeting for selected date range
                ls2 = oneToOneMeetingRepository.getByTrainerScheduledMeetings(trainer.get().getId(),
                    new Date(one2OneMeetingDto.getSessionStart()),
                    new Date(one2OneMeetingDto.getSessionEnd()));
                if (ls2.size() > 0) {
                    return "trainer is not available for the selected one2one meeting session";
                }
            }
            oneToOneMeeting.setDescription(one2OneMeetingDto.getDescription());
            oneToOneMeeting.setMeetingName(one2OneMeetingDto.getMeetingName());
            oneToOneMeeting.setSessionStart(new Date(one2OneMeetingDto.getSessionStart()));
            oneToOneMeeting.setSessionEnd(new Date(one2OneMeetingDto.getSessionEnd()));
            // oneToOneMeeting.setTrainer(one2OneMeetingDto.getTrainer());
            oneToOneMeeting.setTrainer(trainer.get());
            oneToOneMeeting.setWillOnline(one2OneMeetingDto.getWillOnline());
            oneToOneMeeting.setStartup(user.get().getStartup());
            oneToOneMeeting.setUser(user.get());
            oneToOneMeeting.setInvitationStatus(Constant.PENDING.toString());
            oneToOneMeeting.setMeetingLink(one2OneMeetingDto.getMeetingRoomORLink());
            oneToOneMeetingRepository.save(oneToOneMeeting);
            CalendarEvent calendarEvent = new CalendarEvent();
            calendarEvent.setSessionStart(new Date(one2OneMeetingDto.getSessionStart()));
            calendarEvent.setSessionEnd(new Date(one2OneMeetingDto.getSessionEnd()));
            calendarEvent.setStartup(user.get().getStartup());
            calendarEvent.setOneToOneMeeting(oneToOneMeeting);
            calendarEventRepository.save(calendarEvent);
            notificationService.create121MeetingNotificationBySartup(user.get(), trainer.get(), user.get().getStartup(),
                one2OneMeetingDto.getSessionStart());
        }
        return oneToOneMeeting;
    }

    @Transactional
    @Override
    public List<RoleDto> getRoles(Boolean b) {
        List<Role> rls = roleRepository.findAllStartupRoles(b);
        return roleMapper.toRoleDTOList(rls);
    }

    @Override
    @Transactional
    public List<CalendarEventDto> getCalendarEventsByStartupIdAndMonth(CurrentUserObject currentUserObject,
                                                                       Integer month, Integer year, String timezone) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null) {
            Map<String, Date> startAndEnd = utility.atStartAndEndOfMonthWithTz(month, year, timezone);
            List<CalendarEvent> calendarEvents = calendarEventRepository.getCalendarEventsByStartupIdAndMonth(
                user.get().getStartup().getId(), startAndEnd.get("start"), startAndEnd.get("end"));
            List<CalendarEvent> calendarEvents2 = calendarEventRepository.getAllCalendarEventsByStartupIdAndMonth(
                user.get().getStartup().getIntakeProgram().getId(), startAndEnd.get("start"),
                startAndEnd.get("end"));
            List<CalendarEvent> newList = Stream.concat(calendarEvents.stream(), calendarEvents2.stream())
                .collect(Collectors.toList());
            return calendarEventMapper.toCalendarEventDtoList(newList);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public List<CalendarEventDto> getCalendarEventsByStartupIdAndDay(CurrentUserObject currentUserObject, Integer day,
                                                                     Integer month, Integer year, String timezone) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null) {
            Map<String, Date> startAndEnd = utility.atStartAndEndOfDayWithTz(day, month, year, timezone);
            List<CalendarEvent> calendarEvents = calendarEventRepository.getCalendarEventsByStartupIdAndDay(
                user.get().getStartup().getId(), startAndEnd.get("start"), startAndEnd.get("end"));
            List<CalendarEvent> calendarEvents2 = calendarEventRepository.getDayCalendarEventsByStartupIdAndMonth(
                user.get().getStartup().getIntakeProgram().getId(), startAndEnd.get("start"));
            List<CalendarEvent> newList = Stream.concat(calendarEvents.stream(), calendarEvents2.stream())
                .collect(Collectors.toList());
            return calendarEventMapper.toCalendarEventDtoList(newList);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public Startup updateStartupCompanyProfile(CurrentUserObject currentUserObject,
                                               StartupCompanyProfileDto startupCompanyProfileDto) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        Startup ele = null;
        String companyPicPath = null;
        if (user.isPresent() && user.get().getStartup() != null) {
            ele = user.get().getStartup();
            if (startupCompanyProfileDto.getCompanyPic() != null) {
                companyPicPath = fileAdapter.saveCompanyPic(user.get().getStartup().getId(),
                    startupCompanyProfileDto.getCompanyPic());
            }
            ele.setProfileCardJsonForm((startupCompanyProfileDto.getProfileCardJsonForm() != null
                && !startupCompanyProfileDto.getProfileCardJsonForm().trim().isEmpty())
                ? startupCompanyProfileDto.getProfileCardJsonForm()
                : "");
            if (companyPicPath != null) {
                ele.setCompanyPic(companyPicPath);
            }
            startupRepository.save(ele);
        }
        return ele;
    }
}
