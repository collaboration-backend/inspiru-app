package com.stc.inspireu.repositories.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.stc.inspireu.repositories.AttendanceRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.dtos.AttendanceStartupDto;
import com.stc.inspireu.models.Attendance;
import com.stc.inspireu.models.User;

@Repository
@Transactional(readOnly = true)
public class AttendanceRepositoryCustomImpl implements AttendanceRepositoryCustom {

	@PersistenceContext
	EntityManager entityManager;

	@Override
	public List<AttendanceStartupDto> getStartupWiseAttendances(User user, Long academyRoomId, Date searchDate,
			Pageable paging, String filterBy, String searchBy) {

		// System.out.println(searchDate);

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<AttendanceStartupDto> cq = builder.createQuery(AttendanceStartupDto.class); // wrapper class
		Root<Attendance> attendance = cq.from(Attendance.class); // root entity
		List<Predicate> predicates = new ArrayList<>();

//		//Subquery
//		Subquery<Long> markedStartupMemberCount = cq.subquery(Long.class);
//		Root<Attendance> markedStartupMember = markedStartupMemberCount.from(Attendance.class);
//		markedStartupMemberCount.select(builder.count(markedStartupMember));
//		markedStartupMemberCount.where(
//		        builder.equal(markedStartupMember.get("attendanceDate"), attendance.get("attendanceDate")),
//		        builder.equal(markedStartupMember.get("trainingSession"),attendance.get("trainingSession")),
//		        builder.equal(markedStartupMember.get("slot"),attendance.get("slot")),
//		        builder.equal(markedStartupMember.get("oneToOneMeeting"),attendance.get("oneToOneMeeting"))
//		);

		Predicate predicateForOneToOneMeetingIsNotNull = builder.isNotNull(attendance.get("oneToOneMeeting").get("id"));

		Predicate predicateTrainingSessionIsNotNull = builder.isNotNull(attendance.get("trainingSession").get("id"));
		Predicate predicateAcademeyRoom = builder.equal(
				attendance.get("trainingSession").get("workshopSession").get("academyRoom").get("id"), academyRoomId);
		Predicate predicateForEvent = builder.or(predicateForOneToOneMeetingIsNotNull,
				predicateTrainingSessionIsNotNull);

		Predicate predicateAttendanceDate = builder.equal(attendance.get("attendanceDate"), searchDate);

		predicates.add(predicateAttendanceDate);

		if (academyRoomId > 0) {
			predicates.add(predicateAcademeyRoom);
		}

		if (filterBy.equals("oneToOneMeeting")) {
			predicateForEvent = predicateForOneToOneMeetingIsNotNull;
		}

		if (filterBy.equals("trainingSession")) {
			predicateForEvent = predicateTrainingSessionIsNotNull;
		}
		predicates.add(predicateForEvent);

		cq.select(builder.construct(AttendanceStartupDto.class, attendance.get("attendanceDate"),
				builder.selectCase().when(attendance.get("oneToOneMeeting").isNull(), (long) 0)
						.otherwise(attendance.get("oneToOneMeeting").get("id")),
				builder.selectCase().when(attendance.get("trainingSession").isNull(), (long) 0)
						.otherwise(attendance.get("trainingSession").get("id"))
		// markedStartupMemberCount.getSelection()

		));
		cq.where(predicates.toArray(new Predicate[predicates.size()])).groupBy(attendance.get("attendanceDate"),
				attendance.get("oneToOneMeeting"), attendance.get("trainingSession"));

		return entityManager.createQuery(cq).setMaxResults(paging.getPageSize()).setFirstResult(paging.getPageNumber())
				.getResultList();
	}

}
