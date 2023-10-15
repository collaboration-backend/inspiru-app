package com.stc.inspireu.controllers;

import com.stc.inspireu.annotations.Authorize;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.AttendanceStartupDto;
import com.stc.inspireu.dtos.MarkAttendanceDto;
import com.stc.inspireu.dtos.PostAttendanceDto;
import com.stc.inspireu.dtos.PostAttendancePercentageDto;
import com.stc.inspireu.services.AcademyRoomService;
import com.stc.inspireu.services.AttendanceService;
import com.stc.inspireu.utils.ConstantUtility;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/${api.version}")
public class AttendanceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String CURRENT_USER_OBJECT = "currentUserObject";

    private final AttendanceService attendanceService;

    private final AcademyRoomService academyRoomService;

    @GetMapping("management/attendance/startups/{startupId}/members")
    public ResponseEntity<?> getMembers(HttpServletRequest httpServletRequest, @PathVariable Long startupId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return attendanceService.getMembers(currentUserObject, startupId);
    }

    @PostMapping("management/attendance/meetings/{meetingId}/startups/members")
    public ResponseEntity<?> markAttendance(HttpServletRequest httpServletRequest,
                                            @PathVariable Long meetingId,
                                            @Valid @RequestBody List<PostAttendanceDto> postAttendanceDto,
                                            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return attendanceService.createStartupMeetingAttendance(currentUserObject, postAttendanceDto, meetingId);
    }

    @PostMapping("management/attendance/percentage")
    public ResponseEntity<?> markAttendancePercentage(HttpServletRequest httpServletRequest,
                                                      @RequestBody PostAttendancePercentageDto postAttendancePercentageDto,
                                                      BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return attendanceService.saveAttendancePercentage(currentUserObject, postAttendancePercentageDto);
    }

    @GetMapping("management/attendance/{attendanceDate}")
    public ResponseEntity<Object> getAttendance(HttpServletRequest httpServletRequest,
                                                @PathVariable Long attendanceDate,
                                                @RequestParam(defaultValue = "0") Integer pageNo,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                @RequestParam(defaultValue = "asc") String sortDir,
                                                @RequestParam(defaultValue = "all") String filterBy,
                                                @RequestParam(defaultValue = "all") String searchBy,
                                                @RequestParam(defaultValue = "0") Long academyRoomId,
                                                @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("attendanceDate").ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("attendanceDate").descending());
        }

        List<AttendanceStartupDto> list = attendanceService.getStartupAttendancesByManagement(currentUserObject,
            attendanceDate, paging, searchBy, filterBy, academyRoomId);

        return ResponseWrapper.response(list);
    }

    @GetMapping("startups/attendance/members/{memberId}")
    public ResponseEntity<?> getMemberAttendanceByMonth(HttpServletRequest httpServletRequest,
                                                        @PathVariable Long memberId,
                                                        @RequestParam(defaultValue = "1") Integer month,
                                                        @RequestParam(defaultValue = "2021") Integer year) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return attendanceService.getStartupUsersAttendance(currentUserObject, memberId, month, year);
    }

    @GetMapping("startups/attendance/members")
    public ResponseEntity<?> getAttendancePercent(HttpServletRequest httpServletRequest,
                                                  @RequestParam(defaultValue = "0") Integer pageNo,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  @RequestParam(defaultValue = "1") Integer month,
                                                  @RequestParam(defaultValue = "2021") Integer year,
                                                  @RequestParam(defaultValue = "asc") String sortDir) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("id"));

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
        }

        return attendanceService.getStartupUsersAttendancePercent(currentUserObject, paging, month, year);
    }

    @GetMapping("startups/attendance/members/{memberId}/meetings/{date}")
    public ResponseEntity<?> getMemberMeetingsByDate(HttpServletRequest httpServletRequest,
                                                     @PathVariable Long memberId,
                                                     @PathVariable Long date,
                                                     @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return attendanceService.getMemberMeetingsByDate(currentUserObject, memberId, date);
    }

    @GetMapping("startups/attendance")
    public ResponseEntity<?> getStartUpOverallAttendanceByMonth(HttpServletRequest httpServletRequest,
                                                                @RequestParam(defaultValue = "1") Integer month,
                                                                @RequestParam(defaultValue = "2021") Integer year) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return attendanceService.getStartupOverallAttendanceByMonth(currentUserObject, month, year);
    }

    @GetMapping("startups/attendance/members/export")
    public ResponseEntity<?> excelExport(HttpServletRequest httpServletRequest,
                                         @RequestParam(defaultValue = "1") Integer month,
                                         @RequestParam(defaultValue = "2021") Integer year,
                                         @RequestParam(defaultValue = "0") Long memberId,
                                         @RequestParam(defaultValue = "xlsx") String fileFormat,
                                         @RequestParam(defaultValue = "UTC") String timeZone) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return attendanceService.excelExport(currentUserObject, month, year, memberId, fileFormat, timeZone);
    }

    @GetMapping("management/attendance/members/export")
    public ResponseEntity<?> excelExportStartupWise(HttpServletRequest httpServletRequest,
                                                    @RequestParam(defaultValue = "1") Integer month,
                                                    @RequestParam(defaultValue = "2021") Integer year,
                                                    @RequestParam(defaultValue = "xlsx") String fileFormat,
                                                    @RequestParam(defaultValue = "UTC") String timeZone) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return attendanceService.excelExportManagement(currentUserObject, month, year, fileFormat, timeZone);
    }

    @Authorize
    @GetMapping("management/attendance/intakePrograms")
    public ResponseEntity<?> getProgramList() {
        return ResponseWrapper.response(ConstantUtility.getacademyProgramList());
    }

    @Authorize
    @GetMapping("management/attendance/intakePrograms/{intakeProgramName}")
    public ResponseEntity<?> getIntakeList(HttpServletRequest httpServletRequest, @PathVariable String intakeProgramName) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        List<Object> list = academyRoomService.dropdownManagementAcademyRoomIntakes(currentUserObject,
            intakeProgramName);
        return ResponseWrapper.response(list);
    }

    @Authorize
    @GetMapping("management/attendance/intakePrograms/{intakeProgramId}/academyRooms")
    public ResponseEntity<?> getAcademyList(HttpServletRequest httpServletRequest,
                                            @PathVariable Long intakeProgramId,
                                            @RequestParam(defaultValue = "0") Integer pageNo,
                                            @RequestParam(defaultValue = "500") Integer pageSize) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("createdOn").ascending());

        return attendanceService.getAcademyList(currentUserObject, intakeProgramId, paging);
    }

    @GetMapping("management/attendance/intakePrograms/{intakeProgramId}/academyRooms/{academyRoomId}/workshopSessions")
    public ResponseEntity<?> getWorkshopSessionsByAcademyRoomsAndIntakes(HttpServletRequest httpServletRequest,
                                                                         @PathVariable Long intakeProgramId,
                                                                         @PathVariable Long academyRoomId,
                                                                         @RequestParam(defaultValue = "0") Long startDate,
                                                                         @RequestParam(defaultValue = "0") Integer pageNo,
                                                                         @RequestParam(defaultValue = "500") Integer pageSize) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize);

        return attendanceService.getWorkshopSessionsByAcademyRoomsAndIntakes(currentUserObject, intakeProgramId, academyRoomId, startDate, paging);
    }


    @GetMapping("management/attendance/intakePrograms/{intakeProgramId}/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/startups")
    public ResponseEntity<?> getStartups(HttpServletRequest httpServletRequest,
                                         @PathVariable Long intakeProgramId,
                                         @PathVariable Long academyRoomId,
                                         @PathVariable Long workshopSessionId,
                                         @RequestParam(defaultValue = "0") Long startDate,
                                         @RequestParam(defaultValue = "0") Integer pageNo,
                                         @RequestParam(defaultValue = "1000") Integer pageSize) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize);

        return attendanceService.getStartups(currentUserObject, intakeProgramId, academyRoomId, workshopSessionId, paging);
    }


    @GetMapping("management/attendance/intakePrograms/{intakeProgramId}/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/startups/{startupId}/members")
    public ResponseEntity<?> listMembers(HttpServletRequest httpServletRequest,
                                         @PathVariable Long intakeProgramId,
                                         @PathVariable Long academyRoomId,
                                         @PathVariable Long workshopSessionId,
                                         @PathVariable Long startupId,
                                         @RequestParam(defaultValue = "0") Long startDate,
                                         @RequestParam(defaultValue = "0") Integer pageNo,
                                         @RequestParam(defaultValue = "1000") Integer pageSize) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize);

        return attendanceService.listMembers(currentUserObject, intakeProgramId, academyRoomId, workshopSessionId,
            startupId, paging);
    }


    @PutMapping("management/attendance/mark")
    public ResponseEntity<?> markAttendance(HttpServletRequest httpServletRequest,
                                            @Valid @RequestBody MarkAttendanceDto markAttendanceDto) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return attendanceService.markAttendance(currentUserObject, markAttendanceDto);
    }
}
