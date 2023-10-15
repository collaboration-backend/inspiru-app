package com.stc.inspireu.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.ProfileCard;

public interface ProfileCardRepository extends PagingAndSortingRepository<ProfileCard, Long> {

	@Transactional
	Page<ProfileCard> findByNameContainingIgnoreCase(String name, Pageable paging);

	@Transactional
	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM ProfileCard e WHERE e.id = :profileCardId")
	int removeProfileCardById(@Param("profileCardId") Long profileCardId);

	Page<ProfileCard> findByNameContainingIgnoreCaseAndIntakeProgramIsNull(String name, Pageable paging);

	Page<ProfileCard> findByIntakeProgramIsNull(Pageable paging);

	List<ProfileCard> findByIntakeProgramIsNullAndNameIgnoreCase(String profileCardName);

    boolean existsByIntakeProgramIsNullAndNameIgnoreCase(String profileCardName);
}
