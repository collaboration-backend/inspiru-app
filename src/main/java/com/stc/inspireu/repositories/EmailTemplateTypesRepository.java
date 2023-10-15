package com.stc.inspireu.repositories;

import com.stc.inspireu.models.EmailTemplatesTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface EmailTemplateTypesRepository extends PagingAndSortingRepository<EmailTemplatesTypes, Long> {
	Page<EmailTemplatesTypes> findByNameContainingIgnoreCase(String name, Pageable paging);
}
