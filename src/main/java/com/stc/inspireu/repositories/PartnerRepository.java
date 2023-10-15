package com.stc.inspireu.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectPartner;
import com.stc.inspireu.models.Partner;

@Transactional
public interface PartnerRepository extends PagingAndSortingRepository<Partner, Long>{

	Page<ProjectPartner> findByUser_Id(Long userId, Pageable paging);

}
