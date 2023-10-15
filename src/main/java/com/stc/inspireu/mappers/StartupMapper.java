package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetStartupDto;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.Startup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {IntakeProgram.class})
public interface StartupMapper {

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "intakeProgram.programName", target = "intakeProgramName")
    GetStartupDto toGetStartupDto(Startup e);

    List<GetStartupDto> toGetStartupDtoList(Iterable<Startup> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
