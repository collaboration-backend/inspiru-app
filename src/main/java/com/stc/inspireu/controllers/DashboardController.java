package com.stc.inspireu.controllers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.services.DashboardService;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;
import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/${api.version}")
public class DashboardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String CURRENT_USER_OBJECT = "currentUserObject";

    private final DashboardService dashboardService;

    @GetMapping("management/schedules/total")
    public ResponseEntity<Object> getScheduleCount(HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Date currentDate = DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH);
        Object obj = dashboardService.getScheduleCount(currentUserObject, currentDate);
        return ResponseWrapper.response(obj);
    }

    @GetMapping("management/dashboard/intakePrograms/{intakeProgramId}/startups")
    public ResponseEntity<Object> getStartupListByIntake(HttpServletRequest httpServletRequest,
                                                         @PathVariable Long intakeProgramId,
                                                         @RequestParam(defaultValue = "0") Integer pageNo,
                                                         @RequestParam(defaultValue = "500") Integer pageSize,
                                                         @RequestParam(defaultValue = "desc") String sortDir) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, 1000, Sort.by("createdOn"));

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("createdOn").descending());
        }

        return dashboardService.getStartupListByIntake(currentUserObject, intakeProgramId, paging);
    }

    @GetMapping("management/startups")
    public ResponseEntity<?> getStartupList(HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return dashboardService.getStartupList(currentUserObject);
    }

    @GetMapping("management/startups/{startupId}/schedules/total")
    public ResponseEntity<Object> getStartupsWiseScheduleCount(HttpServletRequest httpServletRequest,
                                                               @PathVariable Long startupId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Date currentDate = DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH);
        Object obj = dashboardService.getStartupsWiseScheduleCount(currentUserObject, startupId, currentDate);
        return ResponseWrapper.response(obj);
    }

    @GetMapping("management/schedules/upcoming")
    public ResponseEntity<Object> getUpcomingSchedules(HttpServletRequest httpServletRequest,
                                                       @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Date currentDate = DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH);
        Object obj = dashboardService.getUpcomingEventSchedules(currentUserObject, currentDate, timezone);
        return ResponseWrapper.response(obj);
    }

    @GetMapping("management/startups/{startupId}/progressReports/graphs/{fieldName}")
    public ResponseEntity<Object> getGraphData(HttpServletRequest httpServletRequest,
                                               @PathVariable Long startupId,
                                               @PathVariable String fieldName) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return dashboardService.getManGraphData(currentUserObject, startupId, fieldName);

    }

    @GetMapping("startups/progressReports/graphs/{fieldName}")
    public ResponseEntity<Object> getGraphData(HttpServletRequest httpServletRequest,
                                               @PathVariable String fieldName) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return dashboardService.getGraphData(currentUserObject, fieldName);

    }

    @GetMapping("management/dashboard/info")
    public ResponseEntity<?> getManDashInfo(HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return dashboardService.getManDashInfo(currentUserObject);
    }

    @GetMapping("management/dashboard/intakePrograms")
    public ResponseEntity<?> getIntakes(HttpServletRequest httpServletRequest,
                                        @RequestParam(defaultValue = "0") Integer pageNo,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(defaultValue = "desc") String sortDir) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("id"));

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
        }

        return dashboardService.getIntakes(currentUserObject, paging);
    }

    @GetMapping("management/dashboard/startups")
    public ResponseEntity<?> getStartups(HttpServletRequest httpServletRequest,
                                         @RequestParam(defaultValue = "0") Integer pageNo,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(defaultValue = "desc") String sortDir) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("id"));

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
        }

        return dashboardService.getStartups(currentUserObject, paging);
    }

    @GetMapping("management/dashboard/startups/{startupId}")
    public ResponseEntity<?> getStartup(HttpServletRequest httpServletRequest, @PathVariable Long startupId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return dashboardService.getStartup(currentUserObject, startupId);
    }

    @GetMapping("startups/dashboard/info")
    public ResponseEntity<?> getStartupsDashInfo(HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return dashboardService.getStartupsDashInfo(currentUserObject);
    }

    @GetMapping("startups/dashboard/upcomingEvents")
    public ResponseEntity<?> getCalendarEventsByDate(HttpServletRequest httpServletRequest,
                                                     @RequestParam() Integer day,
                                                     @RequestParam() Integer month,
                                                     @RequestParam() Integer year,
                                                     @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return dashboardService.getStartupsDashUpcomingEvents(currentUserObject, day, month, year, timezone);
    }

    @GetMapping("management/dashboard/upcomingEvents")
    public ResponseEntity<?> getMngtCalendarEventsByDate(HttpServletRequest httpServletRequest,
                                                         @RequestParam() Integer day,
                                                         @RequestParam() Integer month,
                                                         @RequestParam() Integer year,
                                                         @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return dashboardService.getMngtDashUpcomingEvents(currentUserObject, day, month, year, timezone);
    }

}
