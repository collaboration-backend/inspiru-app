package com.stc.inspireu.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.TrainingMaterial;

@Transactional
public interface TrainingMaterialRepository
		extends PagingAndSortingRepository<TrainingMaterial, Long>, JpaSpecificationExecutor<TrainingMaterial> {

	Page<TrainingMaterial> findAllByWorkshopSession_Id(Long workshopSessionId, Pageable paging);

	Page<TrainingMaterial> findAll(Specification<TrainingMaterial> spec, Pageable pageable);

	Optional<TrainingMaterial> findByIdAndWorkshopSession_Id(Long trainingFileId, Long workshopSessionId);

	Page<TrainingMaterial> findAllByWorkshopSession_IdAndName(Long workshopSessionId, String name, Pageable paging);

}
