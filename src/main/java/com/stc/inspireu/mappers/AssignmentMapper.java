package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.AssignmentManagementDto;
import com.stc.inspireu.dtos.GetAssignmentDto;
import com.stc.inspireu.dtos.GetAssignmentFileDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.models.Assignment;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.Startup;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Mapper(componentModel = "spring", uses = {User.class, Startup.class, IntakeProgram.class})
public interface AssignmentMapper {


    @Mapping(source = "dueDate", target = "dueDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "reviwed1On", target = "reviwed1On", qualifiedByName = "convertDateToLong")
    @Mapping(source = "reviwed2On", target = "reviwed2On", qualifiedByName = "convertDateToLong")
    @Mapping(source = "submitDate", target = "submitDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "createdOn", target = "createDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "createdUser.alias", target = "createdBy")
    @Mapping(source = "submittedStartup.id", target = "startupId")
    @Mapping(source = "submittedStartup.startupName", target = "startupName")
    @Mapping(source = "submittedUser.alias", target = "submittedBy")
    @Mapping(source = "submittedStartup.profileInfoJson", target = "startupProfileInfoJson")
    @Mapping(source = "submittedStartup.intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "review1User.alias", target = "review1By")
    @Mapping(source = "review1User.id", target = "review1Id")
    @Mapping(source = "review2User.alias", target = "review2By")
    @Mapping(source = "review2User.id", target = "review2Id")
    AssignmentManagementDto toAssignmentManagementDto(Assignment assignment);

    List<AssignmentManagementDto> toAssignmentManagementDtoList(Iterable<Assignment> list);

    @Mapping(source = "assignment.dueDate", target = "dueDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "assignment.reviwed1On", target = "reviwed1On", qualifiedByName = "convertDateToLong")
    @Mapping(source = "assignment.reviwed2On", target = "reviwed2On", qualifiedByName = "convertDateToLong")
    @Mapping(source = "assignment.submitDate", target = "submitDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "assignment.createdOn", target = "createDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "assignment.createdUser.alias", target = "createdBy")
    @Mapping(source = "assignment.submittedStartup.id", target = "startupId")
    @Mapping(source = "assignment.submittedStartup.startupName", target = "startupName")
    @Mapping(source = "assignment.submittedUser.alias", target = "submittedBy")
    @Mapping(source = "assignment.submittedStartup.profileInfoJson", target = "startupProfileInfoJson")
    @Mapping(source = "assignment.submittedStartup.intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "assignment.review1User.alias", target = "review1By")
    @Mapping(source = "assignment.review1User.id", target = "review1Id")
    @Mapping(source = "assignment.review2User.alias", target = "review2By")
    @Mapping(source = "assignment.review2User.id", target = "review2Id")
    @Mapping(source = "files", target = "assignmentFiles")
    AssignmentManagementDto assignmentAndFileToAssignmentManagementDto(Assignment assignment, List<GetAssignmentFileDto> files);

    @Mapping(source = "assignment.dueDate", target = "dueDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "assignment.reviwed1On", target = "reviwed1On", qualifiedByName = "convertDateToLong")
    @Mapping(source = "assignment.reviwed2On", target = "reviwed2On", qualifiedByName = "convertDateToLong")
    @Mapping(source = "assignment.submitDate", target = "submitDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "assignment.createdOn", target = "createDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "assignment.createdUser.alias", target = "createdBy")
    @Mapping(source = "assignment.submittedStartup.id", target = "startupId")
    @Mapping(source = "assignment.submittedStartup.startupName", target = "startupName")
    @Mapping(source = "assignment.submittedUser.alias", target = "submittedBy")
    @Mapping(source = "assignment.submittedStartup.profileInfoJson", target = "startupProfileInfoJson")
    @Mapping(source = "assignment.submittedStartup.intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "assignment.review1User.alias", target = "review1By")
    @Mapping(source = "assignment.review1User.id", target = "review1Id")
    @Mapping(source = "assignment.review2User.alias", target = "review2By")
    @Mapping(source = "assignment.review2User.id", target = "review2Id")
    @Mapping(source = "files", target = "assignmentFiles")
    @Mapping(source = "submitCount", target = "noOfSubmissions")
    AssignmentManagementDto toAssignmentManagementDtoWithSubmitCount(Assignment assignment, Long submitCount,
                                                                     List<GetAssignmentFileDto> files);


    @Mapping(source = "createdUser.alias", target = "createdUser")
    @Mapping(source = "dueDate", target = "dueDate", qualifiedByName = "convertDateToLong")
    @Mapping(source = "submitDate", target = "submittedOn", qualifiedByName = "convertDateToLong")
    @Mapping(source = "review1Status", target = "reviwed1Status")
    @Mapping(source = "review2Status", target = "reviwed2Status")
    @Mapping(target = "reviwed1On", expression = "java(isStatusNotPending(e.getReviwed1On(), e.getReview1Status()))")
    @Mapping(target = "reviwed2On", expression = "java(isStatusNotPending(e.getReviwed2On(), e.getReview2Status()))")
    GetAssignmentDto toGetAssignmentDto(Assignment e);

    List<GetAssignmentDto> toGetAssignmentDtoList(Iterable<Assignment> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }

    @Named("convertDateToLong")
    default Long convertDateToLong(Date date) {
        return date.getTime();
    }

    @Named("isStatusNotPending")
    default Long isStatusNotPending(Date date, String status) {
        return Constant.PENDING.toString().equals(status) ? null : convertDateToLong(date);
    }
}
