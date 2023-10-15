package com.stc.inspireu.repositories;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.User;
import com.stc.inspireu.enums.Constant;

public interface UserRepositoryCustom {
	Page<User> searchUsers(String filterBy, String filterKeyword, Pageable pageable);
}

@Repository
@Transactional(readOnly = true)
class UserRepositoryCustomImpl implements UserRepositoryCustom {

	@PersistenceContext
	EntityManager entityManager;

	@Override
	public Page<User> searchUsers(String filterBy, String filterKeyword, Pageable pageable) {

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);

		Root<User> root = criteriaQuery.from(User.class);

		List<Predicate> predicates = new ArrayList<Predicate>();

		predicates.add(criteriaBuilder.notEqual(root.<String>get("invitationStatus"),
				Constant.STARTUP_DUEDILIGENCE_INVITATION.toString()));

		if (!filterBy.equals("") && !filterKeyword.equals("")) {
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.<String>get(filterBy)),
					"%" + filterKeyword.toLowerCase() + "%"));
		}

		Predicate[] predArray = new Predicate[predicates.size()];

		predicates.toArray(predArray);

		criteriaQuery.where(predArray);

		criteriaQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), root, criteriaBuilder));

		TypedQuery<User> typedQuery = entityManager.createQuery(criteriaQuery);
		int totalRows = typedQuery.getResultList().size();

		typedQuery.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
		typedQuery.setMaxResults(pageable.getPageSize());
		Page<User> result = new PageImpl<User>(typedQuery.getResultList(), pageable, totalRows);

		return result;

	}

}
