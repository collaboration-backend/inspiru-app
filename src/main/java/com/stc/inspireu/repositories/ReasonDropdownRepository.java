package com.stc.inspireu.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.ReasonDropdown;

@Transactional
public interface ReasonDropdownRepository extends PagingAndSortingRepository<ReasonDropdown, Long> {

}
