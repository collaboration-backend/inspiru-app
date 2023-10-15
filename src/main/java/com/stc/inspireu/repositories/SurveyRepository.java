package com.stc.inspireu.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectId;
import com.stc.inspireu.jpa.projections.ProjectIdNameDueDateStatusSurvey;
import com.stc.inspireu.models.Survey;

@Transactional
public interface SurveyRepository extends PagingAndSortingRepository<Survey, Long>, JpaSpecificationExecutor<Survey> {

	List<ProjectIdNameDueDateStatusSurvey> findByWorkshopSessionIdAndSubmittedStartupIdIsNull(Long workshopSessionId);

	List<ProjectIdNameDueDateStatusSurvey> findByWorkshopSessionIdAndSubmittedStartupId(Long workshopSessionId,
			Long long1);

	@Query(value = "select o from Survey o where o.workshopSession.id = :workshopSessionId AND (o.submittedStartup is null OR o.submittedStartup.id = :startupId) AND o.id NOT IN (select c.refSurvey from Survey c where o.workshopSession.id = :workshopSessionId and c.submittedStartup.id = :startupId)")
	List<Survey> getBySurveysByWorkshopSessionIdAndStartupId(@Param("workshopSessionId") Long workshopSessionId,
			@Param("startupId") Long startupId);

	Optional<Survey> findByIdAndWorkshopSession_Id(Long surveyId, Long workshopSessionId);

	Optional<Survey> findByRefSurvey_IdAndSubmittedStartup_Id(Long surveyId, Long id);

	@Query(value = "select o from Survey o where ((o.workshopSession.id = :refWorkshopSessionId and o.submittedStartup is null and status = :status ) or (o.workshopSession.id = :workshopSessionId and o.submittedStartup.id = :startupId)) and o.id NOT IN (select c.refSurvey from Survey c where c.workshopSession.id = :workshopSessionId and c.submittedStartup.id = :startupId)")
	List<Survey> getStartupWorkshopSessionSurveys(@Param("startupId") Long startupId,
			@Param("refWorkshopSessionId") Long refWorkshopSessionId,
			@Param("workshopSessionId") Long workshopSessionId, @Param("status") String status);

	Page<Survey> findAll(Specification<Survey> spec, Pageable pageable);

	Survey findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNull(Long surveyId,
			Long workshopSessionId, Long academyRoomId);

	long countByRefSurvey_IdAndRefSurvey_WorkshopSession_IdAndRefSurvey_WorkshopSession_AcademyRoom_Id(Long refSurveyId,
			Long workshopSessionId, Long academyRoomId);

	Survey findByIdAndRefSurvey_WorkshopSession_IdAndRefSurvey_WorkshopSession_AcademyRoom_IdAndSubmittedStartupIsNotNull(
			Long surveyId, Long workshopSessionId, Long academyRoomId);

	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM Survey e WHERE e.id = :surveyId")
	void removeById(@Param("surveyId") Long surveyId);

	// mark card
	List<Survey> findByWorkshopSession_IdAndSubmittedStartupIsNullAndStatus(Long workshopSessionId, String status);

	List<Survey> findByWorkshopSession_IdAndSubmittedStartup_IdAndStatusIn(Long workshopSessionId, Long startupId,
			List<String> status);

	long countBySubmittedStartupId(long submittedStartupId);

	@Query("SELECT COUNT(o) FROM Survey o where o.submittedStartup.id = :submittedStartupId and o.workshopSession.refWorkshopSession.academyRoom.id = :academyRoomId")
	long countBySubmittedStartupIdAndAcademicRoomId(long submittedStartupId, Long academyRoomId);

	@Query("SELECT COUNT(o) FROM Survey o where o.submittedStartup is null and o.workshopSession.academyRoom.id = :academyRoomId")
	long countByTotalSurveys(Long academyRoomId);

	@Query("SELECT COUNT(o) FROM Survey o where o.submittedStartup.id = :startupId and o.workshopSession.academyRoom.id = :academyRoomId")
	long countByTotalSurveyAndStartupId(Long startupId, Long academyRoomId);

	long countByWorkshopSessionIdInAndStatusAndRefSurveyIsNull(Set<Long> wsIds, String string);

	Set<ProjectId> findByWorkshopSessionIdInAndStatusAndRefSurveyIsNull(Set<Long> wsIds, String string);

	long countByRefSurveyIdInAndStatusAndSubmittedStartup_Id(Set<Long> wsIds, String string, Long id);
}
