package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetEmailTemplatesTypesDto;
import com.stc.inspireu.models.EmailTemplatesTypes;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mapper(componentModel = "spring", uses = {User.class})
public interface EmailTemplatesTypesMapper {

    @Mapping(source = "id", target = "emailTemplateId")
    @Mapping(source = "createdUser.alias", target = "createdUser")
    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    GetEmailTemplatesTypesDto toGetEmailTemplatesTypesDto(EmailTemplatesTypes e);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
