package com.stc.inspireu.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.DueDiligenceNote2021;

@Transactional
public interface DueDiligenceNote2021Repository extends PagingAndSortingRepository<DueDiligenceNote2021, Long> {

	Page<DueDiligenceNote2021> findByStartup_IdAndFieldId(Long startupId, String fieldId, Pageable pageable);

}
