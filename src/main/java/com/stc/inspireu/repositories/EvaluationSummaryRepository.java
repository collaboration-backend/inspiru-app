package com.stc.inspireu.repositories;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.EvaluationSummary;
import com.stc.inspireu.models.IntakeProgramSubmission;

@Transactional
public interface EvaluationSummaryRepository
		extends PagingAndSortingRepository<EvaluationSummary, Long>, JpaSpecificationExecutor<EvaluationSummary> {
    boolean existsByIntakeProgramSubmission(IntakeProgramSubmission submission);

    EvaluationSummary findByIntakeProgramSubmission(IntakeProgramSubmission submission);
	List<EvaluationSummary> findByIntakeProgram_IdAndEmailAndPhase(Long id, String email, String phase);

	Page<EvaluationSummary> findByIntakeProgram_IdAndSubmittedUser_IdAndPhase(Long intakeProgramId, Long judgeId,
			String string, Pageable paging);

	Page<EvaluationSummary> findByIntakeProgram_IdAndPhase(Long intakeProgramId, String string, Pageable paging);

	List<EvaluationSummary> findByIntakeProgram_IdAndPhaseAndEmail(Long intakeProgramId, String phase, String email);

	EvaluationSummary findByIdAndPhase(Long submissionId, String phase);

	Page<EvaluationSummary> findByIntakeProgram_IdAndStatus(Long intakeProgramId, String status, Pageable paging);

	EvaluationSummary findByIdAndIntakeProgram_IdAndPhaseAndStatus(Long registrationId, Long intakeProgramId,
			String string, String string2);

	EvaluationSummary findByIdAndPhaseContaining(Long submissionId, String phase);

	Page<EvaluationSummary> findByIntakeProgram_IdAndPhaseContainingAndStatus(Long intakeProgramId, String phase,
			String string, Pageable paging);

	Page<EvaluationSummary> findByIntakeProgram_IdAndPhaseAndStatus(Long intakeProgramId, String phase, String string,
			Pageable paging);

	List<EvaluationSummary> findByIntakeProgram_IdAndPhase(Long intakeProgramId, String string);

	EvaluationSummary findByEmail(String registratedEmailAddress);

	Page<EvaluationSummary> findByIntakeProgram_IdAndEmailAndPhaseAndStatus(Long intakeProgramId, String filterKeyword,
			String phase, String string, Pageable paging);

	Page<EvaluationSummary> findByIntakeProgram_IdAndEmailAndPhaseContainingAndStatus(Long intakeProgramId,
			String filterKeyword, String phase, String string, Pageable paging);

	default Page<EvaluationSummary> getJudgeSummaryForAssessment(Long intakeProgramId, String phase, String filterBy,
			String filterKeyword, Pageable paging) {
		return (Page<EvaluationSummary>) findAll(
				EvaluationSummarySpecs.getJudgeSummaryForAssessment(intakeProgramId, phase, filterBy, filterKeyword),
				paging);
	}

	default Page<EvaluationSummary> getJudgeSummaryForBootcamp(Long intakeProgramId, String phase, String filterBy,
			String filterKeyword, Pageable paging) {
		return (Page<EvaluationSummary>) findAll(
				EvaluationSummarySpecs.getJudgeBootcampEvaluations(intakeProgramId, phase, filterBy, filterKeyword),
				paging);
	}

}

class EvaluationSummarySpecs {

	static Specification<EvaluationSummary> getJudgeSummaryForAssessment(Long intakeProgramId, String sts,
			String filterBy, String filterKeyword) {

		return new Specification<EvaluationSummary>() {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<EvaluationSummary> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {

				List<Predicate> predicates = new ArrayList<Predicate>();

				Predicate p1 = criteriaBuilder.equal(root.get("intakeProgram").get("id"), intakeProgramId);

				Path<String> phase = root.<String>get("phase");

				Predicate p2 = criteriaBuilder.equal(root.get("phase"), sts);

				Predicate p3 = criteriaBuilder.and(p1, p2);

				if (!StringUtils.isEmpty(filterBy) && !StringUtils.isEmpty(filterKeyword)) {
					Predicate p4 = criteriaBuilder.like(criteriaBuilder.lower(root.get(filterBy)),
							"%" + filterKeyword.toLowerCase() + "%");

					Predicate p5 = criteriaBuilder.and(p3, p4);

					predicates.add(p5);
				} else {
					predicates.add(p3);
				}

				Predicate[] predicatesArray = new Predicate[predicates.size()];

				return criteriaBuilder.and(predicates.toArray(predicatesArray));

			}
		};
	}

	static Specification<EvaluationSummary> getJudgeBootcampEvaluations(Long intakeProgramId, String sts,
			String filterBy, String filterKeyword) {

		return new Specification<EvaluationSummary>() {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<EvaluationSummary> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {

				List<Predicate> predicates = new ArrayList<Predicate>();

				Predicate p1 = criteriaBuilder.equal(root.get("intakeProgram").get("id"), intakeProgramId);

				Path<String> phase = root.<String>get("phase");

				Predicate p2 = criteriaBuilder.equal(root.get("phase"), sts);

				Predicate p3 = criteriaBuilder.and(p1, p2);

				if (!StringUtils.isEmpty(filterBy) && !StringUtils.isEmpty(filterKeyword)) {
					Predicate p4 = criteriaBuilder.like(criteriaBuilder.lower(root.get(filterBy)),
							"%" + filterKeyword.toLowerCase() + "%");

					Predicate p5 = criteriaBuilder.and(p3, p4);

					predicates.add(p5);
				} else {
					predicates.add(p3);
				}

				Predicate[] predicatesArray = new Predicate[predicates.size()];

				return criteriaBuilder.and(predicates.toArray(predicatesArray));

			}
		};
	}

}
