package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetWorkshopSessionDto;
import com.stc.inspireu.dtos.WorkshopSessionManagementDto;
import com.stc.inspireu.models.AcademyRoom;
import com.stc.inspireu.models.User;
import com.stc.inspireu.models.WorkshopSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {User.class, AcademyRoom.class})
public interface WorkshopSessionMapper {

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "sessionStart", target = "sessionStart", qualifiedByName = "convertDateToLong")
    @Mapping(source = "sessionEnd", target = "sessionEnd", qualifiedByName = "convertDateToLong")
    @Mapping(source = "createdUser.alias", target = "createdBy")
    @Mapping(source = "statusPublish", target = "published")
    WorkshopSessionManagementDto toWorkshopSessionManagementDto(WorkshopSession workshopSession);

    List<WorkshopSessionManagementDto> toWorkshopSessionManagementDtoList(Iterable<WorkshopSession> list);

    @Mapping(source = "academyRoom.id", target = "academyRoomId")
    @Mapping(source = "academyRoom.id", target = "refAcademyRoomId")
    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "sessionStart", target = "sessionStart", qualifiedByName = "convertDateToLong")
    @Mapping(source = "sessionEnd", target = "sessionEnd", qualifiedByName = "convertDateToLong")
    GetWorkshopSessionDto toGetWorkshopSessionDto(WorkshopSession workshopSession);

    List<GetWorkshopSessionDto> toGetWorkshopSessionDtoList(Iterable<WorkshopSession> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
