package com.stc.inspireu.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.WorkshopSessionSubmissions;

@Transactional
public interface WorkshopSessionSubmissionsRepository
		extends PagingAndSortingRepository<WorkshopSessionSubmissions, Long>,
		JpaSpecificationExecutor<WorkshopSessionSubmissions> {

	Optional<WorkshopSessionSubmissions> findByFileTypeAndMetaDataIdAndWorkshopSession_Id(String fileType,
			Long fileTypeId, Long workshopSessionId);

	@Query(value = "select o from WorkshopSessionSubmissions o where (o.workshopSession.refWorkshopSession.academyRoom.id = :academyRoomId and o.workshopSession.refWorkshopSession.id = :workshopsessionId )")
	Page<WorkshopSessionSubmissions> getSubmissions(@Param("academyRoomId") Long academyRoomId,
			@Param("workshopsessionId") Long workshopsessionId, Pageable pageable);

	Page<WorkshopSessionSubmissions> findBySubmittedFileNameContainingIgnoreCaseAndWorkshopSessionId(String name,
			Long workShopSessionId, Pageable pageable);

	Page<WorkshopSessionSubmissions> findByWorkshopSessionId(Long workShopSessionId, Pageable pageable);

}
