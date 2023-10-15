package com.stc.inspireu.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectRevenueModelByKeyValueLabel;
import com.stc.inspireu.models.RevenueModel;

@Transactional
public interface RevenueModelRepository extends PagingAndSortingRepository<RevenueModel, Long> {

	@Query(value = "select o from RevenueModel o")
	Iterable<ProjectRevenueModelByKeyValueLabel> getAll();

}
