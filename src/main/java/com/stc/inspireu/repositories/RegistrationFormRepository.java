package com.stc.inspireu.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.RegistrationForm;

import java.util.Optional;

@Transactional
public interface RegistrationFormRepository extends PagingAndSortingRepository<RegistrationForm, Long> {

	RegistrationForm findByIntakeProgram_Id(Long id);

	Page<RegistrationForm> findByFormNameContainingIgnoreCase(String name, Pageable paging);

	Page<RegistrationForm> findByIntakeProgramIsNull(Pageable paging);

	Page<RegistrationForm> findByFormNameContainingIgnoreCaseAndIntakeProgramIsNull(String name, Pageable paging);

	RegistrationForm findByIntakeProgram_IdAndFormNameIgnoreCase(Long intakePgmId, String registrationFormName);

	RegistrationForm findByIntakeProgramIsNullAndFormNameIgnoreCase(String registrationFormName);

    Optional<RegistrationForm> findByFormName(String registrationFormName);

}
