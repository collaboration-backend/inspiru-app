package com.stc.inspireu.controllers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.CalendarEventDto;
import com.stc.inspireu.dtos.GetJudgeCalenderEventsDto;
import com.stc.inspireu.services.CalendarEventService;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/${api.version}")
public class CalendarEventController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CalendarEventService calendarEventService;

    @GetMapping("management/calendarEvents/{year}/{month}")
    public ResponseEntity<Object> getCalendarEvents(HttpServletRequest httpServletRequest, @PathVariable Integer month,
                                                    @PathVariable Integer year, @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<CalendarEventDto> calendarEventDtos = calendarEventService
            .getManagementCalendarEventsByMonth(currentUserObject, month, year, timezone);
        return ResponseWrapper.response(calendarEventDtos);

    }

    @GetMapping("management/judge/calendarEvents/{year}/{month}")
    public ResponseEntity<Object> getJudgeCalendarEvents(HttpServletRequest httpServletRequest,
                                                         @PathVariable Integer month, @PathVariable Integer year,
                                                         @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<GetJudgeCalenderEventsDto> calendarEventDtos = calendarEventService
            .getJudgeCalendarEventsByMonth(currentUserObject, month, year, timezone);
        return ResponseWrapper.response(calendarEventDtos);

    }

    @GetMapping("management/calendarEvents/{year}/{month}/{day}")
    public ResponseEntity<Object> getCalendarEventsByDate(HttpServletRequest httpServletRequest,
                                                          @PathVariable Integer day, @PathVariable Integer month, @PathVariable Integer year,
                                                          @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<CalendarEventDto> calendarEventDtos = calendarEventService
            .getManagementCalendarEventsByDay(currentUserObject, day, month, year, timezone);
        return ResponseWrapper.response(calendarEventDtos);

    }

    @GetMapping("management/judge/calendarEvents/{year}/{month}/{day}")
    public ResponseEntity<Object> getJudgeCalendarEventsByDate(HttpServletRequest httpServletRequest,
                                                               @PathVariable Integer day, @PathVariable Integer month, @PathVariable Integer year,
                                                               @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<GetJudgeCalenderEventsDto> calendarEventDtos = calendarEventService
            .getJudgeManagementCalendarEventsByDay(currentUserObject, day, month, year, timezone);
        return ResponseWrapper.response(calendarEventDtos);

    }

}
