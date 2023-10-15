package com.stc.inspireu.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.Attendance;

@Transactional
public interface AttendanceRepository
		extends PagingAndSortingRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

	List<Attendance> findByOneToOneMeeting_IdAndAttendanceDate(Long oneToOneMeetingId, Date attendanceDate);

	List<Attendance> findByOneToOneMeeting_Id(Long oneToOneMeetingId);

	Page<Attendance> findAll(Specification<Attendance> spec, Pageable pageable);

	Integer countByMember_IdAndAttendanceDateBetween(Long memberId, Date start, Date end);

	Integer countByMember_IdAndIsPresentAndAttendanceDateBetween(Long memberId, Boolean isPresent, Date start,
			Date end);

	List<Attendance> findByMember_IdAndAttendanceDateBetween(Long memberId, Date start, Date end);

	List<Attendance> findByMember_IdAndAttendanceDate(Long memberId, Date searchDate);

	Attendance findByMember_IdAndOneToOneMeeting_Id(Long memberId, Long oneToOneMeetingId);

	Integer countByMember_Startup_IdAndAttendanceDateBetween(Long memberStartupId, Date start, Date end);

	Integer countByMember_Startup_IdAndIsPresentAndAttendanceDateBetween(Long memberStartupId, Boolean isPresent,
			Date start, Date end);

	List<Attendance> findByAttendanceDateBetween(Date start, Date end);
}
