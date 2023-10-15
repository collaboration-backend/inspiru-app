package com.stc.inspireu.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.dtos.AttendanceStartupDto;
import com.stc.inspireu.models.User;

@Transactional
public interface AttendanceRepositoryCustom {

	List<AttendanceStartupDto> getStartupWiseAttendances(User user, Long academyRoomId, Date searchDate,
			Pageable paging, String filterBy, String searchBy);

}
