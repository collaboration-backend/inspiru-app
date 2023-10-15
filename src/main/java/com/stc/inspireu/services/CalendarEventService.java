package com.stc.inspireu.services;

import java.util.List;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.CalendarEventDto;
import com.stc.inspireu.dtos.GetJudgeCalenderEventsDto;

public interface CalendarEventService {

	List<CalendarEventDto> getManagementCalendarEventsByMonth(CurrentUserObject currentUserObject, Integer month,
			Integer year, String timezone);

	List<GetJudgeCalenderEventsDto> getJudgeCalendarEventsByMonth(CurrentUserObject currentUserObject, Integer month,
			Integer year, String timezone);

	List<CalendarEventDto> getManagementCalendarEventsByDay(CurrentUserObject currentUserObject, Integer day,
			Integer month, Integer year, String timezone);

	List<GetJudgeCalenderEventsDto> getJudgeManagementCalendarEventsByDay(CurrentUserObject currentUserObject,
			Integer day, Integer month, Integer year, String timezone);

}
