package com.stc.inspireu.repositories.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.stc.inspireu.repositories.SurveyRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.dtos.SurveyManagementDto;
import com.stc.inspireu.models.Survey;
import com.stc.inspireu.models.User;
import com.stc.inspireu.enums.Constant;

@Repository
@Transactional(readOnly = true)
public class SurveyRepositoryCustomImpl implements SurveyRepositoryCustom {

	@PersistenceContext
	EntityManager entityManager;

	@Override
	public List<SurveyManagementDto> getSurveys(User user, Long academyRoomId, Long workshopSessionId, Pageable paging,
			String filterBy, String searchBy) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<SurveyManagementDto> cq = builder.createQuery(SurveyManagementDto.class); // wrapper class
		Root<Survey> survey = cq.from(Survey.class); // root entity

		// Subquery
		Subquery<Long> submittedSurveyCount = cq.subquery(Long.class);
		Root<Survey> submittedSurvey = submittedSurveyCount.from(Survey.class);
		submittedSurveyCount.select(builder.count(submittedSurvey));
		submittedSurveyCount.where(builder.equal(submittedSurvey.get("refSurvey").get("id"), survey.get("id")),
				builder.equal(submittedSurvey.get("status"), Constant.SUBMITTED.toString()));

//        Predicate predicateForCreator = builder.equal(survey.get("createdUser").get("id"), user.getId());

//        Predicate predicateForPermission = builder.isMember(user, survey.get("workshopSession").get("sharedUser"));

//        Predicate predicateForWorkshopSessionCreator = builder
//                .equal(survey.get("workshopSession").get("createdUser").get("id"), user.getId());

//        Predicate predicateForCreatorOrSharedMember = builder.or(predicateForCreator, predicateForWorkshopSessionCreator);

		cq.select(
				builder.construct(SurveyManagementDto.class, survey.get("id"), survey.get("name"), survey.get("status"),
						// survey.get("dueDate"),
						survey.get("createdUser").get("alias"), survey.get("createdOn"), survey.get("jsonForm"),
						submittedSurveyCount.getSelection()

				));
		cq.where(builder.isNull(survey.get("submittedStartup")),
				builder.equal(survey.get("workshopSession").get("id"), workshopSessionId),
				builder.equal(survey.get("workshopSession").get("academyRoom").get("id"), academyRoomId)
		// ,predicateForCreatorOrSharedMember
		);

		return entityManager.createQuery(cq).setMaxResults(paging.getPageSize()).setFirstResult(paging.getPageNumber())
				.getResultList();
	}

}
