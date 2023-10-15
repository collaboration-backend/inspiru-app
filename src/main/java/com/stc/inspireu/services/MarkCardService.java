package com.stc.inspireu.services;

import com.stc.inspireu.dtos.PostMarkCardNotificationDto;
import com.stc.inspireu.dtos.PostMarkCardSummaryDto;
import com.stc.inspireu.dtos.PutGenerateMarkcardDto;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.stc.inspireu.beans.CurrentUserObject;

public interface MarkCardService {
	ResponseEntity<?> getStartupMarkCardAcademyRoomDetails(CurrentUserObject currentUserObject, Long markCardId,
			Long startupId, Long academyRoomId);

	ResponseEntity<?> getMarkCardList(CurrentUserObject currentUserObject, Pageable paging, String filterBy);

	ResponseEntity<?> getAcademyRoomsList(CurrentUserObject currentUserObject, Pageable paging, String filterBy,
			Long intakeProgramId);

	ResponseEntity<?> getOneMarkCard(CurrentUserObject currentUserObject, Long markCardId);

	ResponseEntity<?> checkProgressCardReport(CurrentUserObject currentUserObject, Long intakeProgramId);

	ResponseEntity<?> markCardStartupList(CurrentUserObject currentUserObject, Long intakeProgramId, Long academyRoomId,
			Pageable pageable, String filterKeyWord);

	ResponseEntity<?> marCardSummary(CurrentUserObject currentUserObject, Long intakeProgramId, Pageable pageable,
			String filterKeyWord);

	ResponseEntity<?> saveMarkCardSummary(CurrentUserObject currentUserObject,
			PostMarkCardSummaryDto markCardSummaryDto);

	ResponseEntity<?> updateMarkCardSummary(CurrentUserObject currentUserObject,
			PostMarkCardSummaryDto markCardSummaryDto, Long markCardSummaryId);

	ResponseEntity<?> getMarkCardByStartup(CurrentUserObject currentUserObject, Long StartupId, Long academyRoomId);

	ResponseEntity<?> getParticipationPercentage(CurrentUserObject currentUserObject, Long StartupId,
			Long academyRoomId);

	ResponseEntity<?> sendMarkCardNotification(CurrentUserObject currentUserObject, Long StartupId, Long academyRoomId,
			PostMarkCardNotificationDto postMarkCardNotificationDto);

	ResponseEntity<?> markCardAcademRommStatus(CurrentUserObject currentUserObject, Long markCardId,
			Long academyRoomId);

	ResponseEntity<?> generateMarkcard(CurrentUserObject currentUserObject, Long markCardId, Long academyRoomId,
			PutGenerateMarkcardDto putGenerateMarkcardDto);

	ResponseEntity<?> getMarkCardAcademRoomStartupInfo(CurrentUserObject currentUserObject, Long markCardId,
			Long academyRoomId, Long startupId, String timezone);

	ResponseEntity<?> notifyStartup(CurrentUserObject currentUserObject, Long markCardId, Long academyRoomId,
			Long startupId, PostMarkCardNotificationDto postMarkCardNotificationDto);

	ResponseEntity<?> saveTemplate(CurrentUserObject currentUserObject, Long markCardId, Long academyRoomId,
			PutGenerateMarkcardDto putGenerateMarkcardDto);

	ResponseEntity<?> alertStartup(CurrentUserObject currentUserObject, Long markCardId, Long academyRoomId,
			Long startupId, PostMarkCardNotificationDto postMarkCardNotificationDto);

	ResponseEntity<?> getSummaryColumsList(CurrentUserObject currentUserObject, Long markCardId);

	ResponseEntity<?> getSummaryStartups(CurrentUserObject currentUserObject, Long markCardId, String filterBy,
			String filterKeyword, Pageable paging);

}
