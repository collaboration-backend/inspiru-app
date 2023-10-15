package com.stc.inspireu.services;

import java.util.Date;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.stc.inspireu.beans.CurrentUserObject;

public interface DashboardService {

	Object getScheduleCount(CurrentUserObject currentUserObject, Date currentDate);

	ResponseEntity<?> getStartupList(CurrentUserObject currentUserObject);

	Object getStartupsWiseScheduleCount(CurrentUserObject currentUserObject, Long startupId, Date currentDate);

	Object getUpcomingEventSchedules(CurrentUserObject currentUserObject, Date currentDate, String timeZone);

	ResponseEntity<Object> getGraphData(CurrentUserObject currentUserObject, String fieldName);

	ResponseEntity<Object> getManGraphData(CurrentUserObject currentUserObject, Long startupId, String fieldName);

	ResponseEntity<?> getManDashInfo(CurrentUserObject currentUserObject);

	ResponseEntity<?> getStartupsDashInfo(CurrentUserObject currentUserObject);

	ResponseEntity<?> getStartupsDashUpcomingEvents(CurrentUserObject currentUserObject, int day, int month, int year,
			String TimeZone);

	ResponseEntity<?> getMngtDashUpcomingEvents(CurrentUserObject currentUserObject, int day, int month, int year,
			String TimeZone);

	ResponseEntity<?> getIntakes(CurrentUserObject currentUserObject, Pageable paging);

	ResponseEntity<?> getStartups(CurrentUserObject currentUserObject, Pageable paging);

	ResponseEntity<?> getStartup(CurrentUserObject currentUserObject, Long startupId);

	ResponseEntity<Object> getStartupListByIntake(CurrentUserObject currentUserObject, Long intakeProgramId,
			Pageable paging);

}
