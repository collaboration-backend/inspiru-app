package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.FileDto;
import com.stc.inspireu.models.File;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {User.class, IntakeProgram.class})
public interface FileMapper {

    @Mapping(source = "createdUser.id", target = "userId")
    @Mapping(source = "createdUser.alias", target = "userName")
    @Mapping(source = "intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "intakeProgram.programName", target = "inTakePgmName")
    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    FileDto toFileDto(File file);

    List<FileDto> toFileDtoList(Iterable<File> list);

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
