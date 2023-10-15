package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetRegistrationFormDto;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.RegistrationForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, IntakeProgram.class})
public interface RegistrationFormMapper {

    @Mapping(source = "id", target = "formId")
    @Mapping(source = "createdOn", target = "createdDate")
    @Mapping(source = "publishedAt", target = "publishedDate")
    @Mapping(target = "periodEnd",
        expression = "java(form.getDueDate() != null ? String.valueOf(form.getDueDate().toInstant().toEpochMilli()) : null)")
    @Mapping(source = "publishedUser.alias", target = "publishedUser")
    @Mapping(source = "createdUser.alias", target = "createdUser")
    @Mapping(source = "intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "intakeProgram.programName", target = "intakeProgramName")
    GetRegistrationFormDto toGetRegistrationFormDto(RegistrationForm form);
}
