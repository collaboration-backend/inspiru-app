package com.stc.inspireu.repositories;

import com.stc.inspireu.models.MarkCard2022;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MarkCard2022Repository extends PagingAndSortingRepository<MarkCard2022, Long> {

	@Query("select u from MarkCard2022 u where u.intakeProgram.programName =:filterKeyword")
	public Page<MarkCard2022> getByKeyword(String filterKeyword, Pageable paging);

}
