package com.stc.inspireu.repositories.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.stc.inspireu.repositories.FeedbackRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.dtos.FeedbackFormManagementDto;
import com.stc.inspireu.dtos.StartupFeedbackFormsDto;
import com.stc.inspireu.models.Feedback;
import com.stc.inspireu.models.Startup;
import com.stc.inspireu.models.User;
import com.stc.inspireu.enums.Constant;

@Repository
@Transactional(readOnly = true)
public class FeedbackRepositoryCustomImpl implements FeedbackRepositoryCustom {

	@PersistenceContext
	EntityManager entityManager;

	@Override
	public List<StartupFeedbackFormsDto> getStartupFeedbacks(Long refFeedbackId, Long workshopSessionId,
			Long academyRoomId, Pageable paging, String filterBy, String searchBy) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<StartupFeedbackFormsDto> cq = cb.createQuery(StartupFeedbackFormsDto.class);

		Root<Startup> stRoot = cq.from(Startup.class);

		// Subquery1
		Subquery<Feedback> startUpFeedback = cq.subquery(Feedback.class);
		Root<Feedback> submittedFeedback = startUpFeedback.from(Feedback.class);
		startUpFeedback.select(submittedFeedback.get("id"));
		startUpFeedback.where(cb.equal(submittedFeedback.get("refFeedback").get("id"), refFeedbackId),
				// cb.equal(submittedFeedback.get("status"), Constant.SUBMITTED.toString()),
				cb.equal(submittedFeedback.get("refFeedback").get("workshopSession").get("id"), workshopSessionId),
				cb.equal(submittedFeedback.get("refFeedback").get("workshopSession").get("academyRoom").get("id"),
						academyRoomId),
				cb.equal(submittedFeedback.get("forStartup").get("id"), stRoot.get("id")) // ,
		// cb.equal(submittedFeedback.get("workshopSession").get("id"),
		// wsRoot.get("refWorkshopSession").get("id"))
		);

		cq.select(cb.construct(StartupFeedbackFormsDto.class, stRoot.get("id"), stRoot.get("startupName"),
				startUpFeedback.getSelection()));

		return entityManager.createQuery(cq).setMaxResults(paging.getPageSize()).setFirstResult(paging.getPageNumber())
				.getResultList();
	}

	@Override
	public List<FeedbackFormManagementDto> getFeedbacks(User user, Long academyRoomId, Long workshopSessionId,
			Pageable paging, String filterBy, String searchBy) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FeedbackFormManagementDto> cq = builder.createQuery(FeedbackFormManagementDto.class); // wrapper
																											// class
		Root<Feedback> feedback = cq.from(Feedback.class); // root entity

		// Subquery
		Subquery<Long> submittedFeedbackCount = cq.subquery(Long.class);
		Root<Feedback> submittedFeedback = submittedFeedbackCount.from(Feedback.class);
		submittedFeedbackCount.select(builder.count(submittedFeedback));
		submittedFeedbackCount.where(builder.equal(submittedFeedback.get("refFeedback").get("id"), feedback.get("id")),
				builder.equal(submittedFeedback.get("status"), Constant.SUBMITTED.toString()));

		// Predicate predicateForCreator =
		// builder.equal(feedback.get("createdUser").get("id"), user.getId());

//        Predicate predicateForPermission = builder.isMember(user, feedback.get("workshopSession").get("sharedUser"));

//        Predicate predicateForWorkshopSessionCreator = builder
//                .equal(feedback.get("workshopSession").get("createdUser").get("id"), user.getId());

//        Predicate predicateForCreatorOrSharedMember = builder.or(predicateForCreator, predicateForWorkshopSessionCreator);

		cq.select(builder.construct(FeedbackFormManagementDto.class, feedback.get("id"), feedback.get("name"),
				feedback.get("status"), feedback.get("createdUser").get("alias"), feedback.get("createdOn"),
				feedback.get("jsonForm"), submittedFeedbackCount.getSelection()

		));
		cq.where(builder.isNull(feedback.get("forStartup")),
				builder.equal(feedback.get("workshopSession").get("id"), workshopSessionId),
				builder.equal(feedback.get("workshopSession").get("academyRoom").get("id"), academyRoomId)
		// ,predicateForCreatorOrSharedMember
		);

		return entityManager.createQuery(cq).setMaxResults(paging.getPageSize()).setFirstResult(paging.getPageNumber())
				.getResultList();
	}

}
