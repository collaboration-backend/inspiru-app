package com.stc.inspireu.repositories;

import java.util.Date;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectNotification;
import com.stc.inspireu.models.Notification;

@Transactional
public interface NotificationRepository extends PagingAndSortingRepository<Notification, Long> {

	@Modifying
	@Query("DELETE FROM Notification m WHERE m.createdOn < :date")
	void removeOlderThan(@Param("date") Date date);

	@Query("select u from Notification u where (u.targetStartup.id = :startupId or u.targetUser.id = :userId) and u.keywords not like %:exclude1% and u.id not in :notifiedIds AND u.targetDate is null")
	Page<ProjectNotification> startupAdminAlerts(@Param("startupId") Long startupId, @Param("userId") Long userId,
			@Param("exclude1") String exclude1, @Param("notifiedIds") Set<Long> notifiedIds, Pageable paging);

	@Query("select u from Notification u where (u.targetStartup.id = :startupId or u.targetUser.id = :userId) and u.keywords not like %:exclude1% and u.keywords not like %:exclude2% and u.id not in :notifiedIds AND u.targetDate is null")
	Page<ProjectNotification> startupMemberAlerts(@Param("startupId") Long startupId, @Param("userId") Long userId,
			@Param("exclude1") String exclude1, @Param("exclude2") String exclude2,
			@Param("notifiedIds") Set<Long> notifiedIds, Pageable paging);

	@Query("select u from Notification u where u.keywords not like %:exclude1% and u.id not in :notifiedIds AND u.targetDate is null")
	Page<ProjectNotification> superAdminAlerts(@Param("exclude1") String exclude1,
			@Param("notifiedIds") Set<Long> notifiedIds, Pageable paging);

	@Query("select u from Notification u where u.targetUser.id = :userId and u.keywords not like %:exclude1% and u.id not in :notifiedIds AND u.targetDate is null")
	Page<ProjectNotification> managementMemberAlerts(@Param("userId") Long userId, @Param("exclude1") String exclude1,
			@Param("notifiedIds") Set<Long> notifiedIds, Pageable paging);

	@Query("select u from Notification u")
	Page<ProjectNotification> getAlltime(Pageable paging);

}
