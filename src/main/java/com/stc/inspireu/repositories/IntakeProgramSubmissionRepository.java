package com.stc.inspireu.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.stc.inspireu.models.IntakeProgram;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectIntakeProgramSubmission;
import com.stc.inspireu.models.IntakeProgramSubmission;

@Transactional
public interface IntakeProgramSubmissionRepository extends PagingAndSortingRepository<IntakeProgramSubmission, Long>,
    JpaSpecificationExecutor<IntakeProgramSubmission> {

    Page<IntakeProgramSubmission> findAllByEmailOrBeneficiaryIdOrderByIdDesc(String email, Long beneficiaryId, Pageable pageable);

    List<IntakeProgramSubmission> findByIntakeProgram_IdAndPhaseAndEmail(Long long1, String string, String string2);

    Page<IntakeProgramSubmission> findByIntakeProgram_IdAndPhaseContainingIgnoreCase(Long intakeProgramId, String phase,
                                                                                     Pageable paging);

    Optional<IntakeProgramSubmission> findByIntakeProgramAndStartupNameIgnoreCase(IntakeProgram intakeProgram, String startupName);

    @Query(value = "select ips.*\n" +
        "from intake_program_submissions ips \n" +
        "inner join screening_evaluators se on se.application_id = ips.id \n" +
        "left outer join evaluation_summaries es on es.intakeprogramsubmission_id=ips.id\n" +
        "left outer join evaluation_judge_summaries ejs on ejs.evaluationsummaryid=es.id and ejs.submittedjudgeid=se.evaluator_user_id\n" +
        "where se.evaluator_user_id=:userId and ejs.id is null and ips.phase='SCREENING_EVALUATION_START' order by ips.screening_evaluation_started_on desc limit 1", nativeQuery = true)
    Optional<IntakeProgramSubmission> findScreeningEvaluationQueue(Long userId);

    @Query(value = "select ips.*\n" +
        "from intake_program_submissions ips \n" +
        "inner join assessment_judges se on se.intakeprogramsubmissionid = ips.id \n" +
        "left outer join evaluation_summaries es on es.intakeprogramsubmission_id=ips.id\n" +
        "left outer join evaluation_judge_summaries ejs on ejs.evaluationsummaryid=es.id and ejs.submittedjudgeid=se.userid\n" +
        "where se.userid=:userId and (ejs.id is null or ejs.jsonform is null) and ips.phase='ASSESSMENT_EVALUATION_START' order by ips.evaluationstartedon desc limit 1", nativeQuery = true)
    Optional<IntakeProgramSubmission> findAssessmentEvaluationQueue(Long userId);

    @Query(value = "select ips.*\n" +
        "from intake_program_submissions ips \n" +
        "inner join bootcamp_judges se on se.intakeprogramsubmissionid = ips.id \n" +
        "left outer join evaluation_summaries es on es.intakeprogramsubmission_id=ips.id\n" +
        "left outer join evaluation_judge_summaries ejs on ejs.evaluationsummaryid=es.id and ejs.submittedjudgeid=se.userid\n" +
        "where se.userid=:userId and (ejs.id is null or ejs.jsonformbootcamp is null) and ips.phase='BOOTCAMP_EVALUATION_START' order by ips.bootcamp_evaluation_started_on desc limit 1", nativeQuery = true)
    Optional<IntakeProgramSubmission> findBootcampEvaluationQueue(Long userId);

    Page<IntakeProgramSubmission> findByIntakeProgram_IdAndPhaseContainingIgnoreCaseAndStartupNameContainingIgnoreCase(
        Long intakeProgramId, String phase, String keyword, Pageable paging);

    @Query("SELECT COUNT(e) FROM IntakeProgramSubmission e WHERE e.intakeProgram.id = :intakeProgramId and day(e.createdOn) = :day")
    long getTrendCountByDay(@Param("intakeProgramId") Long intakeProgramId, @Param("day") int day);

    @Query("SELECT COUNT(e) FROM IntakeProgramSubmission e WHERE e.intakeProgram.id = :intakeProgramId")
    long totalCount(@Param("intakeProgramId") Long intakeProgramId);

    Page<IntakeProgramSubmission> findByIntakeProgram_IdAndPhase(Long intakeProgramId, String string, Pageable paging);

    Page<IntakeProgramSubmission> findByIntakeProgram_IdAndPhaseAndScreeningEvaluatorsEmpty(Long intakeProgramId, String string, Pageable paging);

    Page<IntakeProgramSubmission> findByEmailContainingIgnoreCaseAndIntakeProgram_IdAndPhase(String email,
                                                                                             Long intakeProgramId, String string, Pageable paging);

    Page<IntakeProgramSubmission> findByEmailContainingIgnoreCaseAndIntakeProgram_IdAndPhaseAndScreeningEvaluatorsEmpty(String email,
                                                                                                                        Long intakeProgramId, String string, Pageable paging);

    Page<IntakeProgramSubmission> findByJsonRegistrationFormContainingIgnoreCaseAndIntakeProgram_IdAndPhase(
        String email, Long intakeProgramId, String string, Pageable paging);

    Page<IntakeProgramSubmission> findByJsonRegistrationFormContainingIgnoreCaseAndIntakeProgram_IdAndPhaseAndScreeningEvaluatorsEmpty(
        String email, Long intakeProgramId, String string, Pageable paging);

    Optional<IntakeProgramSubmission> findByIdAndIntakeProgram_Id(Long registrationId, Long intakeProgramId);

    Page<IntakeProgramSubmission> findByUsers_Id(Long id, Pageable paging);

    Page<IntakeProgramSubmission> findByPhase(String string, Pageable paging);

    Page<IntakeProgramSubmission> findByPhaseContaining(String string, Pageable paging);

    Optional<IntakeProgramSubmission> findByEmailAndIntakeProgram_Id(String email, Long intakeProgramId);

    List<IntakeProgramSubmission> findByUsers_Id(Long id);

    IntakeProgramSubmission findByIdAndIntakeProgram_IdAndUsers_Id(Long assessmentId, Long intakeProgramId, Long id);

    IntakeProgramSubmission findByIdAndIntakeProgram_IdAndBootcampUsers_Id(Long submissionId, Long intakeProgramId,
                                                                           Long id);

    IntakeProgramSubmission findByIdAndIntakeProgram_IdAndScreeningEvaluators_Id(Long submissionId, Long intakeProgramId,
                                                                                 Long id);

    Page<IntakeProgramSubmission> findByBootcampUsers_Id(Long id, Pageable paging);

    List<IntakeProgramSubmission> findByIntakeProgram_IdAndPhase(Long intakeProgramId, String string);

    boolean existsByIntakeProgram_IdAndPhase(Long intakeProgramId, String string);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE IntakeProgramSubmission c SET c.phase = :phase WHERE c.intakeProgram.id = :intakeProgramId and c.email = :email")
    int updateAssesmentNextPhaseState(@Param("intakeProgramId") Long intakeProgramId, @Param("email") String email,
                                      @Param("phase") String phase);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE IntakeProgramSubmission c SET c.phase = :phase WHERE c.intakeProgram.id = :intakeProgramId and c.email = :email")
    int updateBootcampEnd(@Param("intakeProgramId") Long intakeProgramId, @Param("email") String email,
                          @Param("phase") String phase);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE IntakeProgramSubmission c SET c.phase = :t WHERE c.intakeProgram.id = :intakeProgramId and c.phase = :f")
    int updateBootcampFinish(@Param("intakeProgramId") Long intakeProgramId, @Param("f") String f,
                             @Param("t") String t);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE IntakeProgramSubmission c SET c.phase = :t WHERE c.intakeProgram.id = :intakeProgramId and c.email = :email and c.phase = :f")
    int bootcampFinish(@Param("intakeProgramId") Long intakeProgramId, @Param("email") String email,
                       @Param("f") String f, @Param("t") String t);

    @Query(value = "select o from IntakeProgramSubmission o where o.intakeProgram.id = :intakeProgramId and o.phase IN (:phase1, :phase2, :phase3)")
    Page<IntakeProgramSubmission> getEvaluations(@Param("intakeProgramId") Long intakeProgramId,
                                                 @Param("phase1") String phase1, @Param("phase2") String phase2, @Param("phase3") String phase3,
                                                 Pageable paging);

    @Query(value = "select o from IntakeProgramSubmission o where o.intakeProgram.id = :intakeProgramId and o.email like %:email% and o.phase IN (:phase1, :phase2, :phase3)")
    Page<IntakeProgramSubmission> getEvaluationsByEmail(@Param("intakeProgramId") Long intakeProgramId,
                                                        @Param("phase1") String phase1, @Param("phase2") String phase2, @Param("phase3") String phase3,
                                                        Pageable paging, String email);

    @Query(value = "select o from IntakeProgramSubmission o where o.intakeProgram.id = :intakeProgramId and o.jsonRegistrationForm like %:startUp% and o.phase IN (:phase1, :phase2, :phase3)")
    Page<IntakeProgramSubmission> getEvaluationsByStartUp(@Param("intakeProgramId") Long intakeProgramId,
                                                          @Param("phase1") String phase1, @Param("phase2") String phase2, @Param("phase3") String phase3,
                                                          Pageable paging, String startUp);

    Page<IntakeProgramSubmission> findAllByPhaseInAndScreeningEvaluatorsNotEmptyAndIntakeProgram_id(List<String> phases, Long intakeId, Pageable pageable);

    Page<IntakeProgramSubmission> findAllByPhaseInAndScreeningEvaluatorsNotEmptyAndIntakeProgram_idAndStartupNameContainsIgnoreCase(List<String> phases, Long intakeId, String startupName, Pageable pageable);

    Page<IntakeProgramSubmission> findAllByPhaseInAndScreeningEvaluatorsNotEmptyAndIntakeProgram_idAndEmailContainsIgnoreCase(List<String> phases, Long intakeId, String startupName, Pageable pageable);

    List<ProjectIntakeProgramSubmission> findByPhaseAndUsers_Id(String string, Long id);

    List<ProjectIntakeProgramSubmission> findByPhaseAndBootcampUsers_Id(String string, Long id);

    Page<IntakeProgramSubmission> findByIntakeProgram_IdAndPhaseAndLanguage(Long intakeProgramId, String string,
                                                                            String language, Pageable paging);

    List<IntakeProgramSubmission> findByIntakeProgramAndPhaseOrderByIdDesc(IntakeProgram intakeProgram, String phase);

    List<IntakeProgramSubmission> findAllByIdInAndIntakeProgramOrderByIdDesc(List<Long> ids, IntakeProgram intakeProgram);

    Page<IntakeProgramSubmission> findByEmailContainingIgnoreCaseAndIntakeProgram_IdAndPhaseAndLanguage(String email,
                                                                                                        Long intakeProgramId, String string, String language, Pageable paging);


    Page<IntakeProgramSubmission> findByJsonRegistrationFormContainingIgnoreCaseAndIntakeProgram_IdAndPhaseAndLanguage(
        String email, Long intakeProgramId, String string, String language, Pageable paging);

    IntakeProgramSubmission findByEmail(String registratedEmailAddress);

    List<IntakeProgramSubmission> findByIntakeProgram_IdAndEmailAndPhaseContainingIgnoreCase(Long id, String email,
                                                                                             String string);
    boolean existsByIntakeProgram_IdAndEmailAndPhaseContainingIgnoreCase(Long id, String email,
                                                                                             String string);
    default Page<IntakeProgramSubmission> getJudgeAssessmentEvaluations(Long intakeProgramId, String sts,
                                                                        List<String> status, String filterBy, String filterKeyword, Pageable paging) {
        return (Page<IntakeProgramSubmission>) findAll(IntakeProgramSubmissionSpecs
            .getJudgeAssessmentEvaluations(intakeProgramId, sts, status, filterBy, filterKeyword), paging);
    }

    default Page<IntakeProgramSubmission> getJudgeBootcampEvaluations(Long intakeProgramId, String sts,
                                                                      List<String> status, String filterBy, String filterKeyword, Pageable paging) {
        return (Page<IntakeProgramSubmission>) findAll(IntakeProgramSubmissionSpecs
            .getJudgeBootcampEvaluations(intakeProgramId, sts, status, filterBy, filterKeyword), paging);
    }

    List<IntakeProgramSubmission> findTop2ByIntakeProgram_IdAndPhaseContainingIgnoreCase(Long intakeProgramId,
                                                                                         String phase);

    long countByIntakeProgram_IdAndPhaseContainingIgnoreCase(Long intakeProgramId, String phase);

    @Query("select u.id from IntakeProgramSubmission u where u.intakeProgram.id = :intakeProgramId and (LOWER(u.phase) like %:containg% OR u.isAbsent = TRUE)")
    List<Long> getFinalSummary(Long intakeProgramId, String containg);

    @Query("select u.id from IntakeProgramSubmission u where u.intakeProgram.id = :intakeProgramId and (LOWER(u.phase) like %:containg% OR u.isAbsentBootcamp = TRUE)")
    List<Long> getFinalSummaryBootcamp(Long intakeProgramId, String containg);

    @Query("select u.id from IntakeProgramSubmission u where u.intakeProgram.id = :intakeProgramId and (LOWER(u.phase) like %:containg% OR u.isAbsent = TRUE)")
    List<Long> getAssessmentIds(Long intakeProgramId, String containg);

    @Query("select u.id from IntakeProgramSubmission u where u.intakeProgram.id = :intakeProgramId and (LOWER(u.phase) like %:containg% OR u.isAbsentBootcamp = TRUE)")
    List<Long> getBootcampIds(Long intakeProgramId, String containg);

    @Query(value = "select DISTINCT userId from assessment_judges where intakeProgramSubmissionId in :ipss2", nativeQuery = true)
    Set<Long> getAllUserAssessment(Set<Long> ipss2);

    @Query(value = "select DISTINCT userId from bootcamp_judges where intakeProgramSubmissionId in :ipss2", nativeQuery = true)
    Set<Long> getAllUserBootcamp(Set<Long> ipss2);

    @Query(value = "select userId from assessment_judges where intakeProgramSubmissionId = :ipss2", nativeQuery = true)
    Set<Long> getUserIdsFromAssessmentWhere(Long ipss2);

    @Query(value = "select userId from bootcamp_judges where intakeProgramSubmissionId = :ipss2", nativeQuery = true)
    Set<Long> getUserIdsFromBootcampWhere(Long ipss2);

    @Query(value = "select evaluator_user_id from screening_evaluators where application_id = :ipss2", nativeQuery = true)
    Set<Long> getUserIdsFromScreening(Long ipss2);

    @Modifying
    @Query(nativeQuery = true, value = "update intake_program_submissions set jsonassessmentevaluationform=:assessmentFormJson where intakeprogramid=:intakeProgramId")
    void updateAssessmentFormJson(Long intakeProgramId, String assessmentFormJson);

    @Modifying
    @Query(nativeQuery = true, value = "update intake_program_submissions set jsonbootcampevaluationform=:bootcampFormJson where intakeprogramid=:intakeProgramId")
    void updateBootcampFormJson(Long intakeProgramId, String bootcampFormJson);

    @Modifying
    @Query(nativeQuery = true, value = "update intake_program_submissions set jsonpreassessmentevaluationform=:preAssessmentFormJson where intakeprogramid=:intakeProgramId")
    void updatePreAssessmentFormJson(Long intakeProgramId, String preAssessmentFormJson);

    Optional<IntakeProgramSubmission> findByIntakeProgram_IdAndId(Long intakeId, Long submissionId);
}

class IntakeProgramSubmissionSpecs {

    public static Specification<IntakeProgramSubmission> getJudgeAssessmentEvaluations(Long intakeProgramId, String sts,
                                                                                       List<String> status, String filterBy, String filterKeyword) {

        return new Specification<IntakeProgramSubmission>() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public Predicate toPredicate(Root<IntakeProgramSubmission> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<Predicate>();

                Predicate p1 = criteriaBuilder.equal(root.get("intakeProgram").get("id"), intakeProgramId);

                Path<String> phase = root.<String>get("phase");

//                Predicate p2 = phase.in(status);
                Predicate p2 = criteriaBuilder.like(criteriaBuilder.lower(root.get("phase")),
                    "%" + sts.toLowerCase() + "%");

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

    public static Specification<IntakeProgramSubmission> getJudgeBootcampEvaluations(Long intakeProgramId, String sts,
                                                                                     List<String> status, String filterBy, String filterKeyword) {

        return new Specification<IntakeProgramSubmission>() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public Predicate toPredicate(Root<IntakeProgramSubmission> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<Predicate>();

                Predicate p1 = criteriaBuilder.equal(root.get("intakeProgram").get("id"), intakeProgramId);

                Path<String> phase = root.<String>get("phase");

//                Predicate p2 = phase.in(status);
                Predicate p2 = criteriaBuilder.like(criteriaBuilder.lower(root.get("phase")),
                    "%" + sts.toLowerCase() + "%");

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
