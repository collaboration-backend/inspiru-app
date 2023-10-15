package com.stc.inspireu.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectSegmentByKeyValueLabel;
import com.stc.inspireu.models.Segment;

@Transactional
public interface SegmentRepository extends PagingAndSortingRepository<Segment, Long> {

	@Query(value = "select o from Segment o")
	Iterable<ProjectSegmentByKeyValueLabel> getAll();

}
