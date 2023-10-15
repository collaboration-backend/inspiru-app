package com.stc.inspireu.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.Role;

@Transactional
public interface RoleRepository extends PagingAndSortingRepository<Role, Long> {

	Role findByRoleName(String string);

	@Query("select t from Role t where t.willManagement = :b")
	List<Role> findAllStartupRoles(@Param("b") Boolean b);

	List<Role> findByRoleNameNotIn(List<String> roleNames);

}
