package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetIntakeProgramDto;
import com.stc.inspireu.models.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {AssessmentEvaluationForm.class, BootcampEvaluationForm.class,
    RegistrationForm.class, ProfileCard.class})
public interface IntakeProgramMapper {

    @Mapping(target = "periodStart", expression = "java( e.getPeriodStart() !=null? e.getPeriodStart()" +
        " .toInstant().toEpochMilli():null)")
    @Mapping(target = "periodEnd", expression = "java( e.getPeriodEnd() !=null? e.getPeriodEnd() " +
        ".toInstant().toEpochMilli():null)")

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")

    @Mapping(source = "assessmentEvaluationForm.id", target = "assessmentEvaluationFormId")
    @Mapping(source = "assessmentEvaluationForm.formName", target = "assessmentEvaluationFormName")
    @Mapping(source = "assessmentEvaluationForm.jsonForm", target = "assessmentEvaluationFormJsonForm")

    @Mapping(source = "bootcampEvaluationForm.id", target = "bootcampEvaluationFormId")
    @Mapping(source = "bootcampEvaluationForm.formName", target = "bootcampEvaluationFormName")
    @Mapping(source = "bootcampEvaluationForm.jsonForm", target = "bootcampEvaluationFormJsonForm")

    @Mapping(source = "registrationForm.id", target = "registrationFormId")
    @Mapping(source = "registrationForm.formName", target = "registrationFormName")
    @Mapping(source = "registrationForm.jsonForm", target = "registrationFormJsonForm")

    @Mapping(source = "profileCard.id", target = "profileCardFormId")
    @Mapping(source = "profileCard.name", target = "profileCardFormName")
    @Mapping(source = "profileCard.jsonForm", target = "profileCardJsonForm")
    GetIntakeProgramDto toGetIntakeProgramDto(IntakeProgram e);

    List<GetIntakeProgramDto> toGetIntakeProgramDtoList(Iterable<IntakeProgram> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
