package com.stc.inspireu.services;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.AttendanceStartupDto;
import com.stc.inspireu.dtos.MarkAttendanceDto;
import com.stc.inspireu.dtos.PostAttendanceDto;
import com.stc.inspireu.dtos.PostAttendancePercentageDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AttendanceService {

	ResponseEntity<?> createStartupMeetingAttendance(CurrentUserObject currentUserObject,
			List<PostAttendanceDto> postAttendanceDto, Long meetingId);

	ResponseEntity<?> saveAttendancePercentage(CurrentUserObject currentUserObject,
			PostAttendancePercentageDto postAttendanceDto);

	List<AttendanceStartupDto> getStartupAttendancesByManagement(CurrentUserObject currentUserObject,
			Long attendanceDate, Pageable paging, String searchBy, String filterBy, Long academyRoomId);

	ResponseEntity<?> getStartupUsersAttendancePercent(CurrentUserObject currentUserObject, Pageable paging,
			Integer month, Integer year);

	ResponseEntity<?> getStartupUsersAttendance(CurrentUserObject currentUserObject, Long memberId, Integer month,
			Integer year);

	ResponseEntity<?> getMemberMeetingsByDate(CurrentUserObject currentUserObject, Long memberId, Long date);

	ResponseEntity<?> getStartupOverallAttendanceByMonth(CurrentUserObject currentUserObject, Integer month,
			Integer year);

	Object exportStartUpOverallAttendanceByMonth(CurrentUserObject currentUserObject, Integer month, Integer year);

	ResponseEntity<?> excelExport(CurrentUserObject currentUserObject, Integer month, Integer year, Long memberId,
			String fileFormat, String timeZone);

	ResponseEntity<?> excelExportManagement(CurrentUserObject currentUserObject, Integer month, Integer year,
			String fileFormat, String timeZone);

	ResponseEntity<?> getMembers(CurrentUserObject currentUserObject, Long startupId);


	ResponseEntity<?> getWorkshopSessionsByAcademyRoomsAndIntakes(CurrentUserObject currentUserObject,
			Long intakeProgramId, Long academyRoomId, Long startDate, Pageable paging);

	ResponseEntity<?> getAcademyList(CurrentUserObject currentUserObject, Long intakeProgramId, Pageable paging);

	ResponseEntity<?> getStartups(CurrentUserObject currentUserObject, Long intakeProgramId, Long academyRoomId,
			Long workshopSessionId, Pageable paging);

	ResponseEntity<?> listMembers(CurrentUserObject currentUserObject, Long intakeProgramId, Long academyRoomId,
			Long workshopSessionId, Long startupId, Pageable paging);

	ResponseEntity<?> markAttendance(CurrentUserObject currentUserObject,
			MarkAttendanceDto markAttendanceDto);
}
