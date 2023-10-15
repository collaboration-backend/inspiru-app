package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.DueDiligenceTemplate2021Dto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.models.DueDiligenceTemplate2021;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.Startup;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {User.class, IntakeProgram.class, Startup.class})
public interface DueDiligenceTemplate2021Mapper {
    @Mapping(source = "intakeProgram.programName", target = "intakeProgram")
    @Mapping(source = "intakeProgram.periodStart", target = "startedOn", qualifiedByName = "convertDateToLong")
    @Mapping(source = "intakeProgram.periodEnd", target = "endBy", qualifiedByName = "convertDateToLong")
    @Mapping(source = "intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "createdUser.alias", target = "createdBy")
    @Mapping(source = "startup.startupName", target = "startupName")
    @Mapping(source = "startup.id", target = "startupId")
    @Mapping(source = "createdUser.invitationStatus", target = "invitationStatus")
    @Mapping(source = "createdUser.id", target = "userId")

    @Mapping(source = "dd", target = "submittedBy", qualifiedByName = "mapSubmittedBy")
    @Mapping(source = "dd", target = "publishedBy", qualifiedByName = "mapPublishedBy")
    @Mapping(source = "dd", target = "reviewedBy", qualifiedByName = "mapReviewedBy")

    @Mapping(target = "submittedOn", expression = "java( dd.getSubmittedOn() !=null? dd.getSubmittedOn() " +
        ".toInstant().toEpochMilli():0L)" )
    @Mapping(target = "reviewedOn", expression = "java( dd.getReviewedOn() !=null? dd.getReviewedOn() " +
        ".toInstant().toEpochMilli():0L)" )
    DueDiligenceTemplate2021Dto toDueDiligenceTemplate2021DTO(DueDiligenceTemplate2021 dd);

    List<DueDiligenceTemplate2021Dto> toDueDiligenceTemplate2021DTOList(Iterable<DueDiligenceTemplate2021> list);

    @Named("convertDateToLong")
    default long convertDateToLong(LocalDateTime date) {
        long l = 0L;
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return l;
        }
    }

    @Named("mapSubmittedBy")
    default String mapSubmittedBy(DueDiligenceTemplate2021 dd) {
        if (Constant.SUBMITTED.toString().equals(dd.getStatus()) && dd.getSubmittedUser() != null) {
            return dd.getSubmittedUser().getAlias();
        }
        return null;
    }

    @Named("mapPublishedBy")
    default String mapPublishedBy(DueDiligenceTemplate2021 dd) {
        if (Constant.PUBLISHED.toString().equals(dd.getStatus()) && dd.getSubmittedUser() != null) {
            return dd.getSubmittedUser().getAlias();
        }
        return null;
    }

    @Named("mapReviewedBy")
    default String mapReviewedBy(DueDiligenceTemplate2021 dd) {
        if ((Constant.APPROVED.toString().equals(dd.getStatus())
            || Constant.RESUBMIT.toString().equals(dd.getStatus())) && dd.getReviewUser() != null) {
            return dd.getReviewUser().getAlias();
        }
        return null;
    }
}
