package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.DueDiligenceNote2021Dto;
import com.stc.inspireu.models.DueDiligenceNote2021;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {User.class})
public interface DueDiligenceNote2021Mapper {

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(target = "userName", expression = "java(ddn.getManagementUser() == null ? ddn.getStartupUser().getAlias() " +
        ": ddn.getManagementUser().getAlias())")
    @Mapping(target = "note", expression = "java(ddn.getManagementUser() == null ? ddn.getNote() : ddn.getReplyNote())")
    @Mapping(target = "management", expression = "java(ddn.getManagementUser() == null ? false : true)")
    @Mapping(target = "userId", expression = "java(ddn.getManagementUser() == null ? ddn.getStartupUser().getId()" +
        " : ddn.getManagementUser().getId())")
    DueDiligenceNote2021Dto toDueDiligenceNote2021Dto(DueDiligenceNote2021 ddn);

    List<DueDiligenceNote2021Dto> toDueDiligenceNote2021DtoList(Iterable<DueDiligenceNote2021> list);


    @Named("convertDateToLong")
    default long convertDateToLong(LocalDateTime date) {
        long l = 0L;
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return l;
        }
    }
}

