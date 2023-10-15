package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.SurveyManagementDto;
import com.stc.inspireu.models.Startup;
import com.stc.inspireu.models.Survey;
import com.stc.inspireu.models.User;
import com.stc.inspireu.models.WorkshopSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {User.class, Startup.class, WorkshopSession.class})
public interface SurveyMapper {

    @Mapping(source = "createdOn", target = "createdOn", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "modifiedOn", qualifiedByName = "convertDateToLong")
    @Mapping(source = "createdUser.alias", target = "createdBy")
    @Mapping(source = "submittedStartup.startupName", target = "startupName")
    @Mapping(source = "submittedStartup.id", target = "startupId")
    @Mapping(source = "workshopSession.name", target = "workshopSessionName")
    @Mapping(source = "submittedUser.alias", target = "submittedBy")
    @Mapping(source = "submittedStartup.profileInfoJson", target = "startupProfileInfoJson")
    @Mapping(source = "submittedStartup.intakeProgram.id", target = "intakeProgramId")
    @Mapping(target = "dueDate", expression = "java( survey.getDueDate() !=null? survey.getDueDate() " +
        ".toInstant().toEpochMilli():null)")
    @Mapping(target = "submitDate", expression = "java( survey.getSubmittedOn() !=null? survey.getSubmittedOn() " +
        ".toInstant().toEpochMilli():null)")
    SurveyManagementDto toSurveyManagementDto(Survey survey);

    List<SurveyManagementDto> toSurveyManagementDtoList(Iterable<Survey> list);

    @Mapping(source = "survey.createdOn", target = "createdOn", qualifiedByName = "convertDateToLong")
    @Mapping(source = "survey.modifiedOn", target = "modifiedOn", qualifiedByName = "convertDateToLong")
    @Mapping(source = "survey.createdUser.alias", target = "createdBy")
    @Mapping(source = "survey.submittedStartup.startupName", target = "startupName")
    @Mapping(source = "survey.submittedStartup.id", target = "startupId")
    @Mapping(source = "survey.workshopSession.name", target = "workshopSessionName")
    @Mapping(source = "survey.submittedUser.alias", target = "submittedBy")
    @Mapping(expression = "java(survey.getSubmittedStartup() != null ? survey.getSubmittedStartup().getProfileInfoJson()" +
        " : \"\")", target = "startupProfileInfoJson")
    @Mapping(source = "survey.submittedStartup.intakeProgram.id", target = "intakeProgramId")
    @Mapping(target = "dueDate", expression = "java( survey.getDueDate() !=null? survey.getDueDate() " +
        ".toInstant().toEpochMilli():null)")
    @Mapping(target = "submitDate", expression = "java( survey.getSubmittedOn() !=null? survey.getSubmittedOn() " +
        ".toInstant().toEpochMilli():null)")
    @Mapping(source = "submitCount", target = "noOfSubmissions")
    SurveyManagementDto toSurveyManagementDtoWithSubmitCount(Survey survey, Long submitCount);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
