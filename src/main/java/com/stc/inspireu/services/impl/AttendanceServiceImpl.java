package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.jpa.projections.ProjectStartupIdAndname;
import com.stc.inspireu.mappers.AcademyRoomMapper;
import com.stc.inspireu.mappers.AttendanceMapper;
import com.stc.inspireu.mappers.UserMapper;
import com.stc.inspireu.mappers.WorkshopSessionMapper;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.AttendanceService;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserRepository userRepository;
    private final Attendance2022Repository attendance2022Repository;
    private final AttendanceRepository attendanceRepository;
    private final OneToOneMeetingRepository oneToOneMeetingRepository;
    private final NotificationServiceImpl notificationService;
    private final Utility utility;
    private final AttendanceRepositoryCustom attendanceRepositoryCustom;
    private final IntakeProgramRepository intakeProgramRepository;
    private final StartupRepository startupRepository;
    private final AcademyRoomRepository academyRoomRepository;
    private final WorkshopSessionRepository workshopSessionRepository;
    private final StartupAttendanceRepository startupAttendanceRepository;
    private final AcademyRoomMapper academyRoomMapper;
    private final AttendanceMapper attendanceMapper;
    private final UserMapper userMapper;
    private final WorkshopSessionMapper workshopSessionMapper;

    @Override
    @Transactional
    public ResponseEntity<?> createStartupMeetingAttendance(CurrentUserObject currentUserObject,
                                                            List<PostAttendanceDto> postAttendanceDto, Long meetingId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<User> coachOrTrainer = userRepository
            .findById(currentUserObject.getUserId() != null ? currentUserObject.getUserId() : (long) 0);
        Optional<OneToOneMeeting> oneToOneMeeting = oneToOneMeetingRepository.findById(meetingId);
        if (coachOrTrainer.isPresent() && oneToOneMeeting.isPresent() && oneToOneMeeting.get().getStartup() != null) {
            if (!Objects.equals(oneToOneMeeting.get().getTrainer().getId(), coachOrTrainer.get().getId())) {
                return ResponseWrapper.response("attendance can be marked only by assigned trainer", "meetingId",
                    HttpStatus.BAD_REQUEST);
            }
            if (!oneToOneMeeting.get().getInvitationStatus().equals(Constant.ACCEPTED.toString())) {
                return ResponseWrapper.response("attendance cannot be marked for unaccepted meetings", "meetingId",
                    HttpStatus.BAD_REQUEST);
            }
            List<Attendance> attendances = attendanceRepository.findByOneToOneMeeting_IdAndAttendanceDate(
                oneToOneMeeting.get().getId(), oneToOneMeeting.get().getSessionStart());
            if (postAttendanceDto.size() > 0) {
                if (attendances.size() > 0) {
                    attendanceRepository.deleteAll(attendances);
                }
                List<Attendance> newAttendances = new ArrayList<>();
                for (PostAttendanceDto memberAttendance : postAttendanceDto) {
                    Optional<User> userMember = userRepository.findById(
                        memberAttendance.getMemberId() != null ? memberAttendance.getMemberId() : (long) 0);
                    if (userMember.isPresent()
                        && Objects.equals(userMember.get().getStartup().getId(), oneToOneMeeting.get().getStartup().getId())) {
                        Attendance attendance = new Attendance();
                        attendance.setAttendanceDate(oneToOneMeeting.get().getSessionStart());
                        attendance.setIsLate(memberAttendance.getIsPresent() ? memberAttendance.getIsLate() : false);
                        attendance.setIsPresent(memberAttendance.getIsPresent());
                        attendance.setOneToOneMeeting(oneToOneMeeting.get());
                        attendance.setSessionStart(oneToOneMeeting.get().getSessionStart());
                        attendance.setSessionEnd(oneToOneMeeting.get().getSessionEnd());
                        attendance.setCreatedUser(user);
                        attendance.setMember(userMember.get());
                        if (memberAttendance.getIsPresent() && memberAttendance.getIsLate()) {
                            long difference_In_Time = oneToOneMeeting.get().getSessionStart().getTime()
                                - oneToOneMeeting.get().getSessionEnd().getTime();
                            Instant startTime = oneToOneMeeting.get().getSessionStart().toInstant();
                            Instant actualStartTime = startTime.plusMillis(Math.round(difference_In_Time / 2));
                            Date lateSessionStart = Date.from(actualStartTime);
                            attendance.setSessionStart(lateSessionStart);
                            notificationService.sendMeetingAttendanceNotification(userMember.get(),
                                coachOrTrainer.get(), userMember.get().getStartup(), Constant.LATE.toString(),
                                oneToOneMeeting.get().getSessionStart().getTime());
                        }
                        if (!memberAttendance.getIsPresent()) {
                            notificationService.sendMeetingAttendanceNotification(userMember.get(),
                                coachOrTrainer.get(), userMember.get().getStartup(), Constant.ABSENT.toString(),
                                oneToOneMeeting.get().getSessionStart().getTime());
                        } else {
                            notificationService.sendMeetingAttendanceNotification(userMember.get(),
                                coachOrTrainer.get(), userMember.get().getStartup(), Constant.PRESENT.toString(),
                                oneToOneMeeting.get().getSessionStart().getTime());
                        }
                        newAttendances.add(attendance);
                    } else {
                        return ResponseWrapper.response("invalid member is provided", "memberId",
                            HttpStatus.BAD_REQUEST);
                    }
                }
                if (newAttendances.size() > 0) {
                    attendanceRepository.saveAll(newAttendances);
                    return ResponseWrapper.response("invalid meetingId", "meetingId", HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            return ResponseWrapper.response("meeting not found", "meetingId", HttpStatus.BAD_REQUEST);
        }
        return ResponseWrapper.response("invalid meetingId", "meetingId", HttpStatus.BAD_REQUEST);
    }

    @Override
    @Transactional
    public List<AttendanceStartupDto> getStartupAttendancesByManagement(CurrentUserObject currentUserObject,
                                                                        Long attendanceDate, Pageable paging, String searchBy, String filterBy, Long academyRoomId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getWillManagement()) {
            Date searchDate = new Date(attendanceDate);
            List<AttendanceStartupDto> attendancelist = attendanceRepositoryCustom.getStartupWiseAttendances(user,
                academyRoomId, searchDate, paging, filterBy, searchBy);
            attendancelist.forEach(a -> {
                if (a.getOneToOneMeetingId() != (long) 0) {
                    List<Attendance> attendanceMembers = attendanceRepository
                        .findByOneToOneMeeting_IdAndAttendanceDate(a.getOneToOneMeetingId(), searchDate);
                    a.setMembersCount((long) attendanceMembers.size());
                    a.setMembersAttended(attendanceMapper.toUserAttendanceDtoList(attendanceMembers));
                    a.setName(attendanceMembers.get(0).getOneToOneMeeting().getMeetingName());
                    a.setMarkedDate(ZonedDateTime.of(attendanceMembers.get(0).getCreatedOn(), ZoneId.systemDefault()).toInstant().toEpochMilli());
                    a.setStartupName(attendanceMembers.get(0).getOneToOneMeeting().getStartup().getStartupName());
                    a.setStartupId(attendanceMembers.get(0).getOneToOneMeeting().getStartup().getId());
                }
            });
            return attendancelist;
        }
        return null;
    }

    @Transactional
    @Override
    public ResponseEntity<?> getStartupUsersAttendancePercent(CurrentUserObject currentUserObject, Pageable paging,
                                                              Integer month, Integer year) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        List<User> users = new ArrayList<>();
        if (user.getRole().getRoleName().contains("ADMIN")) {
            users = userRepository.getMembers(user.getStartup().getId());
        } else {
            users.add(user);
        }
        List<UserAttendancePercentDto> userAttendanceDtos = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = cal.getTime();
        for (User userFetched : users) {
            UserAttendancePercentDto userAttendanceDto = new UserAttendancePercentDto();
            userAttendanceDto.setId(userFetched.getId());
            userAttendanceDto.setMemberName(userFetched.getAlias());
            userAttendanceDto.setStartupName(userFetched.getStartup().getStartupName());
            userAttendanceDto.setAttencendancePercent((float) 0);
            userAttendanceDto.setMemberId(userFetched.getId());
            // calculate attendance percent for a particular user
            Integer totalDays = attendanceRepository.countByMember_IdAndAttendanceDateBetween(userFetched.getId(),
                startDate, endDate);
            Integer presentDays = attendanceRepository.countByMember_IdAndIsPresentAndAttendanceDateBetween(
                userFetched.getId(), true, startDate, endDate);
            if (totalDays > 0 && presentDays > 0) {
                Float percent = (float) ((presentDays / totalDays) * 100);
                userAttendanceDto.setAttencendancePercent(percent);
            }
            userAttendanceDtos.add(userAttendanceDto);
        }
        return ResponseWrapper.response(userAttendanceDtos);
    }

    @Override
    @Transactional
    public ResponseEntity<?> getStartupUsersAttendance(CurrentUserObject currentUserObject, Long memberId,
                                                       Integer month, Integer year) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        User member = userRepository.findById(memberId).orElseThrow(() -> new CustomRunTimeException("Member not found"));
        if (Objects.equals(user.getStartup().getId(), member.getStartup().getId())) {
            // Millseconds in a day
            final long ONE_DAY_MILLI_SECONDS = 24 * 60 * 60 * 1000;
            List<UserAttendanceDto> userAttendanceDtos = new ArrayList<UserAttendanceDto>();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startDate = cal.getTime();
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date endDate = new Date(cal.getTime().getTime() + ONE_DAY_MILLI_SECONDS);
            Date currentDate = startDate;
            DateFormat dfDay = new SimpleDateFormat("dd");
            while (currentDate.before(endDate)) {
                UserAttendanceDto userAttendanceDto = new UserAttendanceDto();
                userAttendanceDto.setId(member.getId());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentDate);
                userAttendanceDto.setAttendanceDate(currentDate.toInstant().toEpochMilli());
                userAttendanceDto.setDay(dfDay.format(currentDate));
                userAttendanceDtos.add(userAttendanceDto);
                calendar.add(Calendar.DATE, 1);
                currentDate = calendar.getTime();
                userAttendanceDto.setMemberId(member.getId());
            }
            // get user attendance
            List<Attendance> userAttendances = attendanceRepository
                .findByMember_IdAndAttendanceDateBetween(member.getId(), startDate, endDate);
            for (Attendance attendance : userAttendances) {
                userAttendanceDtos.stream()
                    .filter(ua -> ua.getAttendanceDate() == attendance.getAttendanceDate().getTime()).forEach(a -> {
                        if (attendance.getIsPresent()) {
                            a.setStatus(attendance.getIsLate() ? Constant.LATE.toString()
                                : Constant.PRESENT.toString());
                        } else {
                            a.setStatus(Constant.ABSENT.toString());
                        }
                    });
            }
            return ResponseWrapper.response(userAttendanceDtos);
        } else {
            return ResponseWrapper.response400("invalid userId", "userId");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> getMemberMeetingsByDate(CurrentUserObject currentUserObject, Long memberId, Long date) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        User member = userRepository.findById(memberId).orElseThrow(() -> new CustomRunTimeException("Member not found"));
        if (Objects.equals(user.getStartup().getId(), member.getStartup().getId())) {
            Date searchDate = new Date(date);
            List<Attendance> userAttendances = attendanceRepository
                .findByMember_IdAndAttendanceDate(member.getId(), searchDate);
            return ResponseWrapper.response(attendanceMapper.toAttendanceEventDetailDtoList(userAttendances));
        }
        return ResponseWrapper.response(null);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getStartupOverallAttendanceByMonth(CurrentUserObject currentUserObject, Integer month,
                                                                Integer year) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startDate = cal.getTime();
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date endDate = cal.getTime();
            Integer totalDays = attendanceRepository.countByMember_Startup_IdAndAttendanceDateBetween(
                user.getStartup().getId(), startDate, endDate);
            Integer presentDays = attendanceRepository.countByMember_Startup_IdAndIsPresentAndAttendanceDateBetween(
                user.getStartup().getId(), true, startDate, endDate);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("startupName", user.getStartup().getStartupName());
            map.put("overAllPercent", (float) 0);
            map.put("month", month);
            map.put("year", year);
            if (totalDays > 0 && presentDays > 0) {
                Float percent = (float) ((presentDays / totalDays) * 100);
                map.put("overAllPercent", percent);
            }
            return ResponseWrapper.response(map);
        }
        return ResponseWrapper.response(null);
    }

    @Transactional
    @Override
    public Object exportStartUpOverallAttendanceByMonth(CurrentUserObject currentUserObject, Integer month,
                                                        Integer year) {
        return null;
    }

    @Transactional
    @Override
    public ResponseEntity<?> excelExport(CurrentUserObject currentUserObject, Integer month, Integer year,
                                         Long memberId, String fileFormat, String timeZone) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = cal.getTime();
        Map<String, String> result = new HashMap<>();
        if (memberId == 0) {
            List<Attendance> attendanceData = attendanceRepository.findByAttendanceDateBetween(startDate, endDate);
            if (!attendanceData.isEmpty()) {
                List<AttendanceEventDetailDto> attendanceEventDetail = attendanceMapper
                    .toAttendanceEventDetailDtoList(attendanceData);
                Map<String, Object[]> data = new TreeMap<>();
                data.put("1", utility.getDateByMonth(month, year));
                Map<Long, List<AttendanceEventDetailDto>> attendanceGroupByMemberId = attendanceEventDetail.stream()
                    .collect(Collectors.groupingBy(AttendanceEventDetailDto::getMemberID));
                int j = 1;
                for (Long key : attendanceGroupByMemberId.keySet()) {
                    j = j + 1;
                    String count = Integer.toString(j);
                    data.put(count, setValues(attendanceGroupByMemberId.get(key), startDate.toInstant().atZone(ZoneId.of(timeZone)).toLocalDate(),
                        endDate.toInstant().atZone(ZoneId.of(timeZone)).toLocalDate()).toArray());
                }
                result = utility.createExcelFile(data, fileFormat);
            } else {
                result.put("result", "No data");
            }
        } else {
            Map<String, Object[]> data = new TreeMap<>();
            data.put("1", utility.getDateByMonth(month, year));
            List<Attendance> attendanceDetailsByMemberId = attendanceRepository
                .findByMember_IdAndAttendanceDateBetween(memberId, startDate, endDate);
            if (!attendanceDetailsByMemberId.isEmpty()) {
                List<AttendanceEventDetailDto> attendanceEventDetail = attendanceMapper
                    .toAttendanceEventDetailDtoList(attendanceDetailsByMemberId);
                List<AttendanceEventDetailDto> attendanceSortedByDateAndMemberId = attendanceEventDetail.stream()
                    .sorted(Comparator.comparing(AttendanceEventDetailDto::getAttendanceDate))
                    .collect(Collectors.toList());
                LocalDate firstDate = startDate.toInstant().atZone(ZoneId.of(timeZone)).toLocalDate();
                LocalDate lastDate = endDate.toInstant().atZone(ZoneId.of(timeZone)).toLocalDate();
                Object[] eventObj = new Object[]{attendanceSortedByDateAndMemberId.get(0).getMemberName()};
                ArrayList<Object> newObj = new ArrayList<>(Arrays.asList(eventObj));
                for (LocalDate date = firstDate; date.isBefore(lastDate.plusDays(1)); date = date.plusDays(1)) {
                    StringBuilder meetingAttendance = new StringBuilder();
                    for (AttendanceEventDetailDto attendanceEventDetailDto : attendanceSortedByDateAndMemberId) {
                        Long attendanceDate = attendanceEventDetailDto.getAttendanceLongDate();
                        LocalDate dateToCheck = Instant.ofEpochMilli(attendanceDate).atZone(ZoneId.systemDefault())
                            .toLocalDate();
                        if (date.equals(dateToCheck)) {
                            Map<String, Object> oneToOneMeeting = attendanceEventDetailDto
                                .getOneToOneMeeting();
                            String eventTitle = oneToOneMeeting.get("title").toString();
                            Date startTime = new Date((Long) oneToOneMeeting.get("sessionStart"));
                            DateFormat dateFormat = new SimpleDateFormat("hh:mm");
                            String strtime = dateFormat.format(startTime);
                            Date endTime = new Date((Long) oneToOneMeeting.get("sessionEnd"));
                            String enTime = dateFormat.format(endTime);
                            if (meetingAttendance.toString().equals("")) {
                                meetingAttendance = new StringBuilder(eventTitle + " " + strtime + "-" + enTime + "|"
                                    + attendanceEventDetailDto.getAttendance());
                            } else {
                                meetingAttendance.append("\n")
                                    .append(eventTitle)
                                    .append(" ")
                                    .append(strtime)
                                    .append("-")
                                    .append(enTime)
                                    .append("|")
                                    .append(attendanceEventDetailDto.getAttendance());
                            }
                        }
                    }
                    if (meetingAttendance.toString().equals("")) {
                        newObj.add("no data");
                    } else {
                        newObj.add(meetingAttendance.toString());
                    }
                }
                data.put("2", newObj.toArray());
                result = utility.createExcelFile(data, fileFormat);
            } else {
                result.put("result", "No data");
            }
        }
        return ResponseWrapper.response(result);
    }

    private List<Object> setValues(List<AttendanceEventDetailDto> attendanceList, LocalDate firstDate, LocalDate lastDate) {
        Object[] eventObj = new Object[]{attendanceList.get(0).getMemberName()};
        ArrayList<Object> newObj = new ArrayList<>(Arrays.asList(eventObj));
        for (LocalDate date = firstDate; date.isBefore(lastDate.plusDays(1)); date = date.plusDays(1)) {
            StringBuilder meetingAttendance = new StringBuilder();
            for (AttendanceEventDetailDto attendanceEventDetailDto : attendanceList) {
                long attendanceDate = attendanceEventDetailDto.getAttendanceLongDate();
                LocalDate dateToCheck = Instant.ofEpochMilli(attendanceDate).atZone(ZoneId.systemDefault())
                    .toLocalDate();
                if (date.equals(dateToCheck)) {
                    Map<String, Object> oneToOneMeeting = attendanceEventDetailDto.getOneToOneMeeting();
                    String eventTitle = oneToOneMeeting.get("title").toString();
                    Date startTime = new Date((Long) oneToOneMeeting.get("sessionStart"));
                    DateFormat dateFormat = new SimpleDateFormat("hh:mm");
                    String strtime = dateFormat.format(startTime);
                    Date endTime = new Date((Long) oneToOneMeeting.get("sessionEnd"));
                    String enTime = dateFormat.format(endTime);
                    if (meetingAttendance.toString().equals("")) {
                        meetingAttendance = new StringBuilder(eventTitle + " " + strtime + "-" + enTime + "|"
                            + attendanceEventDetailDto.getAttendance());
                    } else {
                        meetingAttendance.append("\n")
                            .append(eventTitle)
                            .append(" ")
                            .append(strtime)
                            .append("-")
                            .append(enTime)
                            .append("|")
                            .append(attendanceEventDetailDto.getAttendance());
                    }
                }
            }
            if (meetingAttendance.toString().equals("")) {
                newObj.add("no data");
            } else {
                newObj.add(meetingAttendance.toString());
            }
        }
        return newObj;
    }

    @Transactional
    @Override
    public ResponseEntity<?> excelExportManagement(CurrentUserObject currentUserObject, Integer month, Integer year,
                                                   String fileFormat, String timeZone) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = cal.getTime();
        Map<String, String> result = new HashMap<>();
        List<Attendance> attendanceData = attendanceRepository.findByAttendanceDateBetween(startDate, endDate);
        if (!attendanceData.isEmpty()) {
            List<AttendanceEventDetailDto> attendanceEventDetail = attendanceMapper
                .toAttendanceEventDetailDtoList(attendanceData);
            List<Map<String, Object[]>> dataList = new ArrayList<>();
            Map<Long, List<AttendanceEventDetailDto>> attendanceGroupByStartup = attendanceEventDetail.stream()
                .collect(Collectors.groupingBy(AttendanceEventDetailDto::getStartupId));
            for (Long startupKey : attendanceGroupByStartup.keySet()) {
                Map<String, Object[]> data = new TreeMap<String, Object[]>();
                data.put("1", utility.getDateByMonth(month, year));
                Map<Long, List<AttendanceEventDetailDto>> attendanceGroupByMemberId = attendanceGroupByStartup
                    .get(startupKey).stream().collect(Collectors.groupingBy(AttendanceEventDetailDto::getMemberID));
                int j = 1;
                for (Long key : attendanceGroupByMemberId.keySet()) {
                    List<AttendanceEventDetailDto> attendancelist = attendanceGroupByMemberId.get(key);
                    LocalDate firstDate = startDate.toInstant().atZone(ZoneId.of(timeZone)).toLocalDate();
                    LocalDate lastDate = endDate.toInstant().atZone(ZoneId.of(timeZone)).toLocalDate();
                    Object[] eventObj = new Object[]{attendancelist.get(0).getMemberName()};
                    ArrayList<Object> newObj = new ArrayList<>(Arrays.asList(eventObj));
                    for (LocalDate date = firstDate; date.isBefore(lastDate.plusDays(1)); date = date.plusDays(1)) {
                        StringBuilder meetingAttendance = new StringBuilder();
                        for (AttendanceEventDetailDto attendanceEventDetailDto : attendancelist) {
                            Long attendanceDate = attendanceEventDetailDto.getAttendanceLongDate();
                            LocalDate dateToCheck = Instant.ofEpochMilli(attendanceDate).atZone(ZoneId.systemDefault())
                                .toLocalDate();
                            if (date.equals(dateToCheck)) {
                                Map<String, Object> oneToOneMeeting = attendanceEventDetailDto.getOneToOneMeeting();
                                String eventTitle = oneToOneMeeting.get("title").toString();
                                Date startTime = new Date((Long) oneToOneMeeting.get("sessionStart"));
                                DateFormat dateFormat = new SimpleDateFormat("hh:mm");
                                String strtime = dateFormat.format(startTime);
                                Date endTime = new Date((Long) oneToOneMeeting.get("sessionEnd"));
                                String enTime = dateFormat.format(endTime);
                                if (meetingAttendance.toString().equals("")) {
                                    meetingAttendance = new StringBuilder(eventTitle + " " + strtime + "-" + enTime + "|"
                                        + attendanceEventDetailDto.getAttendance());
                                } else {
                                    meetingAttendance.append("\n")
                                        .append(eventTitle)
                                        .append(" ")
                                        .append(strtime)
                                        .append("-")
                                        .append(enTime)
                                        .append("|")
                                        .append(attendanceEventDetailDto.getAttendance());
                                }
                            }
                        }
                        if (meetingAttendance.toString().equals("")) {
                            newObj.add("no data");
                        } else {
                            newObj.add(meetingAttendance.toString());
                        }
                    }
                    j = j + 1;
                    String count = Integer.toString(j);
                    data.put(count, newObj.toArray());

                }
                dataList.add(data);
            }
            result = utility.createExcelFileWithMultipleSheet(dataList, fileFormat);
        } else {
            result.put("result", "No data");
        }
        return ResponseWrapper.response(result);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getMembers(CurrentUserObject currentUserObject, Long startupId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getWillManagement()) {
            List<User> users = userRepository.getMembers(startupId);
            return ResponseWrapper.response(userMapper.toUserDTOList(users));
        } else {
            return ResponseWrapper.response(currentUserObject.getUserId() + " not found", "userId",
                HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> saveAttendancePercentage(CurrentUserObject currentUserObject,
                                                      PostAttendancePercentageDto postAttendanceDto) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (attendance2022Repository
            .existsByIntakeProgram_IdAndStartup_IdAndAcademyRoom_IdAndWorkshopSession_Id(
                postAttendanceDto.getIntakeProgramId(), postAttendanceDto.getStartupId(),
                postAttendanceDto.getAcademyRoomId(), postAttendanceDto.getWorkshopSessionId())) {
            return ResponseWrapper.response400("Already Saved", "workshopSessionId");
        } else {
            Optional<IntakeProgram> ip = intakeProgramRepository.findById(postAttendanceDto.getIntakeProgramId());
            Optional<Startup> s = startupRepository.findById(postAttendanceDto.getStartupId());
            Optional<AcademyRoom> ar = academyRoomRepository.findById(postAttendanceDto.getAcademyRoomId());
            Optional<WorkshopSession> ws = workshopSessionRepository
                .findById(postAttendanceDto.getWorkshopSessionId());
            if (ip.isPresent() && s.isPresent() && ar.isPresent() && ws.isPresent()) {
                Attendance2022 attendance2022 = new Attendance2022();
                attendance2022.setPercentage(postAttendanceDto.getPercentage());
                attendance2022.setIntakeProgram(ip.get());
                attendance2022.setStartup(s.get());
                attendance2022.setAcademyRoom(ar.get());
                attendance2022.setWorkshopSession(ws.get());
                attendance2022Repository.save(attendance2022);
                return ResponseWrapper.response("saved", HttpStatus.OK);
            } else {
                return ResponseWrapper.response400("Not Found",
                    "intakeProgramId|startupId|academyRoomId|workshopSessionId");
            }
        }
    }

    ///////////////////////////////////////////////////////// AttendanceController2022//////////////////////////////////////////////////////////

    @Transactional
    @Override
    public ResponseEntity<?> getWorkshopSessionsByAcademyRoomsAndIntakes(CurrentUserObject currentUserObject,
                                                                         Long intakeProgramId, Long academyRoomId, Long startDate, Pageable paging) {
        Page<ProjectStartupIdAndname> startupIds = startupRepository.getRealStartupByIntake(intakeProgramId, paging);
        List<Long> ll = startupIds.getContent().stream().map(ProjectStartupIdAndname::getId).collect(Collectors.toList());
        long uc = userRepository.getAllUserCount(ll, Constant.REGISTERED.toString());
        List<WorkshopSession> ls;
        if (startDate.intValue() == 0) {
            ls = workshopSessionRepository.findByAcademyRoom_IdAndStartupIdIsNull(academyRoomId);
        } else {
            Date start = utility.atStartOfDay(new Date(startDate));
            Date end = utility.atEndOfDay(new Date(startDate));
            ls = workshopSessionRepository
                .findByAcademyRoom_IdAndSessionStartGreaterThanEqualAndSessionEndLessThanEqualAndStartupIdIsNull(
                    academyRoomId, start, end);
        }
        Map<String, Object> r = new HashMap<>();
        r.put("startups", startupIds.getTotalElements());
        r.put("attendees", uc);
        r.put("workshopSessions", workshopSessionMapper.toGetWorkshopSessionDtoList(ls));
        return ResponseWrapper.response(r);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getAcademyList(CurrentUserObject currentUserObject, Long intakeProgramId,
                                            Pageable paging) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<AcademyRoom> academyRooms = academyRoomRepository.findByIntakeProgramIdAndStatusPublish(intakeProgramId,
            Constant.PUBLISHED.toString(), paging);
        return ResponseWrapper.response(academyRooms.toList().stream().map(academyRoomMapper::toAcademyRoomDTO));
    }

    @Transactional
    @Override
    public ResponseEntity<?> getStartups(CurrentUserObject currentUserObject, Long intakeProgramId, Long academyRoomId,
                                         Long workshopSessionId, Pageable paging) {
        Page<ProjectStartupIdAndname> startupIds = startupRepository.getRealStartupByIntake(intakeProgramId, paging);
        Page<Map<String, Object>> r = startupIds.map((e) -> {
            Map<String, Object> m = new HashMap<>();
            long c = userRepository.getUserCount(e.getId(), Constant.REGISTERED.toString());
            m.put("totalUser", c);
            m.put("startupId", e.getId());
            m.put("startupName", e.getStartupName());
            m.put("profileInfoJson", e.getProfileInfoJson());
            m.put("markedAttendance", 0);
            m.put("totalAttendance", c);
            return m;
        });
        return ResponseWrapper.response(r);
    }

    @Transactional
    @Override
    public ResponseEntity<?> listMembers(CurrentUserObject currentUserObject, Long intakeProgramId, Long academyRoomId,
                                         Long workshopSessionId, Long startupId, Pageable paging) {
        List<User> ll = userRepository.getRegistredMembers(startupId, Constant.REGISTERED.toString());
        StartupAttendance sa = startupAttendanceRepository
            .findByEventTypeAndEventTypeIdAndStartupId(Constant.WS.toString(), workshopSessionId, startupId);
        JSONObject userAttendanceJson = new JSONObject();
        try {
            if (sa != null) {
                userAttendanceJson = new JSONObject(sa.getUserAttendanceJson());
            }
        } catch (Exception e2) {
            LOGGER.error(e2.getLocalizedMessage());
        }
        List<Map<String, Object>> r = new ArrayList<>();
        for (User e : ll) {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("alias", e.getAlias());
            m.put("email", e.getEmail());
            m.put("profilePic", e.getProfilePic());
            m.put("isPresent", null);
            if (userAttendanceJson.has(e.getId() + "")) {
                m.put("isPresent", userAttendanceJson.get(e.getId() + ""));
            }
            r.add(m);
        }
        return ResponseWrapper.response(r);
    }

    @Transactional
    @Override
    public ResponseEntity<?> markAttendance(CurrentUserObject currentUserObject, MarkAttendanceDto markAttendanceDto) {
        StartupAttendance sa = startupAttendanceRepository
            .findByEventTypeAndEventTypeIdAndStartupId(Constant.WS.toString(), markAttendanceDto.getEventTypeId(), markAttendanceDto.getStartupId());
        if (sa != null) {
            sa.setUserAttendanceJson(markAttendanceDto.getUserAttendanceJson());
            startupAttendanceRepository.save(sa);
        } else {
            StartupAttendance e = new StartupAttendance();
            e.setEventType(Constant.WS.toString());
            e.setEventTypeId(markAttendanceDto.getEventTypeId());
            e.setStartupId(markAttendanceDto.getStartupId());
            e.setUserAttendanceJson(markAttendanceDto.getUserAttendanceJson());
        }
        return ResponseWrapper.response(null, "attendance saved");
    }

}
