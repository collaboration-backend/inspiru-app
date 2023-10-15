package com.stc.inspireu.services;

import com.stc.inspireu.models.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    Page<Role> findAllRoles(Pageable pageable);

    Optional<Role> findRoleById(Long roleId);

    List<Role> findRolesExcludedGivenList(List<String> excludedRoles);

}
