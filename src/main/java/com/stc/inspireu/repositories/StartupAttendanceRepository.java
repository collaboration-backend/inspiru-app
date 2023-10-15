package com.stc.inspireu.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.stc.inspireu.models.StartupAttendance;

public interface StartupAttendanceRepository extends PagingAndSortingRepository<StartupAttendance, Long> {

	StartupAttendance findByEventTypeAndEventTypeIdAndStartupId(String eventType, Long eventId,
			Long startupId);

}
