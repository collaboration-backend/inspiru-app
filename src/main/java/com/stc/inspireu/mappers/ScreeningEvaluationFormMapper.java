package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.ScreeningEvaluationFormDTO;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.ScreeningEvaluationForm;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {User.class, IntakeProgram.class})
public interface ScreeningEvaluationFormMapper {

    @Mapping(source = "id", target = "formId")
    @Mapping(source = "createdUser.alias", target = "createdUser")
    @Mapping(source = "publishedUser.alias", target = "publishedUser")
    @Mapping(source = "intakeProgram.programName", target = "intakeProgram")
    @Mapping(source = "createdOn", target = "createdAt")
    ScreeningEvaluationFormDTO toScreeningEvaluationFormDTO(ScreeningEvaluationForm form);

}
