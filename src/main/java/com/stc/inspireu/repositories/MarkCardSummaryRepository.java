package com.stc.inspireu.repositories;

import com.stc.inspireu.jpa.projections.ProjectMarkcardStartup;
import com.stc.inspireu.models.MarkCardSummary2022;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MarkCardSummaryRepository extends PagingAndSortingRepository<MarkCardSummary2022, Long> {

	Page<MarkCardSummary2022> findByAcademyRoomIdAndStartupIsNotNull(Long academyRoomId, Pageable pageable);

	Optional<MarkCardSummary2022> findByAcademyRoomIdAndStartupId(Long academyRoomId, Long startupId);

	MarkCardSummary2022 findByMarkCard_IdAndAcademyRoom_IdAndStartupIsNull(Long markCardId, Long academyRoomId);

	List<MarkCardSummary2022> findByMarkCard_IdAndAcademyRoom_IdAndStartupIsNotNull(Long markCardId,
			Long academyRoomId);

	List<MarkCardSummary2022> findByMarkCard_IdAndAcademyRoom_IdAndStartup_IdIn(Long markCardId, Long academyRoomId,
			List<Long> ids);

	MarkCardSummary2022 findByMarkCard_IdAndAcademyRoom_IdAndStartup_Id(Long markCardId, Long academyRoomId,
			Long startupId);

	@Query("SELECT new com.stc.inspireu.jpa.projections.ProjectMarkcardStartup(d.startup.id, d.startup.startupName) FROM MarkCardSummary2022 d LEFT JOIN d.startup e on d.startup.id = e.id where d.id = :markCardId and d.academyRoom.id = :academyRoomId")
	Page<ProjectMarkcardStartup> getMarkCardStartups(Long markCardId, Long academyRoomId, Pageable pageable);

	List<MarkCardSummary2022> findByStartup_IdAndProgressReportId(Long id, Long id2);

	@Query("SELECT d FROM MarkCardSummary2022 d where d.startup.id = :id and d.progressReportId = :id2 and d.amountPaid is not null")
	List<MarkCardSummary2022> getByStartupIdAndProgressReportId(Long id, Long id2);

	@Query("SELECT COUNT(o) FROM MarkCardSummary2022 o WHERE o.markCard.id = :markCardId and o.academyRoom.id = :academyRoomId and o.startup is not null")
	long getCountSubmittedStartups(Long markCardId, Long academyRoomId);

	List<MarkCardSummary2022> findByMarkCard_IdAndStartupIsNull(Long markCardId);

	List<MarkCardSummary2022> findByMarkCard_IdAndStartupIsNullAndIsMarkCardGeneratedTrue(Long markCardId);

	List<MarkCardSummary2022> findByMarkCard_IdAndStartup_IdIn(Long markCardId, Set<Long> startupIds);

}
