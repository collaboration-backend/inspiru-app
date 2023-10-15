package com.stc.inspireu.repositories;

import com.stc.inspireu.models.NotifiedUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

@Transactional
public interface NotifiedUserRepository extends PagingAndSortingRepository<NotifiedUser, Long> {

	@Modifying
	@Query("DELETE FROM NotifiedUser m WHERE m.createdOn < :date")
	void removeOlderThan(@Param("date") Date date);

	@Query("select u.notificationId from NotifiedUser u where u.userId = :userId")
	Set<Long> getNotifiedAlertByUserId(@Param("userId") Long userId);

}
