package com.stc.inspireu.repositories.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.stc.inspireu.repositories.AssignmentRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.dtos.AssignmentManagementDto;
import com.stc.inspireu.models.Assignment;
import com.stc.inspireu.models.User;
import com.stc.inspireu.enums.Constant;

@Repository
@Transactional(readOnly = true)
public class AssignmentRepositoryCustomImpl implements AssignmentRepositoryCustom {

	@PersistenceContext
	EntityManager entityManager;

	@Override
	public List<AssignmentManagementDto> getAssignments(User user, Long academyRoomId, Long workshopSessionId,
			Pageable paging, String filterBy, String searchBy) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<AssignmentManagementDto> cq = builder.createQuery(AssignmentManagementDto.class); // wrapper class
		Root<Assignment> assignment = cq.from(Assignment.class); // root entity

		// Subquery
		Subquery<Long> submittedAssignmentCount = cq.subquery(Long.class);
		Root<Assignment> submittedAssignment = submittedAssignmentCount.from(Assignment.class);
		submittedAssignmentCount.select(builder.count(submittedAssignment));
		submittedAssignmentCount.where(
				builder.equal(submittedAssignment.get("refAssignment").get("id"), assignment.get("id")),
				builder.equal(submittedAssignment.get("status"), Constant.SUBMITTED.toString()));

		// Predicate predicateForCreator =
		// builder.equal(assignment.get("createdUser").get("id"), user.getId());

		// Predicate predicateForPermission = builder.isMember(user,
		// assignment.get("workshopSession").get("sharedUser"));

//        Predicate predicateForWorkshopSessionCreator = builder
//                .equal(assignment.get("workshopSession").get("createdUser").get("id"), user.getId());
//
//        Predicate predicateForCreatorOrSharedMember = builder.or(predicateForCreator, predicateForPermission,
//                predicateForWorkshopSessionCreator);

		cq.select(builder.construct(AssignmentManagementDto.class, assignment.get("id"), assignment.get("name"),
				assignment.get("status"), assignment.get("dueDate"), assignment.get("createdUser").get("alias"),
//		    		builder.selectCase(assignment.get("createdUser")).when(assignment.get("createdUser").isNull(), "null").otherwise(assignment.get("createdUser").get("alias")),
				// builder.literal(assignment.get("createdUser").get("alias")),
				// builder.literal(builder.coalesce(assignment.get("createdUser").get("alias"),"na")),
				// builder.function("IFNULL", String.class, builder.literal( 2
				// ),builder.literal( 4 )),
				// assignment.get("createdUser").get("alias"),
				assignment.get("createdOn"), submittedAssignmentCount.getSelection()

		));
		cq.where(builder.isNull(assignment.get("submittedStartup")),
				builder.equal(assignment.get("workshopSession").get("id"), workshopSessionId),
				builder.equal(assignment.get("workshopSession").get("academyRoom").get("id"), academyRoomId)
		// ,
//                predicateForCreatorOrSharedMember
		);

		return entityManager.createQuery(cq).setMaxResults(paging.getPageSize()).setFirstResult(paging.getPageNumber())
				.getResultList();
	}

}
