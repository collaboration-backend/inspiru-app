package com.stc.inspireu.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectStatusByKeyValueLabel;
import com.stc.inspireu.models.Status;

@Transactional
public interface StatusRepository extends PagingAndSortingRepository<Status, Long> {

	@Query(value = "select o from Status o")
	Iterable<ProjectStatusByKeyValueLabel> getAll();

}
