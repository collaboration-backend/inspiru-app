package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.ProgressReportDto;
import com.stc.inspireu.jpa.projections.ProjectProgressReportFile;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.ProgressReport;
import com.stc.inspireu.models.Startup;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.ocpsoft.prettytime.PrettyTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Mapper(componentModel = "spring", uses = {User.class, Startup.class, IntakeProgram.class})
public interface ProgressReportMapper {

    @Mapping(source = "id", target = "progressReportId")
    @Mapping(source = "createdOn", target = "createdDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "submittedDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "startup.id", target = "startupId")
    @Mapping(source = "startup.profileInfoJson", target = "startupProfileInfoJson")
    @Mapping(source = "intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "jsonReportDetail", target = "jsonForm")
    @Mapping(source = "createdUser.alias", target = "createdUserName")
    @Mapping(source = "submittedUser.alias", target = "submittedUserName")
    ProgressReportDto toProgressReportDto(ProgressReport pr);

    List<ProgressReportDto> toProgressReportDtoList(List<ProgressReport> list);

    @Mapping(source = "pr.id", target = "progressReportId")
    @Mapping(source = "pr.createdOn", target = "createdDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "pr.modifiedOn", target = "submittedDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "pr.startup.id", target = "startupId")
    @Mapping(source = "pr.startup.profileInfoJson", target = "startupProfileInfoJson")
    @Mapping(source = "pr.intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "pr.jsonReportDetail", target = "jsonForm")
    @Mapping(source = "pr.createdUser.alias", target = "createdUserName")
    @Mapping(source = "pr.submittedUser.alias", target = "submittedUserName")
    @Mapping(source = "ls", target = "reportFiles")
    ProgressReportDto toProgressReportDtoAlongWithReportFile(ProgressReport pr, List<ProjectProgressReportFile> ls);

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
