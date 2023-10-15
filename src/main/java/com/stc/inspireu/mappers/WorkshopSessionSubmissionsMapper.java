package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.WorkshopSessionSubmissionManagementDto;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.Startup;
import com.stc.inspireu.models.User;
import com.stc.inspireu.models.WorkshopSessionSubmissions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {Startup.class, User.class, IntakeProgram.class})
public interface WorkshopSessionSubmissionsMapper {

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "submittedOn", target = "submittedOn", qualifiedByName = "convertDateToLong")
    @Mapping(source = "fileType", target = "submittedfileType")
    @Mapping(source = "startup.startupName", target = "startUpName")
    @Mapping(source = "startup.id", target = "startupId")
    @Mapping(source = "startup.profileInfoJson", target = "startupProfileInfoJson")
    @Mapping(source = "startup.intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "submittedUser.alias", target = "submittedBy")
    WorkshopSessionSubmissionManagementDto toWorkshopSessionSubmissionManagementDto(WorkshopSessionSubmissions workshopSS);

    List<WorkshopSessionSubmissionManagementDto> toWorkshopSessionSubmissionManagementDtoList
        (Iterable<WorkshopSessionSubmissions> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
