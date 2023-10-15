package com.stc.inspireu.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.dtos.GetUserDto;
import com.stc.inspireu.models.User;

@Transactional
public interface UserRepository extends PagingAndSortingRepository<User, Long>, UserRepositoryCustom {

    User findByEmail(String email);

    Optional<User> findByIdAndWillManagementTrue(Long id);

    Page<User> findByIdNotInAndRole_RoleNameInAndAliasNotNull(Set<Long> userIds, List<String> roles, Pageable pageable);

    Page<User> findByIdNotInAndRole_RoleNameInAndAliasContainingIgnoreCase(Set<Long> userIds, List<String> roles, String searchKey, Pageable pageable);

    Page<User> findByIdNotInAndRole_RoleNameNotInAndAliasNotNull(Set<Long> userIds, List<String> role, Pageable pageable);

    Page<User> findByIdNotInAndRole_RoleNameNotInAndAliasContainingIgnoreCase(Set<Long> userIds, List<String> role, String searchKey, Pageable pageable);

    User findByEmailAndInvitationStatus(String registratedEmailAddress, String string);

    @Query("select u from User u where u.startup.id = :startupId")
    List<User> getMembers(@Param("startupId") Long startupId);

    @Query("select u from User u where u.startup.id = :startupId and u.id = :memberId")
    User getMember(@Param("startupId") Long startupId, @Param("memberId") Long memberId);

    User findByEmailAndPassword(String email, String hashed);

    List<User> findByRole_RoleNameAndStartupIdIsNull(String string);

    @Query("select new com.stc.inspireu.dtos.GetUserDto(e.id, e.email, e.alias, e.invitationStatus, e.phoneNumber, e.phoneDialCode, e.phoneCountryCodeIso2, e.createdOn, e.startup) from User e where 1=1")
    Page<GetUserDto> getAllUsers(Pageable paging);

    @Query("select u from User u where u.id = :userId")
    User getUserRole(@Param("userId") Long userId);

    List<User> findByStartupId(Long startupId);

    @Query("select u from User u WHERE u.role.roleName IN (:roles) and u.invitationStatus = :status")
    List<User> findByUserRoles(List<String> roles, String status);

    Set<User> findByIdIn(Set<Long> assigneeIds);

    Page<User> findByRole_RoleNameIn(List<String> rln, Pageable paging);

    List<User> findByRole_RoleNameAndStartupId(String roleName, Long startupId);

    Page<User> findByRole_RoleNameInAndInvitationStatus(List<String> rln, Pageable paging, String string);

    Page<User> findByAliasContainingIgnoreCaseAndRole_RoleNameInAndInvitationStatus(String alias, List<String> rln,
                                                                                    Pageable paging, String string);

    Page<User> findByRole_RoleName(String string, Pageable paging);

    List<User> findByStartup_Id(Long startupId, Pageable pageable);

    Page<User> findByIdInAndRole_RoleName(Set<Long> usrList2, String string, Pageable paging);

    @Query("SELECT COUNT(u) FROM User u WHERE u.startup.id IN (:startupIds) and u.invitationStatus = :status")
    long getAllUserCount(List<Long> startupIds, String status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.startup.id = :startupId and u.invitationStatus = :status")
    long getUserCount(Long startupId, String status);


    @Query("select u from User u where u.startup.id = :startupId and u.invitationStatus = :status")
    List<User> getRegistredMembers(Long startupId, String status);

}
