package com.stc.inspireu.services.impl;

import com.stc.inspireu.authorization.Roles;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.beans.MailMetadata;
import com.stc.inspireu.dtos.FeedbackNotifyCoachDto;
import com.stc.inspireu.dtos.GetEmailTemplateDto2;
import com.stc.inspireu.dtos.NotificationIdDto;
import com.stc.inspireu.dtos.PostMarkCardNotificationDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.enums.EmailKey;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.jpa.projections.ProjectNotification;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.EmailTemplateService;
import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.utils.EmailUtil;
import com.stc.inspireu.utils.NotificationKeywords;
import com.stc.inspireu.utils.RoleName;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Utility utility;
    private final EmailUtil emailUtil;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final NotifiedUserRepository notifiedUserRepository;
    private final AcademyRoomRepository academyRoomRepository;
    private final IntakeProgramSubmissionRepository intakeProgramSubmissionRepository;
    private final EmailTemplateService emailTemplateService;

    @Transactional
    @Override
    public Page<ProjectNotification> getStartupNotifications(CurrentUserObject currentUserObject, String filterBy,
                                                             Pageable paging) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<ProjectNotification> ls = Page.empty();
        if (user.getEnableWeb() != null && user.getEnableWeb().equals(true)) {
            Set<Long> notifiedIds = notifiedUserRepository.getNotifiedAlertByUserId(user.getId());
            if (notifiedIds.isEmpty()) {
                notifiedIds.add(Long.MAX_VALUE);
            }
            if (user.getRole().getRoleName().equals(RoleName.ROLE_STARTUPS_ADMIN)) {
                if (user.getStartup() != null) {
                    ls = notificationRepository.startupAdminAlerts(user.getStartup().getId(),
                        user.getId(), NotificationKeywords.user_only, notifiedIds, paging);
                }
            } else {
                if (user.getStartup() != null) {
                    ls = notificationRepository.startupMemberAlerts(user.getStartup().getId(),
                        user.getId(), NotificationKeywords.sensitive, NotificationKeywords.user_only,
                        notifiedIds, paging);
                }
            }
        }
        return ls;
    }

    @Transactional
    @Override
    public Page<ProjectNotification> getManagementNotifications(CurrentUserObject currentUserObject, String filterBy,
                                                                Pageable paging) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<ProjectNotification> ls = Page.empty();
        if (user.getEnableWeb() != null && user.getEnableWeb().equals(true)) {
            Set<Long> notifiedIds = notifiedUserRepository.getNotifiedAlertByUserId(user.getId());
            if (notifiedIds.isEmpty()) {
                notifiedIds.add(Long.MAX_VALUE);
            }
            if (user.getRole().getRoleName().equals(RoleName.ROLE_SUPER_ADMIN)) {
                ls = notificationRepository.superAdminAlerts(NotificationKeywords.user_only, notifiedIds, paging);
            } else {
                ls = notificationRepository.managementMemberAlerts(user.getId(),
                    NotificationKeywords.user_only, notifiedIds, paging);
            }
        }
        return ls;
    }

    @Transactional
    @Override
    public void markAsRead(CurrentUserObject currentUserObject, NotificationIdDto notificationIdDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        for (Long id : notificationIdDto.getNotificationIds()) {
            markAsNotified(id, user.getId());
        }
    }

    void markAsNotified(Long notificationId, Long userId) {
        NotifiedUser notifiedUser = new NotifiedUser();
        notifiedUser.setNotificationId(notificationId);
        notifiedUser.setUserId(userId);
        notifiedUserRepository.save(notifiedUser);
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void sendInviteNotification(MailMetadata mailMetadata, String inviteMessage, String roleName) {
        if (roleName.equals(RoleName.Value.ROLE_STARTUPS_MEMBER.toString())) {
            composeInviteMail(mailMetadata, EmailKey.invite_startup_member.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_STARTUPS_ADMIN.toString())) {
            composeInviteMail(mailMetadata, EmailKey.invite_startup_admin.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_MANAGEMENT_TEAM_ADMIN.toString())) {
            composeInviteMail(mailMetadata, EmailKey.invite_management_admin.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_MANAGEMENT_TEAM_MEMBER.toString())) {
            composeInviteMail(mailMetadata, EmailKey.invite_management_member.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_NON_STC_JUDGES.toString())) {
            composeInviteMail(mailMetadata, EmailKey.invite_non_stc_judge.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_STC_JUDGES.toString())) {
            composeInviteMail(mailMetadata, EmailKey.invite_stc_judge.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_COACHES_AND_TRAINERS.toString())) {
            composeInviteMail(mailMetadata, EmailKey.invite_coachs_and_trainers.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_EXISTING_STARTUPS.toString())) {
            composeInviteMail(mailMetadata, EmailKey.invite_existing_startup.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_PARTNER.toString())) {
            composeInviteMail(mailMetadata, EmailKey.invite_partners.toString());
        }
        try {
            emailUtil.sendEmail(mailMetadata);
        } catch (MessagingException e) {
            LOGGER.error("sendEmail", e);
        }
    }

    private void composeInviteMail(MailMetadata mailMetadata, String templatekey) {
        GetEmailTemplateDto2 emailTemplate = emailTemplateService.findActiveEmailTemplateByKeyAndLanguage(templatekey, "en")
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
        mailMetadata.setSubject(emailTemplate.getSubject());
        String header = emailTemplate.getHeader();
        String body = emailTemplate.getContent();
        String footer = emailTemplate.getFooter();
        Map<String, Object> props = mailMetadata.getProps();
        Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("EMAIL", mailMetadata.getTo());
        tokenValues.put("INVITE_LINK", "");
        body = emailUtil.replaceEmailToken(body, tokenValues);
        String emailHtml = emailUtil.getTemplateHtml(header, body, footer, (String) props.get("inviteLink"),
            "Register");
        mailMetadata.setTemplateString(emailHtml);
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void sendStartupDueDiligenceInviteMail(MailMetadata mailMetadata) {
        com.stc.inspireu.dtos.GetEmailTemplateDto2 emailTemplate = emailTemplateService
            .findActiveEmailTemplateByKeyAndLanguage(EmailKey.on_due_diligence_file_upload.toString(), "en")
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
        mailMetadata.setSubject(emailTemplate.getSubject());
        String header = emailTemplate.getHeader();
        String body = emailTemplate.getContent();
        String footer = emailTemplate.getFooter();
        Map<String, Object> props = mailMetadata.getProps();
        Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("EMAIL", mailMetadata.getTo());
        tokenValues.put("invite-link", "");
        body = emailUtil.replaceEmailToken(body, tokenValues);
        String emailHtml = emailUtil.getTemplateHtml(header, body, footer, (String) props.get("inviteLink"),
            "Due-Diligence upload Link");
        mailMetadata.setTemplateString(emailHtml);
        try {
            emailUtil.sendEmail(mailMetadata);
            LOGGER.debug("mail sending complete");
        } catch (MessagingException e) {
            LOGGER.error("sendMail", e);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void sendResetPasswordNotification(MailMetadata mailMetadata) {
        GetEmailTemplateDto2 emailTemplate = emailTemplateService.findActiveEmailTemplateByKeyAndLanguage(EmailKey.reset_password.toString(), "en")
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        mailMetadata.setSubject(emailTemplate.getSubject());
        String header = emailTemplate.getHeader();
        String body = emailTemplate.getContent();
        String footer = emailTemplate.getFooter();
        Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("EMAIL", mailMetadata.getTo());
        body = emailUtil.replaceEmailToken(body, tokenValues);
        String emailHtml = emailUtil.getTemplateHtml(header, body, footer, "", "");
        mailMetadata.setTemplateString(emailHtml);
        try {
            emailUtil.sendEmail(mailMetadata);
            LOGGER.debug("mail sending complete");
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void sendForgotPasswordNotification(MailMetadata mailMetadata) {
        GetEmailTemplateDto2 emailTemplate = emailTemplateService.findActiveEmailTemplateByKeyAndLanguage(EmailKey.forgot_password.toString(), "en")
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
        mailMetadata.setSubject(emailTemplate.getSubject());
        String header = emailTemplate.getHeader();
        String body = emailTemplate.getContent();
        String footer = emailTemplate.getFooter();
        Map<String, Object> props = mailMetadata.getProps();
        Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("EMAIL", mailMetadata.getTo());
        tokenValues.put("resetLink", "");
        body = emailUtil.replaceEmailToken(body, tokenValues);
        String emailHtml = emailUtil.getTemplateHtml(header, body, footer, (String) props.get("resetLink"),
            "ForgotPassword");
        mailMetadata.setTemplateString(emailHtml);
        try {
            emailUtil.sendEmail(mailMetadata);
            LOGGER.debug("mail sending complete");
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void sendWelcomeNotification(MailMetadata mailMetadata, String roleName) {
        if (roleName.equals(Roles.ROLE_STARTUPS_MEMBER.name()))
            return;
        if (roleName.equals(RoleName.Value.ROLE_STARTUPS_MEMBER.toString())) {
            composeWelcomeMail(mailMetadata, EmailKey.welcome_startup_member.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_STARTUPS_ADMIN.toString())) {
            composeWelcomeMail(mailMetadata, EmailKey.welcome_startup_admin.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_MANAGEMENT_TEAM_ADMIN.toString())) {
            composeWelcomeMail(mailMetadata, EmailKey.welcome_management_admin.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_MANAGEMENT_TEAM_MEMBER.toString())) {
            composeWelcomeMail(mailMetadata, EmailKey.welcome_management_member.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_NON_STC_JUDGES.toString())) {
            composeWelcomeMail(mailMetadata, EmailKey.welcome_non_stc_judge.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_STC_JUDGES.toString())) {
            composeWelcomeMail(mailMetadata, EmailKey.welcome_stc_judge.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_COACHES_AND_TRAINERS.toString())) {
            composeWelcomeMail(mailMetadata, EmailKey.welcome_coachs_and_trainers.toString());
        } else if (roleName.equals(RoleName.Value.ROLE_EXISTING_STARTUPS.toString())) {
            composeWelcomeMail(mailMetadata, EmailKey.welcome_existing_startup.toString());
        }
        try {
            emailUtil.sendEmail(mailMetadata);
            LOGGER.debug("mail sending complete");
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void composeWelcomeMail(MailMetadata mailMetadata, String emailTemplateKey) {
        GetEmailTemplateDto2 emailTemplate = emailTemplateService.findActiveEmailTemplateByKeyAndLanguage(emailTemplateKey, "en")
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
        mailMetadata.setSubject(emailTemplate.getSubject());
        String header = emailTemplate.getHeader();
        String body = emailTemplate.getContent();
        String footer = emailTemplate.getFooter();
        Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("EMAIL", mailMetadata.getTo());
        body = emailUtil.replaceEmailToken(body, tokenValues);
        String emailHtml = emailUtil.getTemplateHtml(header, body, footer, "", "");
        mailMetadata.setTemplateString(emailHtml);
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void registrationFormSubmittedNotification(MailMetadata mailMetadata, String language) {
        if (Objects.isNull(language))
            language = "en";
        GetEmailTemplateDto2 emailTemplate = emailTemplateService
            .findActiveEmailTemplateByKeyAndLanguage(EmailKey.registration_form_submitted.toString(), language)
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
        mailMetadata.setSubject(emailTemplate.getSubject());
        String header = emailTemplate.getHeader();
        String body = emailTemplate.getContent();
        String footer = emailTemplate.getFooter();
        Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("EMAIL", mailMetadata.getTo());
        body = emailUtil.replaceEmailToken(body, tokenValues);
        String emailHtml = emailUtil.getTemplateHtml(header, body, footer, "", "", language);
        mailMetadata.setTemplateString(emailHtml);
        try {
            emailUtil.sendEmail(mailMetadata);
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
        }
        String mail = mailMetadata.getTo();
        if (!mail.isEmpty()) {
            Notification notificationToAdmin = new Notification();
            String message = mail + " is submitted  registration form.";
            notificationToAdmin.setKeywords(NotificationKeywords.not_important);
            notificationToAdmin.setMessage(message);
            notificationToAdmin.setShowToAdmin(true);
            notificationRepository.save(notificationToAdmin);

        }
    }

    @Override
    public void sendMailMangeFormsNotifications(MailMetadata mailMetadata, String phase) {
        if (phase.equals(EmailKey.selected_for_assessment.toString())) {
            composeManageFormsNotification(mailMetadata, EmailKey.selected_for_assessment.toString());
        } else if (phase.equals(EmailKey.ASSIGNED_TO_SCREENING_EVALUATORS.toString())) {
            composeManageFormsNotification(mailMetadata, EmailKey.ASSIGNED_TO_SCREENING_EVALUATORS.toString());
        } else if (phase.equals(EmailKey.selected_for_bootcamp.toString())) {
            composeManageFormsNotification(mailMetadata, EmailKey.selected_for_bootcamp.toString());
        } else if (phase.equals(EmailKey.not_selected_for_assessment.toString())) {
            composeManageFormsNotification(mailMetadata, EmailKey.not_selected_for_assessment.toString());
        } else if (phase.equals(EmailKey.not_selected_for_bootcamp.toString())) {
            composeManageFormsNotification(mailMetadata, EmailKey.not_selected_for_bootcamp.toString());
        }
        try {
            emailUtil.sendEmail(mailMetadata);
            LOGGER.debug("mail sending complete");
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void composeManageFormsNotification(MailMetadata mailMetadata, String emailTemplateKey) {
        GetEmailTemplateDto2 emailTemplate = emailTemplateService.findActiveEmailTemplateByKeyAndLanguage(emailTemplateKey, "en")
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
        mailMetadata.setSubject(emailTemplate.getSubject());
        String header = emailTemplate.getHeader();
        String body = emailTemplate.getContent();
        String footer = emailTemplate.getFooter();
        Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("EMAIL", mailMetadata.getTo());
        body = emailUtil.replaceEmailToken(body, tokenValues);
        String emailHtml = emailUtil.getTemplateHtml(header, body, footer, "", "");
        mailMetadata.setTemplateString(emailHtml);
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void create121MeetingNotificationByAdmin(User user, Startup startup, long longValue) {
        Optional<Startup> s = startupRepository.findById(startup.getId());
        Optional<User> u = userRepository.findById(user.getId());
        if (s.isPresent() && u.isPresent()) {
            Notification notification = new Notification();
            String message = user.getEmail() + " scheduled one2one meeting with startup " + startup.getStartupName()
                + " on " + utility.toLegacyUTCString(new Date(longValue));
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setTargetStartup(s.get());
            notification.setSourceUser(u.get());
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void create121MeetingNotificationByCoach(User coach, Startup startup, long sessionStart) {
        Optional<Startup> s = startupRepository.findById(startup.getId());
        Optional<User> u = userRepository.findById(coach.getId());
        if (s.isPresent() && u.isPresent()) {
            Notification notification = new Notification();
            String message = coach.getEmail() + " scheduled one2one meeting with startup " + startup.getStartupName()
                + " on " + utility.toLegacyUTCString(new Date(sessionStart));
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setTargetStartup(s.get());
            notification.setSourceUser(u.get());
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void create121MeetingNotificationBySartup(User user, User coach, Startup startup, long sessionStart) {
        Optional<Startup> s = startupRepository.findById(startup.getId());
        Optional<User> c = userRepository.findById(coach.getId());
        Optional<User> u = userRepository.findById(user.getId());
        if (s.isPresent() && c.isPresent() && u.isPresent()) {
            Notification notification = new Notification();
            String message = startup.getStartupName() + " scheduled one2one meeting with Coach " + coach.getEmail()
                + " on " + utility.toLegacyUTCString(new Date(sessionStart));
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setSourceUser(u.get());
            notification.setTargetUser(c.get());
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void registrationNotifications(User sourceUser, String role) {
        Optional<User> u = userRepository.findById(sourceUser.getId());
        if (u.isPresent()) {
            Notification notificationToAdmin = new Notification();
            String message = sourceUser.getEmail() + "  is registered as " + role + " by  the invitationLink ";

            notificationToAdmin.setKeywords(NotificationKeywords.not_important);
            notificationToAdmin.setMessage(message);
            notificationToAdmin.setSourceUser(u.get());
            notificationToAdmin.setShowToAdmin(true);
            if (role.equals(RoleName.Value.ROLE_STARTUPS_MEMBER.toString())
                || role.equals(RoleName.Value.ROLE_STARTUPS_ADMIN.toString())) {
                notificationToAdmin.setTargetStartup(u.get().getStartup());
            }
            notificationRepository.save(notificationToAdmin);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void sendMeetingAttendanceNotification(User sourceUser, User TargetUser, Startup startup, String status,
                                                  long sessionTime) {
        Optional<Startup> s = startupRepository.findById(startup.getId());
        Optional<User> u = userRepository.findById(sourceUser.getId());
        Optional<User> t = userRepository.findById(TargetUser.getId());
        if (s.isPresent() && u.isPresent() && t.isPresent()) {
            Notification notification = new Notification();
            String message = "";
            if (status.equals(Constant.PRESENT.toString())) {
                message = u.get().getEmail() + " was present in oneToOneMeeting at "
                    + utility.toLegacyUTCString(new Date(sessionTime));
            } else if (status.equals(Constant.LATE.toString())) {
                message = u.get().getEmail() + " was late in oneToOneMeeting at "
                    + utility.toLegacyUTCString(new Date(sessionTime));

            } else if (status.equals(Constant.ABSENT.toString())) {
                message = u.get().getEmail() + " was Absent in oneToOneMeeting at "
                    + utility.toLegacyUTCString(new Date(sessionTime));
            }
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setTargetStartup(s.get());
            notification.setSourceUser(u.get());
            notification.setTargetUser(t.get());
            notification.setShowToAdmin(true);
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void remainedMeetingNotification(User user, Startup startup, long longValue) {
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void acadamicRoomsPublishedNotification(Long userId, boolean isWorkShop) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            Notification notification = new Notification();
            String message = isWorkShop
                ? user.get().getRole().getRoleAlias() + " " + user.get().getEmail()
                + " created new WorkShop Session"
                : user.get().getRole().getRoleAlias() + " " + user.get().getEmail() + " created new Academy Room";
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setSourceUser(user.get());
            notification.setShowToAdmin(false);
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void eventNotificationCreatedByStartup(User user, Startup startup, long longValue) {
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void conformationNotificationByCoach(User user, Startup startup, long longValue, String status) {
        Optional<Startup> s = startupRepository.findById(startup.getId());
        Optional<User> c = userRepository.findById(user.getId());
        if (s.isPresent() && c.isPresent()) {
            Notification notification = new Notification();
            String message = "";
            if (status.equalsIgnoreCase(Constant.ACCEPTED.toString())) {
                message = c.get().getAlias() + " accepted sheduled meeting on "
                    + utility.toLegacyUTCString(new Date(longValue));
            }
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setSourceUser(c.get());
            notification.setTargetStartup(s.get());
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void trainingMaterialsUploadNotification(User user) {
        Optional<User> u = userRepository.findById(user.getId());
        if (u.isPresent()) {
            Notification notification = new Notification();
            String message = user.getEmail() + " uploaded new training materials";
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setSourceUser(u.get());
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void assignmentUploadNotification(User user) {
        Optional<User> u = userRepository.findById(user.getId());
        if (u.isPresent()) {
            Notification notification = new Notification();
            String message = user.getEmail() + " uploaded new assignment";
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setSourceUser(u.get());
            notification.setShowToAdmin(true);
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void assignmentSubmitNotification(User user, boolean present, boolean isLate, User coach) {
        Optional<User> u = userRepository.findById(user.getId());
        Optional<User> t = userRepository.findById(coach.getId());
        if (u.isPresent() && t.isPresent()) {
            Notification notification = new Notification();
            if (present && isLate) {
                String message = "Late submit";
                notification.setKeywords(NotificationKeywords.not_important);
                notification.setMessage(message);
                notification.setTargetStartup(u.get().getStartup());
                notification.setSourceUser(t.get());
                notification.setTargetUser(coach);
            } else if (present) {
                String message = "Assignment submitted by " + user.getAlias();
                notification.setKeywords(NotificationKeywords.not_important);
                notification.setMessage(message);
                notification.setTargetUser(coach);
                notification.setSourceUser(user);
                notification.setTargetStartup(u.get().getStartup());
            } else {
                String message = "Assignment not submitted";
                notification.setKeywords(NotificationKeywords.not_important);
                notification.setMessage(message);
                notification.setTargetStartup(u.get().getStartup());
                notification.setSourceUser(t.get());
                notification.setTargetUser(coach);
            }
            notification.setShowToAdmin(true);
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void assignmentReviewNotification(User user, Startup startup, String status) {
        Optional<Startup> s = startupRepository.findById(startup.getId());
        Optional<User> u = userRepository.findById(user.getId());
        if (s.isPresent() && u.isPresent()) {
            Notification notification = new Notification();
            String message = startup.getStartupName() + "'s assignment was Approved";
            if (status.equals(Constant.RESUBMIT.toString())) {
                message = startup.getStartupName() + "'s assignment need to resubmit ";
            }
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setTargetStartup(s.get());
            notification.setSourceUser(u.get());
            notification.setShowToAdmin(true);
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void surveySubmitNotification(User user, User coach) {
        Optional<User> u = userRepository.findById(user.getId());
        Optional<User> t = userRepository.findById(coach.getId());
        if (u.isPresent() && t.isPresent()) {
            Notification notification = new Notification();
            String message = "Survey sumbited by " + user.getEmail();
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setTargetStartup(u.get().getStartup());
            notification.setSourceUser(u.get());
            notification.setTargetUser(t.get());
            notification.setShowToAdmin(true);
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void surveyResubmitNotification(User coach, Startup startup, long longValue) {
        Optional<User> u = userRepository.findById(coach.getId());
        Optional<Startup> s = startupRepository.findById(startup.getId());
        if (u.isPresent() && s.isPresent()) {
            Notification notification = new Notification();
            String message = "Resubmit the survey before " + utility.toLegacyUTCString(new Date(longValue));
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setTargetStartup(s.get());
            notification.setSourceUser(u.get());
            notification.setShowToAdmin(true);
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void sendMailForUnblockedUsersByAdmin(MailMetadata mailMetadata, Boolean isBock) {
        if (isBock) {
            GetEmailTemplateDto2 emailTemplate = emailTemplateService.findActiveEmailTemplateByKeyAndLanguage(EmailKey.user_blocked.toString(), "en")
                .orElseThrow(() -> new CustomRunTimeException("No active template found"));
            if (emailTemplate != null) {
                mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
                mailMetadata.setSubject(emailTemplate.getSubject());
                String header = emailTemplate.getHeader();
                String body = emailTemplate.getContent();
                String footer = emailTemplate.getFooter();
                Map<String, String> tokenValues = new HashMap<>();
                tokenValues.put("EMAIL", mailMetadata.getTo());
                body = emailUtil.replaceEmailToken(body, tokenValues);
                String emailHtml = emailUtil.getTemplateHtml(header, body, footer, null, "BlockUser");
                mailMetadata.setTemplateString(emailHtml);
            }
        } else {
            GetEmailTemplateDto2 emailTemplate = emailTemplateService.findActiveEmailTemplateByKeyAndLanguage(EmailKey.user_unblocked.toString(), "en")
                .orElseThrow(() -> new CustomRunTimeException("No active template found"));
            if (emailTemplate != null) {
                mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
                mailMetadata.setSubject(emailTemplate.getSubject());
                String header = emailTemplate.getHeader();
                String body = emailTemplate.getContent();
                String footer = emailTemplate.getFooter();
                Map<String, String> tokenValues = new HashMap<>();
                tokenValues.put("EMAIL", mailMetadata.getTo());
                body = emailUtil.replaceEmailToken(body, tokenValues);
                String emailHtml = emailUtil.getTemplateHtml(header, body, footer, null, "UnBlockUser");
                mailMetadata.setTemplateString(emailHtml);
            }
        }
        try {
            emailUtil.sendEmail(mailMetadata);
            LOGGER.debug("mail sending complete");
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void dueDiligenceResubmitNotification(MailMetadata mailMetadata, String msg, Long sourceUserID,
                                                 Long startupId, String link) {
        GetEmailTemplateDto2 emailTemplate = emailTemplateService.findActiveEmailTemplateByKeyAndLanguage(EmailKey.on_due_diligence_reject.toString(), "en")
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
        mailMetadata.setSubject(emailTemplate.getSubject());
        String header = emailTemplate.getHeader();
        String body = emailTemplate.getContent();
        String footer = emailTemplate.getFooter();
        Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("EMAIL", mailMetadata.getTo());
        body = emailUtil.replaceEmailToken(body, tokenValues);
        if (msg != null && !msg.isEmpty())
            body = body + emailUtil.centerDivWrapper(msg);
        String emailHtml = emailUtil.getTemplateHtml(header, body, footer, link, "Click");
        mailMetadata.setTemplateString(emailHtml);
        try {
            emailUtil.sendEmail(mailMetadata);
            LOGGER.debug("mail sending complete");
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
        }
        Optional<User> sourceUser = userRepository.findById(sourceUserID);
        Optional<Startup> s = startupRepository.findById(startupId);
        if (s.isPresent() && sourceUser.isPresent()) {
            Notification notification = new Notification();
            String message = sourceUser.get().getAlias() + " Rejected Due-Diligence";
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setSourceUser(sourceUser.get());
            notification.setTargetStartup(s.get());
            notification.setShowToAdmin(true);
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void evaluationFormSubmitted(User judge) {
        Optional<User> t = userRepository.findById(judge.getId());
        if (t.isPresent()) {
            Notification notification = new Notification();
            String message = judge.getEmail() + " submited Evaluation form";
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setSourceUser(t.get());
            notification.setShowToAdmin(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    public void monthlyProgressReportSubmitted(User sourceUser) {
        Optional<User> t = userRepository.findById(sourceUser.getId());
        if (t.isPresent()) {
            Notification notification = new Notification();
            String message = sourceUser.getEmail() + " monthly Progress report submitted";
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            notification.setSourceUser(t.get());
            notification.setTargetStartup(t.get().getStartup());
            notification.setShowToAdmin(true);
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void updateProfileCardNotification(User sourceUser) {
        Optional<User> u = userRepository.findById(sourceUser.getId());
        if (u.isPresent()) {
            Notification notification = new Notification();
            String message = sourceUser.getRole().getRoleAlias() + " " + sourceUser.getEmail()
                + " updated user profile";
            notification.setKeywords(NotificationKeywords.not_important);
            notification.setMessage(message);
            if (sourceUser.getRole().getRoleName().equals(RoleName.Value.ROLE_STARTUPS_ADMIN.toString())
                || sourceUser.getRole().getRoleName().equals(RoleName.Value.ROLE_STARTUPS_MEMBER.toString())) {
                notification.setTargetStartup(u.get().getStartup());
            }
            notification.setSourceUser(u.get());
            notification.setShowToAdmin(true);
            notificationRepository.save(notification);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void bootcampInterview(User sourceUser, User TargetUser, Startup startup, String status, String role) {
        Optional<Startup> s = startupRepository.findById(startup.getId());
        Optional<User> u = userRepository.findById(sourceUser.getId());
        Notification notificationToStartup = new Notification();
        String message = sourceUser.getEmail() + " was " + status + "the bootcampInterview ";
        notificationToStartup.setKeywords(NotificationKeywords.not_important);
        notificationToStartup.setMessage(message);
        notificationToStartup.setTargetStartup(s.orElse(null));
        notificationToStartup.setSourceUser(u.orElse(null));
        notificationRepository.save(notificationToStartup);
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void markCardNotification(User sourceUser, Startup startup,
                                     PostMarkCardNotificationDto postMarkCardNotificationDto) {
        Optional<Startup> s = startupRepository.findById(startup.getId());
        Optional<User> u = userRepository.findById(sourceUser.getId());
        Notification notificationToStartup = new Notification();
        notificationToStartup.setKeywords(NotificationKeywords.not_important);
        notificationToStartup.setMessage(postMarkCardNotificationDto.getMessage());
        notificationToStartup.setTargetStartup(s.orElse(null));
        notificationToStartup.setSourceUser(u.orElse(null));
        notificationRepository.save(notificationToStartup);
        List<User> userList = userRepository.findByRole_RoleNameAndStartupId(RoleName.ROLE_STARTUPS_ADMIN,
            startup.getId());
        if (!userList.isEmpty()) {
            for (User user : userList) {
                MailMetadata mailMetadata = new MailMetadata();
                Map<String, Object> props = new HashMap<>();
                props.put("toMail", user.getEmail());
                mailMetadata.setFrom("");
                mailMetadata.setTo(user.getEmail());
                mailMetadata.setProps(props);
                mailMetadata.setSubject("MarkCard Payment");
                mailMetadata.setTemplateFile(EmailKey.general_notification.toString());
                GetEmailTemplateDto2 emailTemplate = emailTemplateService
                    .findActiveEmailTemplateByKeyAndLanguage(EmailKey.general_notification.toString(), "en")
                    .orElseThrow(() -> new CustomRunTimeException("No active template found"));
                if (emailTemplate != null) {
                    mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
                    String header = emailTemplate.getHeader();
                    String body = postMarkCardNotificationDto.getMessage();
                    String footer = emailTemplate.getFooter();
                    Map<String, String> tokenValues = new HashMap<>();
                    tokenValues.put("EMAIL", mailMetadata.getTo());
                    body = emailUtil.replaceEmailToken(body, tokenValues);
                    String emailHtml = emailUtil.getTemplateHtml(header, body, footer, null, "");
                    mailMetadata.setTemplateString(emailHtml);
                    try {
                        emailUtil.sendEmail(mailMetadata);
                        LOGGER.debug("mail sending complete");
                    } catch (MessagingException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void sendAuthOtp(Integer random, String email) {
        GetEmailTemplateDto2 emailTemplate = emailTemplateService.findActiveEmailTemplateByKeyAndLanguage(EmailKey.login_otp.toString(), "en")
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        MailMetadata mailMetadata = new MailMetadata();
        mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
        Map<String, Object> props = new HashMap<>();
        props.put("toMail", email);
        mailMetadata.setFrom("");
        mailMetadata.setTo(email);
        mailMetadata.setProps(props);
        mailMetadata.setSubject(emailTemplate.getSubject());
        String header = emailTemplate.getHeader();
        String body = emailTemplate.getContent();
        String footer = emailTemplate.getFooter();
        body = emailUtil.centerDivWrapper(body) + emailUtil.centerDivWrapper(random + "");
        String emailHtml = emailUtil.getTemplateHtml(header, body, footer, "", "");
        mailMetadata.setTemplateString(emailHtml);
        try {
            emailUtil.sendEmail(mailMetadata);
            LOGGER.debug("otp mail sending complete");
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void feedbackNotification(User sourceUser, Startup startup) {
        Optional<Startup> s = startupRepository.findById(startup.getId());
        Optional<User> u = userRepository.findById(sourceUser.getId());
        Notification notificationToStartup = new Notification();
        String message = sourceUser.getEmail() + " gave a feedback ";
        notificationToStartup.setKeywords(NotificationKeywords.not_important);
        notificationToStartup.setMessage(message);
        notificationToStartup.setTargetStartup(s.orElse(null));
        notificationToStartup.setSourceUser(u.orElse(null));
        notificationRepository.save(notificationToStartup);
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void notifyManagementToPublishProgressReportTemplate(User user, IntakeProgram intakeProgram) {
        Notification notificationToStartup = new Notification();
        String message = user.getEmail() + " requested to publish progress report template for intake "
            + intakeProgram.getId();
        notificationToStartup.setKeywords(NotificationKeywords.not_important);
        notificationToStartup.setMessage(message);
        notificationToStartup.setTargetStartup(null);
        notificationToStartup.setSourceUser(null);
        notificationToStartup.setShowToAdmin(true);
        notificationRepository.save(notificationToStartup);
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void notifyCoach(User user, FeedbackNotifyCoachDto feedbackNotifyCoachDto) {
        userRepository.findById(feedbackNotifyCoachDto.getCoachId()).ifPresent(u -> {
            Notification notificationToStartup = new Notification();
            String message = "Please submit feedback for " + feedbackNotifyCoachDto.getStartupName();
            notificationToStartup.setKeywords(NotificationKeywords.not_important);
            notificationToStartup.setMessage(message);
            notificationToStartup.setTargetStartup(null);
            notificationToStartup.setSourceUser(null);
            notificationToStartup.setShowToAdmin(false);
            notificationToStartup.setTargetUser(u);
            notificationRepository.save(notificationToStartup);
            GetEmailTemplateDto2 emailTemplate = emailTemplateService
                .findActiveEmailTemplateByKeyAndLanguage(EmailKey.general_notification.toString(), "en")
                .orElseThrow(() -> new CustomRunTimeException("No active template found"));
            MailMetadata mailMetadata = new MailMetadata();
            mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
            Map<String, Object> props = new HashMap<>();
            props.put("toMail", user.getEmail());
            mailMetadata.setFrom("");
            mailMetadata.setTo(user.getEmail());
            mailMetadata.setProps(props);
            mailMetadata.setSubject("Submit feedback");
            String header = emailTemplate.getHeader();
            String body = emailUtil
                .centerDivWrapper("Please submit feedback for " + feedbackNotifyCoachDto.getStartupName());
            String footer = emailTemplate.getFooter();
            Map<String, String> tokenValues = new HashMap<>();
            body = emailUtil.replaceEmailToken(body, tokenValues);
            String emailHtml1 = emailUtil.getTemplateHtml(header, body, footer, "", "");
            mailMetadata.setTemplateString(emailHtml1);
            try {
                emailUtil.sendEmail(mailMetadata);
            } catch (MessagingException e) {
                LOGGER.error(e.getMessage());
            }
        });
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void sendDueNotes(DueDiligenceNote2021 dn, User user, Long startupId) {
        Pageable pageable = PageRequest.of(0, 3);
        Optional<User> su = userRepository.findById(user.getId());
        List<User> users = userRepository.findByStartup_Id(startupId, pageable);
        if (users.size() == 1 && su.isPresent()) {
            GetEmailTemplateDto2 emailTemplate = emailTemplateService
                .findActiveEmailTemplateByKeyAndLanguage(EmailKey.general_notification.toString(), "en")
                .orElseThrow(() -> new CustomRunTimeException("No active template found"));
            if (emailTemplate != null) {
                MailMetadata mailMetadata = new MailMetadata();
                mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
                Map<String, Object> props = new HashMap<>();
                props.put("toMail", users.get(0).getEmail());
                mailMetadata.setFrom("");
                mailMetadata.setTo(users.get(0).getEmail());
                mailMetadata.setProps(props);
                mailMetadata.setSubject("Chat message from " + su.get().getEmail());
                String header = emailTemplate.getHeader();
                String body = emailUtil.centerDivWrapper(dn.getReplyNote());
                String footer = emailTemplate.getFooter();
                Map<String, String> tokenValues = new HashMap<>();
                body = emailUtil.replaceEmailToken(body, tokenValues);
                String emailHtml1 = emailUtil.getTemplateHtml(header, body, footer, "", "");
                mailMetadata.setTemplateString(emailHtml1);
                try {
                    emailUtil.sendEmail(mailMetadata);
                    LOGGER.debug("mail sending complete");
                } catch (MessagingException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }

    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void alertStartup(User user, Startup startup, AcademyRoom academyRoom, MarkCard2022 markCard2022,
                             PostMarkCardNotificationDto postMarkCardNotificationDto) {
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void sendCalendlyNotification(OpenEvent openEvent, MailMetadata mailMetadata, String msg) {
        GetEmailTemplateDto2 emailTemplate = emailTemplateService.findActiveEmailTemplateByKeyAndLanguage(EmailKey.general_notification.toString(), "en")
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
        String header = "";
        String body = emailUtil.evaluationSlotMail(mailMetadata.getProps().get("inviteLink").toString(), openEvent.getEventPhase());
        String footer = "";
        Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("EMAIL", mailMetadata.getTo());
        tokenValues.put("INVITE_LINK", "");
        body = emailUtil.replaceEmailToken(body, tokenValues);
        String emailHtml = emailUtil.getTemplateHtml(header, body, footer, "",
            "Click");
        mailMetadata.setTemplateString(emailHtml);
        try {
            emailUtil.sendEmail(mailMetadata);
            LOGGER.debug("calendly mail sending complete");
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public Page<ProjectNotification> getAlltime(CurrentUserObject currentUserObject, String filterBy, Pageable paging) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        Page<ProjectNotification> ls = Page.empty();
        if (user.isPresent()) {
            ls = notificationRepository.getAlltime(paging);
        }
        return ls;
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void academyRoomShareNotification(Long academyRoomId, List<Long> sharedUserIds, User currentUser,
                                             Role currentRole) {
        GetEmailTemplateDto2 emailTemplate = emailTemplateService.findActiveEmailTemplateByKeyAndLanguage(EmailKey.academy_share.toString(), "en")
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        Optional<AcademyRoom> ar = academyRoomRepository.findById(academyRoomId);
        if (ar.isPresent()) {
            for (Long sharedUserId : sharedUserIds) {
                Optional<User> user = userRepository.findById(sharedUserId);
                if (user.isPresent()
                    && user.get().getRole().getRoleName().equals(RoleName.ROLE_COACHES_AND_TRAINERS)) {
                    MailMetadata mailMetadata = new MailMetadata();
                    mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
                    Map<String, Object> props = new HashMap<>();
                    props.put("toMail", user.get().getEmail());
                    mailMetadata.setFrom("");
                    mailMetadata.setTo(user.get().getEmail());
                    mailMetadata.setProps(props);
                    mailMetadata.setSubject(emailTemplate.getSubject());
                    String header = emailTemplate.getHeader();
                    String body = emailTemplate.getContent();
                    String footer = emailTemplate.getFooter();
                    body = emailUtil.centerDivWrapper(body) + emailUtil.centerDivWrapper(
                        user.get().getEmail() + " shared Academy room " + ar.get().getName() + " with you.");
                    String emailHtml = emailUtil.getTemplateHtml(header, body, footer, "", "");
                    mailMetadata.setTemplateString(emailHtml);
                    try {
                        emailUtil.sendEmail(mailMetadata);
                    } catch (MessagingException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        }
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void evaluationStartNotification(String string, IntakeProgramSubmission intakeProgramSubmission) {
        GetEmailTemplateDto2 emailTemplate = emailTemplateService.findActiveEmailTemplateByKeyAndLanguage(EmailKey.general_notification.toString(), "en")
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        Set<Long> userIds = Collections.emptySet();
        if (string.equals(Constant.ASSESSMENT.toString())) {
            userIds = intakeProgramSubmissionRepository
                .getUserIdsFromAssessmentWhere(intakeProgramSubmission.getId());
        } else if (string.equals(Constant.BOOTCAMP.toString())) {
            userIds = intakeProgramSubmissionRepository
                .getUserIdsFromBootcampWhere(intakeProgramSubmission.getId());
        } else if (string.equals(Constant.SCREENING.toString())) {
            userIds = intakeProgramSubmissionRepository
                .getUserIdsFromScreening(intakeProgramSubmission.getId());
        }
        userIds.forEach(id -> userRepository.findById(id).ifPresent(user -> {
            MailMetadata mailMetadata = new MailMetadata();
            mailMetadata.setAttachments(emailTemplate.getAttachmentPaths());
            Map<String, Object> props = new HashMap<>();
            props.put("toMail", user.getEmail());
            mailMetadata.setFrom("");
            mailMetadata.setTo(user.getEmail());
            mailMetadata.setProps(props);
            mailMetadata.setSubject(emailTemplate.getSubject());
            String header = emailTemplate.getHeader();
            String body = emailTemplate.getContent();
            String footer = emailTemplate.getFooter();
            String txt = "";
            if (string.equals(Constant.ASSESSMENT.toString()))
                txt = "Assessment";
            else if (string.equals(Constant.BOOTCAMP.toString()))
                txt = "Bootcamp";
            else if (string.equals(Constant.SCREENING.toString()))
                txt = "Screening";
            body = emailUtil.centerDivWrapper(body) + emailUtil.centerDivWrapper(txt + " evaluation started");
            String emailHtml = emailUtil.getTemplateHtml(header, body, footer, "", "");
            mailMetadata.setTemplateString(emailHtml);
            try {
                emailUtil.sendEmail(mailMetadata);
            } catch (MessagingException e) {
                LOGGER.error(e.getMessage());
            }
        }));
    }
}
