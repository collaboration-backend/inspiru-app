package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.CalendarEventDto;
import com.stc.inspireu.dtos.GetIntakeProgramDto;
import com.stc.inspireu.dtos.GetStartupDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.mappers.CalendarEventMapper;
import com.stc.inspireu.mappers.IntakeProgramMapper;
import com.stc.inspireu.mappers.StartupMapper;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.DashboardService;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final StartupRepository startupRepository;
    private final ProgessReportRepository progessReportRepository;
    private final AssignmentRepository assignmentRepository;
    private final SurveyRepository surveyRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final UserRepository userRepository;
    private final IntakeProgramRepository intakeProgramRepository;
    private final Utility utility;
    private final KeyValueRepository keyValueRepository;
    private final AcademyRoomRepository academyRoomRepository;
    private final StartupMapper startupMapper;
    private final IntakeProgramMapper intakeProgramMapper;
    private final CalendarEventMapper calendarEventMapper;

    @Override
    @Transactional
    public Object getScheduleCount(CurrentUserObject currentUserObject, Date currentDate) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        long oneToOneMeetingNew;
        long oneToOneMeetingCompleted;
        if (user.getRole().getRoleName().equals(RoleName.ROLE_SUPER_ADMIN)) {
            oneToOneMeetingNew = calendarEventRepository
                .countBySessionStartGreaterThanEqualAndOneToOneMeetingIsNotNull(currentDate);
            oneToOneMeetingCompleted = calendarEventRepository
                .countBySessionStartLessThanAndOneToOneMeetingIsNotNull(currentDate);
        } else {
            oneToOneMeetingNew = calendarEventRepository
                .countBySessionStartGreaterThanEqualAndOneToOneMeetingIsNotNullAndOneToOneMeeting_Trainer_Id(
                    currentDate, user.getId());
            oneToOneMeetingCompleted = calendarEventRepository
                .countBySessionStartLessThanAndOneToOneMeetingIsNotNullAndOneToOneMeeting_Trainer_Id(
                    currentDate, user.getId());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("oneToOneMeetingCompleted", oneToOneMeetingCompleted);
        data.put("oneToOneMeetingNew", oneToOneMeetingNew);
        return data;
    }

    @Override
    @Transactional
    public ResponseEntity<?> getStartupList(CurrentUserObject currentUserObject) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Iterable<Startup> ls = startupRepository.findAll();
        List<GetStartupDto> list = startupMapper.toGetStartupDtoList(ls);
        return ResponseWrapper.response(list);
    }

    @Override
    @Transactional
    public Object getStartupsWiseScheduleCount(CurrentUserObject currentUserObject, Long startupId, Date currentDate) {
        long oneToOneMeetingNew;
        long oneToOneMeetingCompleted;
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getRole().getRoleName().equals(RoleName.ROLE_SUPER_ADMIN)) {
            oneToOneMeetingNew = calendarEventRepository
                .countBySessionStartGreaterThanEqualAndStartup_IdAndOneToOneMeetingIsNotNull(currentDate,
                    startupId);
            oneToOneMeetingCompleted = calendarEventRepository
                .countBySessionStartLessThanAndStartup_IdAndOneToOneMeetingIsNotNull(currentDate, startupId);
        } else {
            oneToOneMeetingNew = calendarEventRepository
                .countBySessionStartGreaterThanEqualAndStartup_IdAndOneToOneMeetingIsNotNullAndOneToOneMeeting_Trainer_Id(
                    currentDate, startupId, user.getId());
            oneToOneMeetingCompleted = calendarEventRepository
                .countBySessionStartLessThanAndStartup_IdAndOneToOneMeetingIsNotNullAndOneToOneMeeting_Trainer_Id(
                    currentDate, startupId, user.getId());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("oneToOneMeetingCompleted", oneToOneMeetingCompleted);
        data.put("oneToOneMeetingNew", oneToOneMeetingNew);
        return data;
    }

    @Override
    @Transactional
    public Object getUpcomingEventSchedules(CurrentUserObject currentUserObject, Date currentDate, String timeZone) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        List<CalendarEvent> calendarEvents;
        if (user.getRole().getRoleName().equals(RoleName.ROLE_SUPER_ADMIN)) {
            calendarEvents = calendarEventRepository.getSuperAdminUpcomingCalendarEventsForManagement(currentDate);
        } else {
            calendarEvents = calendarEventRepository.getTrainerUpcomingCalendarEventsForManagement(currentDate,
                user.getId());
        }
        List<CalendarEventDto> calendarEventDtos = calendarEventMapper.toCalendarEventDtoList(calendarEvents);
        Map<Long, List<CalendarEventDto>> eventGroupByDate = calendarEventDtos.stream()
            .collect(Collectors.groupingBy(CalendarEventDto::getEventDate));
        Map<Long, List<CalendarEventDto>> sortedMap = new TreeMap<>(eventGroupByDate);
        List<Object> eventlist = new ArrayList<>();
        sortedMap.forEach((k, v) -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("date", k);
            map.put("value", v);
            eventlist.add(map);
        });
        return eventlist;
    }

    @Transactional
    @Override
    public ResponseEntity<Object> getGraphData(CurrentUserObject currentUserObject, String fieldName) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            switch (fieldName) {
                case "fundraiseInvestment": {
                    List<Map<String, Object>> ls = progessReportRepository
                        .getByStartupAndFundraiseInvestment(user.getStartup().getId());
                    Map<String, Object> d = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("fundraiseInvestment", ls);
                        }
                    };
                    return ResponseWrapper.response(d);
                }
                case "marketValue": {
                    List<Map<String, Object>> ls = progessReportRepository
                        .getByStartupAndMarketValue(user.getStartup().getId());
                    Map<String, Object> d = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("marketValue", ls);
                        }
                    };
                    return ResponseWrapper.response(d);
                }
                case "sales": {
                    List<Map<String, Object>> ls1 = progessReportRepository
                        .getByStartupAndSales(user.getStartup().getId());
                    List<Map<String, Object>> ls2 = progessReportRepository
                        .getByStartupAndSalesEx(user.getStartup().getId());
                    Map<String, Object> d = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("sales", ls1);
                            put("salesExpected", ls2);
                        }
                    };
                    return ResponseWrapper.response(d);
                }
                case "revenue": {
                    List<Map<String, Object>> ls1 = progessReportRepository
                        .getByStartupAndRevenue(user.getStartup().getId());
                    List<Map<String, Object>> ls2 = progessReportRepository
                        .getByStartupAndRevenueEx(user.getStartup().getId());
                    Map<String, Object> d = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("revenue", ls1);
                            put("revenueExpected", ls2);
                        }
                    };
                    return ResponseWrapper.response(d);
                }
                case "loans": {
                    List<Map<String, Object>> ls = progessReportRepository
                        .getByStartupAndLoans(user.getStartup().getId());
                    Map<String, Object> d = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("loans", ls);
                        }
                    };
                    return ResponseWrapper.response(d);
                }
                case "fteEmployees": {
                    List<Map<String, Object>> ls1 = progessReportRepository
                        .getByStartupAndFteEmployees(user.getStartup().getId());
                    List<Map<String, Object>> ls2 = progessReportRepository
                        .getByStartupAndFteEmployeesEx(user.getStartup().getId());
                    Map<String, Object> d = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("fteEmployees", ls1);
                            put("fteEmployeesExpected", ls2);
                        }
                    };
                    return ResponseWrapper.response(d);
                }
                case "pteEmployees": {
                    List<Map<String, Object>> ls1 = progessReportRepository
                        .getByStartupAndPteEmployees(user.getStartup().getId());
                    List<Map<String, Object>> ls2 = progessReportRepository
                        .getByStartupAndPteEmployeesEx(user.getStartup().getId());
                    Map<String, Object> d = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("pteEmployees", ls1);
                            put("pteEmployeesExpected", ls2);
                        }
                    };
                    return ResponseWrapper.response(d);
                }
                case "freelancers": {
                    List<Map<String, Object>> ls1 = progessReportRepository
                        .getByStartupAndFreelancers(user.getStartup().getId());
                    List<Map<String, Object>> ls2 = progessReportRepository
                        .getByStartupAndFreelancersEx(user.getStartup().getId());
                    Map<String, Object> d = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("freelancers", ls1);
                            put("freelancersExpected", ls2);
                        }
                    };
                    return ResponseWrapper.response(d);
                }
                case "users": {
                    List<Map<String, Object>> ls1 = progessReportRepository
                        .getByStartupAndCustomers(user.getStartup().getId());
                    List<Map<String, Object>> ls2 = progessReportRepository
                        .getByStartupAndCustomersEx(user.getStartup().getId());
                    Map<String, Object> d = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("users", ls1);
                            put("usersExpected", ls2);
                        }
                    };
                    return ResponseWrapper.response(d);
                }
                case "profitLoss": {
                    List<Map<String, Object>> ls1 = progessReportRepository
                        .getByStartupAndProfitOrLoss(user.getStartup().getId());
                    List<Map<String, Object>> ls2 = progessReportRepository
                        .getByStartupAndProfitOrLossEx(user.getStartup().getId());
                    Map<String, Object> d = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("profitLoss", ls1);
                            put("profitLossExpected", ls2);
                        }
                    };
                    return ResponseWrapper.response(d);
                }
                case "highGrossMerchandise": {
                    List<Map<String, Object>> ls = progessReportRepository
                        .getByStartupAndHighGrossMerchandise(user.getStartup().getId());
                    Map<String, Object> d = new HashMap<String, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("highGrossMerchandise", ls);
                        }
                    };
                    return ResponseWrapper.response(d);
                }
                default:
                    return ResponseWrapper.response400("invalid fieldName", "fieldName");
            }
        } else {
            return ResponseWrapper.response(currentUserObject.getUserId() + " not found", "userId",
                HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<Object> getManGraphData(CurrentUserObject currentUserObject, Long startupId,
                                                  String fieldName) {
        switch (fieldName) {
            case "fundraiseInvestment": {
                List<Map<String, Object>> ls = progessReportRepository.getByStartupAndFundraiseInvestment(startupId);
                Map<String, Object> d = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("fundraiseInvestment", ls);
                    }
                };
                return ResponseWrapper.response(d);
            }
            case "marketValue": {
                List<Map<String, Object>> ls = progessReportRepository.getByStartupAndMarketValue(startupId);
                Map<String, Object> d = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("marketValue", ls);
                    }
                };
                return ResponseWrapper.response(d);
            }
            case "sales": {
                List<Map<String, Object>> ls1 = progessReportRepository.getByStartupAndSales(startupId);
                List<Map<String, Object>> ls2 = progessReportRepository.getByStartupAndSalesEx(startupId);
                Map<String, Object> d = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("sales", ls1);
                        put("salesExpected", ls2);
                    }
                };
                return ResponseWrapper.response(d);
            }
            case "revenue": {
                List<Map<String, Object>> ls1 = progessReportRepository.getByStartupAndRevenue(startupId);
                List<Map<String, Object>> ls2 = progessReportRepository.getByStartupAndRevenueEx(startupId);
                Map<String, Object> d = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("revenue", ls1);
                        put("revenueExpected", ls2);
                    }
                };
                return ResponseWrapper.response(d);
            }
            case "loans": {
                List<Map<String, Object>> ls = progessReportRepository.getByStartupAndLoans(startupId);
                Map<String, Object> d = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("loans", ls);
                    }
                };
                return ResponseWrapper.response(d);
            }
            case "fteEmployees": {
                List<Map<String, Object>> ls1 = progessReportRepository.getByStartupAndFteEmployees(startupId);
                List<Map<String, Object>> ls2 = progessReportRepository.getByStartupAndFteEmployeesEx(startupId);
                Map<String, Object> d = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("fteEmployees", ls1);
                        put("fteEmployeesExpected", ls2);
                    }
                };
                return ResponseWrapper.response(d);
            }
            case "pteEmployees": {
                List<Map<String, Object>> ls1 = progessReportRepository.getByStartupAndPteEmployees(startupId);
                List<Map<String, Object>> ls2 = progessReportRepository.getByStartupAndPteEmployeesEx(startupId);
                Map<String, Object> d = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("pteEmployees", ls1);
                        put("pteEmployeesExpected", ls2);
                    }
                };
                return ResponseWrapper.response(d);
            }
            case "freelancers": {
                List<Map<String, Object>> ls1 = progessReportRepository.getByStartupAndFreelancers(startupId);
                List<Map<String, Object>> ls2 = progessReportRepository.getByStartupAndFreelancersEx(startupId);
                Map<String, Object> d = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("freelancers", ls1);
                        put("freelancersExpected", ls2);
                    }
                };
                return ResponseWrapper.response(d);
            }
            case "users": {
                List<Map<String, Object>> ls1 = progessReportRepository.getByStartupAndCustomers(startupId);
                List<Map<String, Object>> ls2 = progessReportRepository.getByStartupAndCustomersEx(startupId);
                Map<String, Object> d = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("users", ls1);
                        put("usersExpected", ls2);
                    }
                };
                return ResponseWrapper.response(d);
            }
            case "profitLoss": {
                List<Map<String, Object>> ls1 = progessReportRepository.getByStartupAndProfitOrLoss(startupId);
                List<Map<String, Object>> ls2 = progessReportRepository.getByStartupAndProfitOrLossEx(startupId);
                Map<String, Object> d = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("profitLoss", ls1);
                        put("profitLossExpected", ls2);
                    }
                };
                return ResponseWrapper.response(d);
            }
            case "highGrossMerchandise": {
                List<Map<String, Object>> ls = progessReportRepository.getByStartupAndHighGrossMerchandise(startupId);
                Map<String, Object> d = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("highGrossMerchandise", ls);
                    }
                };
                return ResponseWrapper.response(d);
            }
            default:
                return ResponseWrapper.response400("invalid fieldName", "fieldName");
        }

    }

    @Transactional
    @Override
    public ResponseEntity<?> getManDashInfo(CurrentUserObject currentUserObject) {
        Map<String, Object> data = new HashMap<>();
        data.put("startupsIncubated", 0);
        KeyValue totalStartups = keyValueRepository.findByKeyName(Constant.TOTAL_STARTUPS_INCUBATED.toString());
        if (totalStartups != null) {
            String count = totalStartups.getValueName();
            int c = Integer.parseInt(count);
            data.put("startupsIncubated", c);
        }
        data.put("marketValue", 0);
        Integer tmv = progessReportRepository.sumMarketValue();
        if (tmv != null) {
            data.put("marketValue", tmv);
        }
        data.put("fundRaised", 0);
        Integer tf = progessReportRepository.sumFundraiser();
        if (tf != null) {
            data.put("fundRaised", tf);
        }
        data.put("transactions", 0);
        data.put("intakeCompleted", 0);
        Long itc = intakeProgramRepository.countIntakeByDate(Constant.PUBLISHED.toString(), new Date());
        if (itc != null) {
            data.put("intakeCompleted", itc);
        }
        return ResponseWrapper.response(data);
    }

    @Override
    public ResponseEntity<?> getStartupsDashInfo(CurrentUserObject currentUserObject) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Map<String, Object> data = new HashMap<>();
        data.put("Assignments", assignmentRepository.countBySubmittedStartupId(user.getStartup().getId()));
        data.put("Survey", surveyRepository.countBySubmittedStartupId(user.getStartup().getId()));
        data.put("mileStone", "0");
        long c = academyRoomRepository.countByStartup_IdAndSessionEndLessThan(user.getStartup().getId(),
            new Date());
        data.put("Acadamics", c);
        return ResponseWrapper.response(data);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getStartupsDashUpcomingEvents(CurrentUserObject currentUserObject, int day, int month,
                                                           int year, String timeZone) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            Map<String, Date> startAndEnd = utility.atStartAndEndOfDayWithTz(day, month, year, timeZone);
            List<CalendarEvent> result = calendarEventRepository.getCalendarEventsByStartupIdAndMonth(
                user.getStartup().getId(), startAndEnd.get("start"), startAndEnd.get("end"));
            List<CalendarEvent> calendarEvents2 = calendarEventRepository.getDayCalendarEventsByStartupIdAndMonth(
                user.getStartup().getIntakeProgram().getId(), startAndEnd.get("start"));
            List<CalendarEvent> newList = Stream.concat(result.stream(), calendarEvents2.stream())
                .collect(Collectors.toList());
            List<CalendarEventDto> calendarEventDtos = calendarEventMapper.toCalendarEventDtoList(newList);
            return ResponseWrapper.response(calendarEventDtos);
        }
        return ResponseWrapper.response(currentUserObject.getUserId() + " not found", "userId", HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getMngtDashUpcomingEvents(CurrentUserObject currentUserObject, int day, int month,
                                                       int year, String timeZone) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Map<String, Date> startAndEnd = utility.atStartAndEndOfDayWithTz(day, month, year, timeZone);
        List<CalendarEvent> result = calendarEventRepository.findBySessionStartBetween(startAndEnd.get("start"),
            startAndEnd.get("end"));
        List<CalendarEvent> calendarEvents2 = calendarEventRepository
            .getDayCalendarEvents(startAndEnd.get("start"));
        List<CalendarEvent> newList = new ArrayList<>();
        Set<Long> dedup = new HashSet<Long>();
        for (CalendarEvent i : result) {
            if (!dedup.contains(i.getId())) {
                dedup.add(i.getId());
                newList.add(i);
            }
        }
        for (CalendarEvent i : calendarEvents2) {
            if (!dedup.contains(i.getId())) {
                dedup.add(i.getId());
                newList.add(i);
            }
        }
        List<CalendarEventDto> calendarEventDtos = calendarEventMapper.toCalendarEventDtoList(newList);
        return ResponseWrapper.response(calendarEventDtos);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getIntakes(CurrentUserObject currentUserObject, Pageable paging) {
        Page<IntakeProgram> ls = intakeProgramRepository.findByStatus(Constant.PUBLISHED.toString(), paging);
        Page<GetIntakeProgramDto> list = ls.map(intakeProgramMapper::toGetIntakeProgramDto);
        return ResponseWrapper.response(list);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getStartups(CurrentUserObject currentUserObject, Pageable paging) {
        Page<Startup> ls = startupRepository.findAll(paging);
        Page<GetStartupDto> list = ls.map(startupMapper::toGetStartupDto);
        return ResponseWrapper.response(list);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getStartup(CurrentUserObject currentUserObject, Long startupId) {
        return startupRepository.findById(startupId)
            .map(startup -> ResponseWrapper.response(startupMapper.toGetStartupDto(startup)))
            .orElse(null);
    }

    @Transactional
    @Override
    public ResponseEntity<Object> getStartupListByIntake(CurrentUserObject currentUserObject, Long intakeProgramId,
                                                         Pageable paging) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<Startup> ls = startupRepository.findByIntakeProgram_IdAndIsRealTrue(intakeProgramId, paging);
        Page<GetStartupDto> list = ls.map(startupMapper::toGetStartupDto);
        return ResponseWrapper.response(list);
    }

}
