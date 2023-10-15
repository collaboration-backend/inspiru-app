package com.stc.inspireu.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.UserResourcePermission;

@Transactional
public interface UserResourcePermissionRepository extends PagingAndSortingRepository<UserResourcePermission, Long> {

	@Query("select u.resourceId from UserResourcePermission u where u.userId = :userId and u.resource = :resource")
	Set<Long> getAcademyRoomResourceIds(@Param("userId") Long userId, @Param("resource") String resource);

	@Query("select u.actionsOrMethods from UserResourcePermission u where u.userId = :userId and u.resourceId = :academyRoomId and u.resource = :resource and u.actionsOrMethods = :actionsOrMethods")
	Set<String> getAcademyRoomResourceResources(@Param("userId") Long userId,
			@Param("academyRoomId") Long academyRoomId, @Param("resource") String resource,
			@Param("actionsOrMethods") String actionsOrMethods);

	@Query("select u.resourceId from UserResourcePermission u where u.userId = :userId and u.resource = :resource")
	Set<Long> getWorkshpSessionResourceIds(@Param("userId") Long userId, @Param("resource") String resource);

	List<UserResourcePermission> findByResourceAndResourceIdAndUserIdNot(String resource, Long resourceId, Long userId);

	Optional<UserResourcePermission> findByResourceAndResourceIdAndUserId(String resource, Long resourceId,
			Long userId);

	@Query("select u.actionsOrMethods from UserResourcePermission u where u.userId = :userId and u.resourceId = :resourceId and u.resource = :resource")
	Set<String> getPermissions(@Param("userId") Long userId, @Param("resourceId") Long resourceId,
			@Param("resource") String resource);

	void deleteAllByUserIdAndResourceIdAndResource(Long l, Long academyRoomId, String mar);

	void deleteAllByUserIdInAndResourceIdAndResource(List<Long> sharedUserIds, long l, String mar);

	UserResourcePermission findByUserIdAndResourceIdAndResource(Long sharedUserId, Long academyRoomId, String mar);

	void deleteAllByParentResourceIdOrGrandParentResourceId(Long academyRoomId, Long academyRoomId2);

	@Modifying
	@Query(value = "DELETE FROM UserResourcePermission e WHERE e.userId = :userId AND (e.parentResourceId = :resourceId OR e.grandParentResourceId = :resourceId OR e.resourceId = :resourceId)")
	void removeAllAcademyRoomChildResource(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

	List<UserResourcePermission> findByResourceAndResourceId(String resource, Long resourceId);

	List<UserResourcePermission> findByResourceAndParentResourceId(String resource, Long parentResourceId);

	@Query("select u from UserResourcePermission u where u.resourceId = :academyRoomId and u.resource = :resource and u.actionsOrMethods <> :actionsOrMethods and u.userId IN (:sharedUserIds)")
	List<UserResourcePermission> getPartialPermissions(@Param("resource") String resource,
			@Param("academyRoomId") Long academyRoomId, @Param("actionsOrMethods") String actionsOrMethods,
			@Param("sharedUserIds") List<Long> sharedUserIds);

	List<UserResourcePermission> findByResourceAndParentResourceIdAndResourceIdNotAndUserIdIn(String resource,
			Long parentResourceId, Long resourceId, List<Long> sharedUserIds);

	List<UserResourcePermission> findByUserIdAndResourceAndParentResourceId(Long sharedUserId, String resource,
			Long parentResourceId);

	List<UserResourcePermission> findByUserIdAndResourceAndParentResourceIdAndResourceIdNot(Long sharedUserId,
			String resource, Long parentResourceId, Long resourceId);

	UserResourcePermission findByUserIdAndResourceIdAndResourceAndParentResourceId(Long sharedUserId,
			Long academyRoomId, String mmcar, Long parentResourceId);

	@Query("select u.resourceId from UserResourcePermission u where u.userId = :userId and u.resource = :resource and u.parentResourceId = :parentResourceId")
	Set<Long> getMarkCardAcademyRoomResourceIds(@Param("userId") Long userId, @Param("resource") String resource,
			Long parentResourceId);

	@Query("select u.resourceId from UserResourcePermission u where (u.userId = :userId or u.startupId = :startupId) and u.resource = :resource")
	Set<Long> getFileFolderIds(Long userId, Long startupId, String resource);

	@Query("select u.userId from UserResourcePermission u where u.resourceId = :resourceId and u.resource = :resource")
	Set<Long> getUsersByResource(@Param("resourceId") Long resourceId, @Param("resource") String resource);

	@Query("select u.resourceId from UserResourcePermission u where u.userId = :userId and u.resource = :resource")
	Set<Long> getFileFolderResources(Long userId, String resource);

	@Query("select u.resourceId from UserResourcePermission u where u.startupId = :startupId and u.resourceId = :resourceId and u.resource = :resource")
	Set<Long> getFileFolderResourcesForStartups(Long startupId, Long resourceId, String resource);

	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM UserResourcePermission e WHERE e.resourceId = :resourceId")
	void removeById(Long resourceId);

	@Query("select u.userId from UserResourcePermission u where u.resourceId = :resourceId and u.resource = :resource")
	Set<Long> sharedMembersFileFolders(Long resourceId, String resource);

	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM UserResourcePermission e WHERE e.userId = :userId and e.resourceId = :resourceId and e.resource = :resource")
	void removeByUserIdAndResourceId(Long userId, Long resourceId, String resource);

	@Query("select u.resourceId from UserResourcePermission u where (u.userId = :userId or u.startupId = :startupId) and u.resource = :resource")
	Set<Long> getStartupFileFolderResources(Long startupId, Long userId, String resource);

	@Query("select u.resourceId from UserResourcePermission u where u.intakeProgramId = :intakeId and u.resource = :resource")
	Set<Long> getIntakeFileFolderResources(Long intakeId, String resource);

	Set<Long> findByResourceIdAndResourceAndIntakeProgramId(Long id, String mff, Long id2);

	UserResourcePermission findTopByUserIdAndResourceIdAndResource(Long id, Long userId, String mff);

	@Query("select u.intakeProgramId from UserResourcePermission u where u.resourceId = :resourceId and u.resource = :resource and u.intakeProgramId is not null")
	Set<Long> getIntakeIdsFileFolderResources(Long resourceId, String resource);

	@Modifying
	@Query(value = "DELETE FROM UserResourcePermission u WHERE u.resourceId = :resourceId and u.resource = :resource and u.intakeProgramId is not null")
	void removeFileFolderWithIntake(Long resourceId, String resource);
}
