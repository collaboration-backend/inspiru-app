package com.stc.inspireu.services;

import java.util.List;

import javax.validation.Valid;

import com.stc.inspireu.models.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.beans.MailMetadata;
import com.stc.inspireu.dtos.FeedbackNotifyCoachDto;
import com.stc.inspireu.dtos.NotificationIdDto;
import com.stc.inspireu.dtos.PostMarkCardNotificationDto;
import com.stc.inspireu.jpa.projections.ProjectNotification;

public interface NotificationService {

	Page<ProjectNotification> getStartupNotifications(CurrentUserObject currentUserObject, String filterBy,
			Pageable paging);

	Page<ProjectNotification> getManagementNotifications(CurrentUserObject currentUserObject, String filterBy,
			Pageable paging);

	void sendInviteNotification(MailMetadata mailMetadata, String inviteMessage, String roleName);

	void sendWelcomeNotification(MailMetadata mailMetadata, String roleName);

	void sendForgotPasswordNotification(MailMetadata mailMetadata);

	void sendResetPasswordNotification(MailMetadata mailMetadata);

	void sendStartupDueDiligenceInviteMail(MailMetadata mailMetadata);

	void registrationFormSubmittedNotification(MailMetadata mailMetadata,String language);

	void create121MeetingNotificationByCoach(User user, Startup startup, long longValue);

	void sendMailMangeFormsNotifications(MailMetadata mailMetadata, String phase);

	void sendMeetingAttendanceNotification(User sourceUser, User TargetUser, Startup startup, String status,
			long sessionTime);

	void registrationNotifications(User sourceUser, String role);

	void create121MeetingNotificationByAdmin(User user, Startup startup, long longValue);

	void remainedMeetingNotification(User user, Startup startup, long longValue);

	void acadamicRoomsPublishedNotification(Long userId, boolean isWorkShop);

	void eventNotificationCreatedByStartup(User user, Startup startup, long longValue);

	void conformationNotificationByCoach(User user, Startup startup, long longValue, String status);

	void assignmentSubmitNotification(User user, boolean present, boolean isLate, User coach);

	void assignmentReviewNotification(User user, Startup startup, String status);

	void surveySubmitNotification(User user, User coach);

	void surveyResubmitNotification(User user, Startup startup, long longValue);

	void sendMailForUnblockedUsersByAdmin(MailMetadata mailMetadata, Boolean isBlock);

	void dueDiligenceResubmitNotification(MailMetadata mailMetadata, String string, Long targetId, Long startupId,
			String inviteToken);

	void updateProfileCardNotification(User sourceUser);

	void bootcampInterview(User sourceUser, User TargetUser, Startup startup, String status, String role);

	void markCardNotification(User sourceUser, Startup startup,
			PostMarkCardNotificationDto postMarkCardNotificationDto);

	void create121MeetingNotificationBySartup(User user, User coach, Startup startup, long longValue);

	void trainingMaterialsUploadNotification(User user);

	void assignmentUploadNotification(User user);

	void evaluationFormSubmitted(User judge);

	void monthlyProgressReportSubmitted(User sourceUser);

	void markAsRead(CurrentUserObject currentUserObject, NotificationIdDto notificationIdDto);

	void sendAuthOtp(Integer random, String string);

	void feedbackNotification(User sourceUser, Startup s);

	void notifyManagementToPublishProgressReportTemplate(User user, IntakeProgram intakeProgram);

	void notifyCoach(User user, @Valid FeedbackNotifyCoachDto feedbackNotifyCoachDto);

	void sendDueNotes(DueDiligenceNote2021 x, User user, Long startupId);

	void alertStartup(User user, Startup startup, AcademyRoom academyRoom, MarkCard2022 markCard2022,
			PostMarkCardNotificationDto postMarkCardNotificationDto);

	void sendCalendlyNotification(OpenEvent openEvent, MailMetadata mailMetadata, String msg);

	Page<ProjectNotification> getAlltime(CurrentUserObject currentUserObject, String filterBy, Pageable paging);

	void academyRoomShareNotification(Long academyRoomId, List<Long> sharedUserIds, User currentUser, Role currentRole);

	void evaluationStartNotification(String string, IntakeProgramSubmission intakeProgramSubmission);

}
