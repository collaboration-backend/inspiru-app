package com.stc.inspireu.specifications;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.stc.inspireu.models.Attendance;

public class AttendanceSpecification {

	public static Specification<Attendance> getAttendancesForManagement(Long academyRoomId, Long oneToOneMeetingId,
			Long trainingSessionId, Long startupId, String searchBy) {
		return (root, query, criteriaBuilder) -> {

			List<Predicate> predicates = new ArrayList<>();

			Predicate predicateForAcademyRoom = criteriaBuilder.equal(
					root.get("trainingSession").get("workshopSession").get("academyRoom").get("id"), academyRoomId);

			Predicate predicateForOnetoOneMeeting = criteriaBuilder.equal(root.get("oneToOneMeeting").get("id"),
					oneToOneMeetingId);

			Predicate predicateForTrainingSession = criteriaBuilder.equal(root.get("trainingSession").get("id"),
					trainingSessionId);

			Predicate predicateForStartup = criteriaBuilder.equal(root.get("oneToOneMeeting").get("startup").get("id"),
					startupId);

			Predicate predicateForPresent = criteriaBuilder.isTrue(root.get("isPresent"));

			Predicate predicateForAbsent = criteriaBuilder.isFalse(root.get("isPresent"));

			Predicate predicateForLate = criteriaBuilder.isTrue(root.get("isLate"));

			Predicate predicateForMemberName = criteriaBuilder
					.like(criteriaBuilder.lower(root.get("member").get("alias")), "%" + searchBy.toLowerCase() + "%");

			Predicate predicateForStartupName = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("oneToOneMeeting").get("startup").get("startupName")),
					"%" + searchBy.toLowerCase() + "%");

			Predicate predicateForSessionName = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("oneToOneMeeting").get("meetingName")),
					"%" + searchBy.toLowerCase() + "%");

			Predicate predicateForAttendance = null;

			if (searchBy != null && !searchBy.isEmpty() && !searchBy.equals("all")) {

				if ("present".contains(searchBy.toLowerCase())) {
					predicateForAttendance = predicateForPresent;
				}

				if ("late".contains(searchBy.toLowerCase())) {
					predicateForAttendance = predicateForLate;
				}

				if ("absent".contains(searchBy.toLowerCase())) {
					predicateForAttendance = predicateForAbsent;
				}

				// all fields
				Predicate pred = criteriaBuilder.and(predicateForMemberName, predicateForStartupName,
						predicateForSessionName);

				if (predicateForAttendance != null) {
					pred = criteriaBuilder.and(predicateForMemberName, predicateForStartupName, predicateForSessionName,
							predicateForAttendance);
				}
				predicates.add(pred);

			}

			if (academyRoomId != null && academyRoomId != 0) {
				predicates.add(predicateForAcademyRoom);
			}

			if (oneToOneMeetingId != null && oneToOneMeetingId != 0) {
				predicates.add(predicateForOnetoOneMeeting);
			}

			if (trainingSessionId != null && trainingSessionId != 0) {
				predicates.add(predicateForTrainingSession);
			}

			if (startupId != null && startupId != 0) {
				predicates.add(predicateForStartup);
			}

			query.distinct(true);
			if (predicates == null || predicates.size() == 0) {
				return null;
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
		};
	}

}
