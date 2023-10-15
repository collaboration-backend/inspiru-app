package com.stc.inspireu.services.impl;

import com.stc.inspireu.models.Role;
import com.stc.inspireu.repositories.RoleRepository;
import com.stc.inspireu.services.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Page<Role> findAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @Override
    public Optional<Role> findRoleById(Long roleId) {
        return roleRepository.findById(roleId);
    }

    @Override
    public List<Role> findRolesExcludedGivenList(List<String> excludedRoles) {
        return roleRepository.findByRoleNameNotIn(excludedRoles);
    }
}
