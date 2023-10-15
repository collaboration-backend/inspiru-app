package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetMarkCard2022Dto;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.MarkCard2022;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {IntakeProgram.class})
public interface MarkCard2022Mapper {

    @Mapping(source = "intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "id", target = "markCardId")
    @Mapping(source = "createdOn", target = "createdAt")
    @Mapping(source = "intakeProgram.programName", target = "intakeProgramName")
    @Mapping(source = "intakeProgram.periodStart", target = "startDate")
    @Mapping(source = "intakeProgram.periodEnd", target = "endDate")
    GetMarkCard2022Dto toGetMarkCard2022Dto(MarkCard2022 markCard2022);
}
