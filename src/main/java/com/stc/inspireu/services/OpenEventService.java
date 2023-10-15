package com.stc.inspireu.services;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostOpenEventDto;

public interface OpenEventService {

	ResponseEntity<?> createPublicCalendarEvent(CurrentUserObject currentUserObject, PostOpenEventDto postOpenEventDto);

	ResponseEntity<?> updatePublicCalendarEvent(CurrentUserObject currentUserObject, Long eventId,
			PostOpenEventDto postOpenEventDto);

	ResponseEntity<?> getPublicCalendarEvent(CurrentUserObject currentUserObject, Long eventId);

	ResponseEntity<?> getAllIntakePrograms(CurrentUserObject currentUserObject, Pageable paging);

	ResponseEntity<?> getUpComingEvents(CurrentUserObject currentUserObject, Long IntakeProgramId, String timeZone,
			Integer month, Integer Year, Integer day);

	ResponseEntity<?> deleteEventById(CurrentUserObject currentUserObject, Long eventId);

	ResponseEntity<?> getEventLink(CurrentUserObject currentUserObject, Long eventId, Long intakeProgramId);
}
