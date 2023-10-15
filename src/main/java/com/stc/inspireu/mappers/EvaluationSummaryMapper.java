package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.EvaluationSummaryDto;
import com.stc.inspireu.models.EvaluationSummary;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.IntakeProgramSubmission;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {IntakeProgram.class, User.class, IntakeProgramSubmission.class})
public interface EvaluationSummaryMapper {

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "intakeProgram.programName", target = "intakeProgramName")
    @Mapping(source = "submittedUser.id", target = "submittedUserId")
    @Mapping(source = "submittedUser.alias", target = "submittedUserName")
    @Mapping(source = "intakeProgramSubmission.id", target = "applicationId")
    EvaluationSummaryDto toEvaluationSummaryDto(EvaluationSummary e);

    List<EvaluationSummaryDto> toEvaluationSummaryDtoList(Iterable<EvaluationSummary> ls);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
