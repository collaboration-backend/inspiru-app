package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetAssignmentFileDto;
import com.stc.inspireu.models.AssignmentFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {})
public interface AssignmentFileMapper {


    GetAssignmentFileDto toGetAssignmentFileDto(AssignmentFile assignmentFile);

    List<GetAssignmentFileDto> toGetAssignmentFileDtoList(List<AssignmentFile> list);

}
