package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetUserDto;
import com.stc.inspireu.dtos.UserDto;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.Role;
import com.stc.inspireu.models.Startup;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {Startup.class, Role.class, IntakeProgram.class})
public interface UserMapper {

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "startup.id", target = "startupId")
    @Mapping(source = "role.id", target = "roleId")
    @Mapping(source = "role.roleAlias", target = "roleName")
    UserDto toUserDTO(User user);

    List<UserDto> toUserDTOList(Iterable<User> list);

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToWrapperLong")
    @Mapping(source = "role.id", target = "roleId")
    @Mapping(source = "role.roleAlias", target = "roleAlias")
    @Mapping(source = "role.roleName", target = "roleName")
    @Mapping(source = "startup.id", target = "startupId")
    @Mapping(source = "startup.startupName", target = "startupName")
    @Mapping(source = "startup.intakeProgram.id", target = "intakeId")
    @Mapping(source = "startup.intakeProgram.programName", target = "intakeName")
    GetUserDto toGetUserDto(User e);

    List<GetUserDto> toGetUserDtoList(Iterable<User> list);

    @Named("convertDateToLong")
    default long convertDateToLong(LocalDateTime date) {
        long l = 0L;
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return l;
        }
    }

    @Named("convertDateToWrapperLong")
    default Long convertDateToWrapperLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
