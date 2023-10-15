package com.stc.inspireu.specifications;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.stc.inspireu.models.Assignment;
import com.stc.inspireu.models.Survey;
import com.stc.inspireu.models.TrainingMaterial;
import com.stc.inspireu.models.User;
import com.stc.inspireu.models.WorkshopSessionSubmissions;

public class WorkShopSpecification {

	public static Specification<TrainingMaterial> getWorkshopSessionsTrainings(User user, Long workshopSessionId,
			String filterBy, String searchBy, Long academyRoomId) {
		return (root, query, criteriaBuilder) -> {
			// AcademyRoomGetRequestDto progressReportRequest
			List<Predicate> predicates = new ArrayList<>();

			Predicate predicateForName = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
					"%" + searchBy.toLowerCase() + "%");
//            Predicate predicateForDescription = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
//                    "%" + searchBy.toLowerCase() + "%");
			Predicate predicateForUploadedBy = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("createdUser").get("alias")), "%" + searchBy.toLowerCase() + "%");

			predicates.add(criteriaBuilder.equal(root.get("workshopSession").get("id"), workshopSessionId));
			predicates.add(
					criteriaBuilder.equal(root.get("workshopSession").get("academyRoom").get("id"), academyRoomId));

			// Predicate predicateForCreator =
			// criteriaBuilder.equal(root.get("createdUser").get("id"), user.getId());

//            Predicate predicateForPermission = criteriaBuilder.isMember(user,
//                    root.get("workshopSession").get("sharedUser"));

//            Predicate predicateForWorkshopSessionCreator = criteriaBuilder
//                    .equal(root.get("workshopSession").get("createdUser").get("id"), user.getId());

//            Predicate predicateForCreatorOrSharedMember = criteriaBuilder.or(predicateForCreator,
//                    predicateForPermission, predicateForWorkshopSessionCreator);

//            predicates.add(predicateForCreatorOrSharedMember);

			if (searchBy != null && !searchBy.isEmpty()) {

				// name
				if (filterBy.equals("materialName")) {
					predicates.add(predicateForName);
				}
//                // description
//                if (filterBy.equals("description")) {
//                    predicates.add(predicateForDescription);
//                }
				// description
				if (filterBy.equals("uploadedBy")) {
					predicates.add(predicateForUploadedBy);
				}

				// all
				if (filterBy.equals("all")) {
					Predicate pred = criteriaBuilder.or(predicateForName, predicateForUploadedBy);
					predicates.add(pred);
				}
			}

			if (predicates == null || predicates.size() == 0) {
				return null;
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
		};
	}

	// submitted assignments

	public static Specification<Assignment> getWorkshopSessionsAssignmentsSubmitted(User user, Long assignmentId,
			Long workshopSessionId, String filterBy, String searchBy, Long academyRoomId) {
		return (root, query, criteriaBuilder) -> {
			// AcademyRoomGetRequestDto progressReportRequest
			List<Predicate> predicates = new ArrayList<>();

//            Predicate predicateForName = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
//                    "%" + searchBy.toLowerCase() + "%");
//            Predicate predicateForDescription = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
//                    "%" + searchBy.toLowerCase() + "%");
			Predicate predicateForStatus = criteriaBuilder.like(criteriaBuilder.lower(root.get("status")),
					"%" + searchBy.toLowerCase() + "%");
			Predicate predicateForStartupName = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("submittedStartup").get("startupName")),
					"%" + searchBy.toLowerCase() + "%");
			Predicate predicateForSubmittedBy = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("submittedUser").get("alias")), "%" + searchBy.toLowerCase() + "%");

			// predicates.add(criteriaBuilder.equal(root.get("workshopSession").get("id"),
			// workshopSessionId));
			// predicates.add(criteriaBuilder.equal(root.get("workshopSession").get("academyRoom").get("id"),
			// academyRoomId));
			predicates.add(criteriaBuilder.equal(root.get("refAssignment").get("id"), assignmentId));
			predicates.add(criteriaBuilder.equal(root.get("refAssignment").get("workshopSession").get("id"),
					workshopSessionId));
			predicates.add(criteriaBuilder.equal(
					root.get("refAssignment").get("workshopSession").get("academyRoom").get("id"), academyRoomId));

			// Predicate
			// predicateForCreator=criteriaBuilder.equal(root.get("createdUser").get("id"),
			// user.getId());

//            Predicate predicateForCreator = criteriaBuilder
//                    .equal(root.get("refAssignment").get("createdUser").get("id"), user.getId());
//
//            Predicate predicateForPermission = criteriaBuilder.isMember(user,
//                    root.get("refAssignment").get("workshopSession").get("sharedUser"));

//            Predicate predicateForWorkshopSessionCreator = criteriaBuilder
//                    .equal(root.get("refAssignment").get("workshopSession").get("createdUser").get("id"), user.getId());

//            Predicate predicateForCreatorOrSharedMember = criteriaBuilder.or(predicateForCreator,
//                    predicateForPermission, predicateForWorkshopSessionCreator);

			// predicates.add(predicateForCreatorOrSharedMember);

			if (searchBy != null && !searchBy.isEmpty()) {

//                // name
//                if (filterBy.equals("name")) {
//                    predicates.add(predicateForName);
//                }
//                // jsonForm
//                if (filterBy.equals("description")) {
//                    predicates.add(predicateForDescription);
//                }
				// status
				if (filterBy.equals("startupName")) {
					predicates.add(predicateForStartupName);
				}

				if (filterBy.equals("status")) {
					predicates.add(predicateForStatus);
				}

				if (filterBy.equals("submittedBy")) {
					predicates.add(predicateForSubmittedBy);
				}

				// all
				if (filterBy.equals("all")) {
					// Predicate pred = criteriaBuilder.or(predicateForName,
					// predicateForDescription, predicateForStatus);
					Predicate pred = criteriaBuilder.or(predicateForStartupName, predicateForStatus,
							predicateForSubmittedBy);
					predicates.add(pred);
				}
			}
			query.distinct(true);
			// query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
			if (predicates == null || predicates.size() == 0) {
				return null;
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
		};
	}

	// submitted surveys

	public static Specification<Survey> getWorkshopSessionsSurveysSubmitted(User user, Long surveyId,
			Long workshopSessionId, String filterBy, String searchBy, Long academyRoomId) {
		return (root, query, criteriaBuilder) -> {

			List<Predicate> predicates = new ArrayList<>();

//            Predicate predicateForName = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
//                    "%" + searchBy.toLowerCase() + "%");
//			Predicate predicateForDescription = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
//					"%" + searchBy.toLowerCase() + "%");
			Predicate predicateForStatus = criteriaBuilder.like(criteriaBuilder.lower(root.get("status")),
					"%" + searchBy.toLowerCase() + "%");
			Predicate predicateForStartupName = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("submittedStartup").get("startupName")),
					"%" + searchBy.toLowerCase() + "%");
			Predicate predicateForSubmittedBy = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("submittedUser").get("alias")), "%" + searchBy.toLowerCase() + "%");

			predicates.add(criteriaBuilder.equal(root.get("refSurvey").get("id"), surveyId));
			predicates.add(
					criteriaBuilder.equal(root.get("refSurvey").get("workshopSession").get("id"), workshopSessionId));
			predicates.add(criteriaBuilder
					.equal(root.get("refSurvey").get("workshopSession").get("academyRoom").get("id"), academyRoomId));

//            Predicate predicateForCreator = criteriaBuilder.equal(root.get("refSurvey").get("createdUser").get("id"),
//                    user.getId());

//            Predicate predicateForPermission = criteriaBuilder.isMember(user,
//                    root.get("refSurvey").get("workshopSession").get("sharedUser"));

//            Predicate predicateForWorkshopSessionCreator = criteriaBuilder
//                    .equal(root.get("refSurvey").get("workshopSession").get("createdUser").get("id"), user.getId());
//
//            Predicate predicateForCreatorOrSharedMember = criteriaBuilder.or(predicateForCreator,
//                    predicateForPermission, predicateForWorkshopSessionCreator);
//
//            predicates.add(predicateForCreatorOrSharedMember);

			if (searchBy != null && !searchBy.isEmpty()) {

//                // name
//                if (filterBy.equals("name")) {
//                    predicates.add(predicateForName);
//                }
				// jsonForm
//				if (filterBy.equals("description")) {
//					predicates.add(predicateForDescription);
//				}
				// status
				if (filterBy.equals("status")) {
					predicates.add(predicateForStatus);
				}
				if (filterBy.equals("startupName")) {
					predicates.add(predicateForStartupName);
				}

				if (filterBy.equals("submittedBy")) {
					predicates.add(predicateForSubmittedBy);
				}
				// all
				if (filterBy.equals("all")) {
					Predicate pred = criteriaBuilder.or(predicateForStartupName, predicateForStatus,
							predicateForSubmittedBy);
					predicates.add(pred);
				}
			}
			query.distinct(true);
			if (predicates == null || predicates.size() == 0) {
				return null;
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
		};
	}

	// submitted logs

	public static Specification<WorkshopSessionSubmissions> getWorkshopSessionsSubmisionLogs(User user,
			Long workshopSessionId, String filterBy, String searchBy, Long academyRoomId) {
		return (root, query, criteriaBuilder) -> {

			List<Predicate> predicates = new ArrayList<>();

			Predicate predicateForStatus = criteriaBuilder.like(criteriaBuilder.lower(root.get("status")),
					"%" + searchBy.toLowerCase() + "%");

			Predicate predicateForStartupName = criteriaBuilder
					.like(criteriaBuilder.lower(root.get("startup").get("startupName")), "%" + searchBy + "%");

			Predicate predicateForSubmittedBy = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("submittedUser").get("alias")), "%" + searchBy.toLowerCase() + "%");

			Predicate predicateForFileName = criteriaBuilder.like(criteriaBuilder.lower(root.get("submittedFileName")),
					"%" + searchBy.toLowerCase() + "%");

			predicates.add(criteriaBuilder.equal(root.get("workshopSession").get("refWorkshopSession").get("id"),
					workshopSessionId));

			predicates.add(criteriaBuilder.equal(
					root.get("workshopSession").get("refWorkshopSession").get("academyRoom").get("id"), academyRoomId));

			if (searchBy != null && !searchBy.isEmpty()) {

				// submittedFileName
				if (filterBy.equals("submittedFileName")) {
					predicates.add(predicateForFileName);
				}
				// status
				if (filterBy.equals("status")) {
					predicates.add(predicateForStatus);
				}
				if (filterBy.equals("startupName")) {
					predicates.add(predicateForStartupName);
				}

				if (filterBy.equals("submittedBy")) {
					predicates.add(predicateForSubmittedBy);
				}
				// all
				if (filterBy.equals("all")) {
					Predicate pred = criteriaBuilder.or(predicateForFileName, predicateForStartupName,
							predicateForStatus, predicateForSubmittedBy);
					predicates.add(pred);
				}
			}
			query.distinct(true);
			if (predicates == null || predicates.size() == 0) {
				return null;
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
		};
	}

}
