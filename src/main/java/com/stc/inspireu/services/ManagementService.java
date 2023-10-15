package com.stc.inspireu.services;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.FeedbackNotifyCoachDto;
import com.stc.inspireu.dtos.InviteUserDto;
import com.stc.inspireu.dtos.ManagementRegistrationDto;
import com.stc.inspireu.dtos.PostFeedbackDto;
import com.stc.inspireu.dtos.PostOne2OneMeetingManagementDto;
import com.stc.inspireu.dtos.PostTrainingSessionDto;
import com.stc.inspireu.dtos.PutFeedbackDto;
import com.stc.inspireu.dtos.PutOne2OneMeetingManagementDto;
import com.stc.inspireu.models.User;

public interface ManagementService {

	Object inviteUser(InviteUserDto inviteUserDto);

	Object createFeedbacks(CurrentUserObject currentUserObject, PostFeedbackDto postFeedbackDto, Long refFeedbackId);

	ResponseEntity<?> updateFeedbacks(CurrentUserObject currentUserObject, PutFeedbackDto putFeedbackDto,
			Long refFeedbackId);

	Object getStartupFeedback(CurrentUserObject currentUserObject, Long startupId, Long workshopSessionId,
			Long refFeedbackId);

	ResponseEntity<Object> createOne2OneMeeting(CurrentUserObject currentUserObject,
			PostOne2OneMeetingManagementDto one2OneMeetingDto);

	Object updateOne2OneMeeting(CurrentUserObject currentUserObject, PutOne2OneMeetingManagementDto one2OneMeetingDto,
			Long meetingId);

	Object getOne2OneMeeting(CurrentUserObject currentUserObject, Long meetingId);

	ResponseEntity<?> deleteOne2OneMeeting(CurrentUserObject currentUserObject, Long meetingId);

	Object getAllOne2OneMeeting(CurrentUserObject currentUserObject, Long IntakeProgram);

	Object managementRegistration(ManagementRegistrationDto managementRegistrationDto, Map<String, Object> claims,
			User user);

	ResponseEntity<Object> _managementRegistration(ManagementRegistrationDto managementRegistrationDto);

	ResponseEntity<?> createTrainingSession(CurrentUserObject currentUserObject,
			PostTrainingSessionDto postTrainingSession);

	ResponseEntity<?> getTrainingSession(CurrentUserObject currentUserObject, Long trainingSessions);

	ResponseEntity<?> cancelTrainingSession(CurrentUserObject currentUserObject, Long trainingSessionId);

	ResponseEntity<?> getTrainingSessionIntakePrograms(CurrentUserObject currentUserObject);

	ResponseEntity<?> getTrainingSessionAcademyRooms(CurrentUserObject currentUserObject, Long intakeProgramId);

	ResponseEntity<?> getTrainingSessionWorkshopSessions(CurrentUserObject currentUserObject, Long academyRoomId);

	ResponseEntity<?> notifyCoach(CurrentUserObject currentUserObject,
			@Valid FeedbackNotifyCoachDto feedbackNotifyCoachDto);
}
