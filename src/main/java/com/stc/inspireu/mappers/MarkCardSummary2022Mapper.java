package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetMarkCardStartupsDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.models.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mapper(componentModel = "spring", uses = {User.class, Startup.class, AcademyRoom.class, MarkCard2022.class})
public interface MarkCardSummary2022Mapper {

    @Mapping(source = "startup.id", target = "startupId")
    @Mapping(source = "startup.startupName", target = "startupName")
    @Mapping(source = "startup", target = "status", qualifiedByName = "mapStatus")

    @Mapping(source = "id", target = "markCardId")
    @Mapping(source = "jsonMarkCard", target = "markCardJson")
    @Mapping(source = "amountPaid", target = "amount")

    @Mapping(source = "refMarkCardSummary.id", target = "markCardRefId")
    @Mapping(source = "updatedUser.alias", target = "updatedBy")

    @Mapping(target = "isMarkCardGenerated", source = "isMarkCardGenerated", qualifiedByName = "mapIsMarkCardGenerated")

    @Mapping(source = "academyRoom.id", target = "academicRoomId")
    @Mapping(source = "academyRoom.name", target = "academicRoomName")
    @Mapping(source = "academyRoom.sessionEnd", target = "academicRoomEndDate")

    @Mapping(source = "modifiedOn", target = "updateDate", qualifiedByName = "convertDateToLong")
    GetMarkCardStartupsDto toGetMarkCardStartupsDto(MarkCardSummary2022 markCardSummary);


    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }

    @Named("mapStatus")
    default String mapStatus(Startup startup) {
        return startup != null ? Constant.PAID.toString() : Constant.PENDING.toString();
    }

    @Named("mapIsMarkCardGenerated")
    default boolean mapIsMarkCardGenerated(Boolean isMarkCardGenerated) {
        return isMarkCardGenerated != null && isMarkCardGenerated;
    }
}
