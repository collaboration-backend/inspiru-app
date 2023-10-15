package com.stc.inspireu.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.Milestone;

@Transactional
public interface MilestoneRepository extends PagingAndSortingRepository<Milestone, Long> {

}
