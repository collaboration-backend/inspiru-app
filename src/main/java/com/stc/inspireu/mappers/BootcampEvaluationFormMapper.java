package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetBootCampFormsDto;
import com.stc.inspireu.models.BootcampEvaluationForm;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {User.class, IntakeProgram.class})
public interface BootcampEvaluationFormMapper {

    @Mapping(source = "createdOn", target = "createdAt")
    @Mapping(source = "createdUser.alias", target = "createdUser")
    @Mapping(source = "publishedUser.alias", target = "publishedUser")
    @Mapping(source = "intakeProgram.programName", target = "intakeProgram")
    GetBootCampFormsDto toGetBootCampFormsDto(BootcampEvaluationForm form);
}
