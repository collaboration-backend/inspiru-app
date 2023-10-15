package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.RoleDto;
import com.stc.inspireu.models.Role;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {})
public interface RoleMapper {

    RoleDto toRoleDTO(Role role);

    List<RoleDto> toRoleDTOList(Iterable<Role> list);
}
