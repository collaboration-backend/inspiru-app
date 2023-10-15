package com.stc.inspireu.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.Feedback;

@Transactional
public interface FeedbackRepository
		extends PagingAndSortingRepository<Feedback, Long>, JpaSpecificationExecutor<Feedback> {

	@Query(value = "select o from Feedback o where o.workshopSession.id = :workshopSessionId AND o.forStartup.id = :startupId")
	List<Feedback> getByFeedbacksByWorkshopSessionIdAndStartupId(@Param("workshopSessionId") Long workshopSessionId,
			@Param("startupId") Long startupId);

	Page<Feedback> findAll(Specification<Feedback> spec, Pageable pageable);

	List<Feedback> findByForStartup_IdAndWorkshopSession_IdAndRefFeedback_Status(Long id, Long workshopSessionId,
			String status);

	Optional<Feedback> findByForStartup_IdAndWorkshopSession_IdAndRefFeedback_Id(Long startupId, Long workshopSessionId,
			Long refFeedbackId);

	Feedback findByIdAndWorkshopSession_IdAndWorkshopSession_AcademyRoom_IdAndForStartupIsNull(Long feedbackId,
			Long workshopSessionId, Long academyRoomId);

	long countByRefFeedback_IdAndRefFeedback_WorkshopSession_IdAndRefFeedback_WorkshopSession_AcademyRoom_Id(
			Long refFeedbackId, Long workshopSessionId, Long academyRoomId);

	List<Feedback> findBySubmittedUser_IdAndRefFeedback_Id(Long id, Long feedbackId);

	@Query("select u from Feedback u where u.submittedUser.id = :id and u.refFeedback.id = :feedbackId and u.forStartup is not null and lower(u.forStartup.startupName) like %:filterKeyword%")
	public List<Feedback> getPRByKeyword(Long id, String filterKeyword, Long feedbackId);

	Optional<Feedback> findByForStartup_IdAndWorkshopSession_IdAndRefFeedback_IdAndSubmittedUser_Id(Long startupId,
			Long workshopSessionId, Long refFeedbackId, Long id);

}
