package com.stc.inspireu.specifications;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.stc.inspireu.models.DueDiligenceTemplate2021;
import com.stc.inspireu.models.User;
import com.stc.inspireu.enums.Constant;

public class DueDiligenceTemplate2021Specification {

	// submitted due dilgilence

	public static Specification<DueDiligenceTemplate2021> getDueDiligenceSubmisions(User user, Long dueDiligenceId,
			String filterBy, String searchBy) {
		return (root, query, criteriaBuilder) -> {

			List<Predicate> predicates = new ArrayList<>();

			Predicate predicateForStatus = criteriaBuilder.like(criteriaBuilder.lower(root.get("status")),
					"%" + searchBy.toLowerCase() + "%");

			Predicate predicateForStartupName = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("startup").get("startupName")), "%" + searchBy.toLowerCase() + "%");

			Predicate predicateForCreatedBy = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("createdUser").get("alias")), "%" + searchBy.toLowerCase() + "%");

			Predicate predicateForSubmittedBy = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("submittedUser").get("alias")), "%" + searchBy.toLowerCase() + "%");

			Predicate predicateForNullStartup = criteriaBuilder.isNull(root.get("startup"));
			Predicate predicateForNotNullStartup = criteriaBuilder.not(predicateForNullStartup);

			predicates.add(predicateForNotNullStartup);
			predicates.add(criteriaBuilder.equal(root.get("refDueDiligenceTemplate2021").get("id"), dueDiligenceId));
			Predicate predicateForSubmitStatus = criteriaBuilder.or(
					criteriaBuilder.equal(root.get("status"), Constant.SUBMITTED.toString()),
					criteriaBuilder.equal(root.get("status"), Constant.APPROVED.toString()),
					criteriaBuilder.equal(root.get("status"), Constant.RESUBMIT.toString()));
			predicates.add(predicateForSubmitStatus);
			if (searchBy != null && !searchBy.isEmpty() && !searchBy.equals("all")) {

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

				if (filterBy.equals("createdBy")) {
					predicates.add(predicateForCreatedBy);
				}

				// all
				if (filterBy.equals("all")) {
					Predicate pred = criteriaBuilder.or(predicateForStartupName, predicateForStatus,
							predicateForSubmittedBy, predicateForCreatedBy);
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
