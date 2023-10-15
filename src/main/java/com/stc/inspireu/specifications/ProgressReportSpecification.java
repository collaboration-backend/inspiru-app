package com.stc.inspireu.specifications;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.stc.inspireu.dtos.ProgressReportGetRequestDto;
import com.stc.inspireu.models.ProgressReport;

public class ProgressReportSpecification {

	public static Specification<ProgressReport> getProgressReports(Long userId, Long startUpId,
			ProgressReportGetRequestDto progressReportRequest) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (userId != null && progressReportRequest.getIsUserGenerated() == true) {
				predicates.add(criteriaBuilder.equal(root.join("submittedUser").get("id"), userId));
			}
			if (startUpId != null) {
				predicates.add(criteriaBuilder.equal(root.join("startup").get("id"), startUpId));
			}
			if (progressReportRequest.getReportName() != null && !progressReportRequest.getReportName().isEmpty()) {
				predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("reportName")),
						"%" + progressReportRequest.getReportName().toLowerCase() + "%"));
			}
			// query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
			if (predicates == null || predicates.size() == 0) {
				return null;
			}
			// if (predicates.isEmpty()) {
//				return criteriaBuilder.conjunction();
//			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
		};
	}

}
