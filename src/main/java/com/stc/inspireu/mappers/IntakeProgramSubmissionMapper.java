package com.stc.inspireu.mappers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.GetIntakeProgramSubmissionDto;
import com.stc.inspireu.dtos.GetIntakeProgramSubmissionForJudgeAppDto;
import com.stc.inspireu.models.EvaluationSummary;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.IntakeProgramSubmission;
import com.stc.inspireu.models.ScreeningEvaluationForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {IntakeProgram.class, EvaluationSummary.class, ScreeningEvaluationForm.class})
public interface IntakeProgramSubmissionMapper {


    GetIntakeProgramSubmissionForJudgeAppDto toGetIntakeProgramSubmissionForJudgeAppDto(IntakeProgramSubmission e);

    List<GetIntakeProgramSubmissionForJudgeAppDto> toGetIntakeProgramSubmissionForJudgeAppDtoList(
        Iterable<IntakeProgramSubmission> list);

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "intakeProgram.programName", target = "intakeProgramName")
    @Mapping(source = "intakeProgram.screeningEvaluationForm.jsonForm", target = "jsonScreeningEvaluationForm")

    @Mapping(target = "interviewStart", expression = "java(e.getInterviewStart()!=null?e.getInterviewStart().toInstant().toEpochMilli():null)")
    @Mapping(target = "interviewEnd", expression = "java(e.getInterviewEnd()!=null?e.getInterviewEnd().toInstant().toEpochMilli():null)")
    @Mapping(target = "interviewStartBootcamp", expression = "java(e.getInterviewStartBootcamp()!=null?e.getInterviewStartBootcamp().toInstant().toEpochMilli():null)")
    @Mapping(target = "interviewEndBootcamp", expression = "java(e.getInterviewEndBootcamp()!=null?e.getInterviewEndBootcamp().toInstant().toEpochMilli():null)")

    @Mapping(target = "jsonRegistrationForm", expression = "java(transformJsonRegistrationForm(e))")
    @Mapping(target = "profileInfoJson", expression = "java(transformProfileInfoJson(e.getProfileInfoJson()))")
    GetIntakeProgramSubmissionDto toGetIntakeProgramSubmissionDto(IntakeProgramSubmission e);

    List<GetIntakeProgramSubmissionDto> toGetIntakeProgramSubmissionDtoList(Iterable<IntakeProgramSubmission> list);

    @Mapping(source = "e.createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "e.email", target = "email")
    @Mapping(source = "e.intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "e.intakeProgram.programName", target = "intakeProgramName")
    @Mapping(source = "e.intakeProgram.screeningEvaluationForm.jsonForm", target = "jsonScreeningEvaluationForm")

    @Mapping(target = "interviewStart", expression = "java(e.getInterviewStart()!=null?e.getInterviewStart().toInstant().toEpochMilli():null)")
    @Mapping(target = "interviewEnd", expression = "java(e.getInterviewEnd()!=null?e.getInterviewEnd().toInstant().toEpochMilli():null)")
    @Mapping(target = "interviewStartBootcamp", expression = "java(e.getInterviewStartBootcamp()!=null?e.getInterviewStartBootcamp().toInstant().toEpochMilli():null)")
    @Mapping(target = "interviewEndBootcamp", expression = "java(e.getInterviewEndBootcamp()!=null?e.getInterviewEndBootcamp().toInstant().toEpochMilli():null)")

    @Mapping(target = "jsonRegistrationForm", expression = "java(transformJsonRegistrationForm(e))")
    @Mapping(target = "profileInfoJson", expression = "java(transformProfileInfoJson(e.getProfileInfoJson()))")

    @Mapping(target = "hasStartedScreeningEvaluation", expression = "java(checkScreeningEvaluationStart(e))")
    @Mapping(target = "hasStartedScreeningEvaluationByMe", expression = "java(checkScreeningEvaluationByCurrentUser" +
        "(e, currentUserObject))")
    GetIntakeProgramSubmissionDto toGetIntakeProgramSubmissionDtoWithCurrentUserObject(IntakeProgramSubmission e,
                                                                                       CurrentUserObject currentUserObject);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }

    default String transformJsonRegistrationForm(IntakeProgramSubmission e) {
        if (e.getJsonRegistrationForm() != null && !e.getJsonRegistrationForm().isEmpty()) {
            return e.getJsonRegistrationForm().replace("custom-mobile", "custom-text")
                .replace("\"_type\":\"number\"", "\"_type\":\"text\"");
        }
        return e.getJsonRegistrationForm();
    }

    default String transformProfileInfoJson(String profileInfoJson) {
        if (profileInfoJson != null && !profileInfoJson.isEmpty()) {
            return profileInfoJson.replace("custom-mobile", "custom-text")
                .replace("\"_type\":\"number\"", "\"_type\":\"text\"");
        }
        return profileInfoJson;
    }

    default boolean checkScreeningEvaluationStart(IntakeProgramSubmission entity) {
        return entity != null && !entity.getScreeningEvaluators().isEmpty();
    }

    default boolean checkScreeningEvaluationByCurrentUser(IntakeProgramSubmission entity, CurrentUserObject currentUserObject) {
        if (entity != null && currentUserObject != null) {
            return entity.getScreeningEvaluators().stream()
                .anyMatch(u -> u.getId().equals(currentUserObject.getUserId()));
        }
        return false;
    }
}
