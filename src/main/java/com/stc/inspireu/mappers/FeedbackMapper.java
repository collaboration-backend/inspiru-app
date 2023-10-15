package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.FeedbackFormManagementDto;
import com.stc.inspireu.dtos.GetFeedbackDto;
import com.stc.inspireu.models.Feedback;
import com.stc.inspireu.models.Startup;
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
public interface FeedbackMapper {

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "createdUser.alias", target = "createdBy")
    @Mapping(source = "forStartup.startupName", target = "startupName")
    @Mapping(source = "forStartup.startupName", target = "startupId")
    @Mapping(source = "workshopSession.name", target = "workshopSessionName")
    FeedbackFormManagementDto toFeedbackFormManagementDto(Feedback feedback);

    List<FeedbackFormManagementDto> toFeedbackFormManagementDtoList(Iterable<Feedback> list);

    @Mapping(source = "feedback.createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "feedback.modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "feedback.createdUser.alias", target = "createdBy")
    @Mapping(source = "feedback.forStartup.startupName", target = "startupName")
    @Mapping(source = "feedback.forStartup.startupName", target = "startupId")
    @Mapping(source = "feedback.workshopSession.name", target = "workshopSessionName")
    @Mapping(source = "submitCount", target = "noOfSubmissions")
    FeedbackFormManagementDto toFeedbackFormManagementDtoWithSubmitCount(Feedback feedback, Long submitCount);

    @Mapping(source = "createdUser.alias", target = "uploadedBy")
    GetFeedbackDto toGetFeedbackDto(Feedback feedback);

    List<GetFeedbackDto> toGetFeedbackDtoList(Iterable<Feedback> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
