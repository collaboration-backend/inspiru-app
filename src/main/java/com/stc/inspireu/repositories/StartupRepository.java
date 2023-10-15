package com.stc.inspireu.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectId;
import com.stc.inspireu.jpa.projections.ProjectStartupIdAndname;
import com.stc.inspireu.models.Startup;

@Transactional
public interface StartupRepository
		extends PagingAndSortingRepository<Startup, Long>, JpaSpecificationExecutor<Startup> {

	Page<Startup> findByIntakeProgram_Id(Long intakeProgramId, Pageable paging);

	List<Startup> findByIntakeProgram_IdAndIsRealTrue(Long id);

	List<Startup> findByStartupNameContainingIgnoreCaseAndIntakeProgram_IdAndIsRealTrue(String name, Long id);

	Page<Startup> findByIsRealTrue(Pageable paging);

	Page<Startup> findByIntakeProgram_IdAndIsRealTrue(Long intakeProgramId, Pageable paging);

	@Query("SELECT o FROM Startup o where o.intakeProgram.id = :intakeProgramId and o.isReal = true")
	Page<ProjectStartupIdAndname> getRealStartupByIntake(Long intakeProgramId, Pageable pageable);

	Page<ProjectStartupIdAndname> findByStartupNameContainingIgnoreCaseAndIntakeProgram_IdAndIsRealTrue(String name,
			Long id, Pageable pageable);

	@Query("SELECT COUNT(o) FROM Startup o WHERE o.intakeProgram.id = :id and o.isReal = true")
	long getCountRealStartupByIntake(Long id);

	default Page<Startup> getSummaryStartups(Long intakeId, String filterBy, String filterKeyword, Pageable paging) {
		return (Page<Startup>) findAll(StartupSpecs.getSummaryStartups(intakeId, filterBy, filterKeyword), paging);
	}

	@Query("SELECT o.id FROM Startup o where o.intakeProgram.id = :intakeProgramId and o.isReal = true")
	Set<ProjectId> getByIntakeProgramAndIsReal(Long intakeProgramId);

}

class StartupSpecs {

	public static Specification<Startup> getSummaryStartups(Long intakeProgramId, String filterBy,
			String filterKeyword) {

		return new Specification<Startup>() {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<Startup> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

				List<Predicate> predicates = new ArrayList<Predicate>();

				Predicate p1 = criteriaBuilder.equal(root.get("intakeProgram").get("id"), intakeProgramId);

				if (!StringUtils.isEmpty(filterBy) && !StringUtils.isEmpty(filterKeyword)) {
					Predicate p4 = criteriaBuilder.like(criteriaBuilder.lower(root.get(filterBy)),
							"%" + filterKeyword.toLowerCase() + "%");

					Predicate p5 = criteriaBuilder.and(p1, p4);

					predicates.add(p5);
				} else {
					predicates.add(p1);
				}

				Predicate[] predicatesArray = new Predicate[predicates.size()];

				return criteriaBuilder.and(predicates.toArray(predicatesArray));
			}

		};

	}

}
