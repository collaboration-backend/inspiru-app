package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.CalendarEventDto;
import com.stc.inspireu.dtos.GetJudgeCalenderEventsDto;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.mappers.CalendarEventMapper;
import com.stc.inspireu.mappers.JudgeCalendarMapper;
import com.stc.inspireu.models.CalendarEvent;
import com.stc.inspireu.models.JudgeCalendar;
import com.stc.inspireu.models.User;
import com.stc.inspireu.repositories.CalendarEventRepository;
import com.stc.inspireu.repositories.JudgeCalenderRepository;
import com.stc.inspireu.repositories.UserRepository;
import com.stc.inspireu.services.CalendarEventService;
import com.stc.inspireu.utils.RoleName;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CalendarEventServiceImpl implements CalendarEventService {

    private final Utility utility;
    private final CalendarEventRepository calendarEventRepository;
    private final UserRepository userRepository;
    private final JudgeCalenderRepository judgeCalenderRepository;
    private final JudgeCalendarMapper judgeCalendarMapper;
    private final CalendarEventMapper calendarEventMapper;

    @Override
    @Transactional
    public List<CalendarEventDto> getManagementCalendarEventsByMonth(CurrentUserObject currentUserObject, Integer month,
                                                                     Integer year, String timezone) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Map<String, Date> startAndEnd = utility.atStartAndEndOfMonthWithTz(month, year, timezone);
        List<CalendarEvent> calendarEvents;
        if (user.getRole().getRoleName().equals(RoleName.ROLE_SUPER_ADMIN)) {
            calendarEvents = calendarEventRepository.getCalendarEventsByMonthForSuperAdmin(startAndEnd.get("start"),
                startAndEnd.get("end"));
        } else {
            calendarEvents = calendarEventRepository.getCalendarEventsByMonthForManagement(startAndEnd.get("start"),
                startAndEnd.get("end"), user.getId());
        }
        return calendarEventMapper.toCalendarEventDtoList(calendarEvents);
    }

    @Override
    public List<GetJudgeCalenderEventsDto> getJudgeCalendarEventsByMonth(CurrentUserObject currentUserObject,
                                                                         Integer month, Integer year, String timezone) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Map<String, Date> startAndEnd = utility.atStartAndEndOfMonthWithTz(month, year, timezone);
        List<JudgeCalendar> judgeCalendars = judgeCalenderRepository.getCalendarEventsByMonthForJudge(startAndEnd.get("start"),
            startAndEnd.get("end"), user.getId());
        return judgeCalendarMapper.toGetJudgeCalenderEventsDtoList(judgeCalendars);
    }

    @Override
    @Transactional
    public List<CalendarEventDto> getManagementCalendarEventsByDay(CurrentUserObject currentUserObject, Integer day,
                                                                   Integer month, Integer year, String timezone) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getWillManagement()) {
            Map<String, Date> startAndEnd = utility.atStartAndEndOfDayWithTz(day, month, year, timezone);
            List<CalendarEvent> calendarEvents;
            if (user.getRole().getRoleName().equals(RoleName.ROLE_SUPER_ADMIN)) {
                calendarEvents = calendarEventRepository.getCalendarEventsByDayForSuperAdmin(startAndEnd.get("start"),
                    startAndEnd.get("end"));
            } else {
                calendarEvents = calendarEventRepository.getCalendarEventsByDayForManagement(startAndEnd.get("start"),
                    startAndEnd.get("end"), user.getId());
            }
            calendarEvents.addAll(calendarEventRepository
                .getDayCalendarEvents(startAndEnd.get("start")));
            List<CalendarEvent> newList = new ArrayList<>();
            Set<Long> dedup = new HashSet<>();
            for (CalendarEvent i : calendarEvents) {
                if (!dedup.contains(i.getId())) {
                    dedup.add(i.getId());
                    newList.add(i);
                }
            }
            return calendarEventMapper.toCalendarEventDtoList(newList);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<GetJudgeCalenderEventsDto> getJudgeManagementCalendarEventsByDay(CurrentUserObject currentUserObject,
                                                                                 Integer day, Integer month, Integer year, String timezone) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getWillManagement()) {
            Map<String, Date> startAndEnd = utility.atStartAndEndOfDayWithTz(day, month, year, timezone);
            List<JudgeCalendar> calendarEvents = judgeCalenderRepository.getCalendarEventsByMonthForJudge(startAndEnd.get("start"),
                startAndEnd.get("end"), user.getId());
            List<JudgeCalendar> calendarEvents2 = judgeCalenderRepository.getDayCalendarEvents(startAndEnd.get("start"),
                user.getId());
            List<JudgeCalendar> newList = Stream.concat(calendarEvents.stream(), calendarEvents2.stream())
                .collect(Collectors.toList());
            return judgeCalendarMapper.toGetJudgeCalenderEventsDtoList(newList);
        } else {
            return Collections.emptyList();
        }
    }
}
