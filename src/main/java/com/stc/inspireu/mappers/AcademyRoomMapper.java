package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.AcademyRoomDto;
import com.stc.inspireu.dtos.AcademyRoomManagementDto;
import com.stc.inspireu.models.AcademyRoom;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {User.class})
public interface AcademyRoomMapper {

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "createdUser.alias", target = "createdUserName")
    AcademyRoomDto toAcademyRoomDTO(AcademyRoom academyRoom);

    List<AcademyRoomDto> toAcademyRoomDTOList(Iterable<AcademyRoom> list);

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "createdUser.alias", target = "createdBy")
    @Mapping(source = "statusPublish", target = "published")
    AcademyRoomManagementDto toAcademyRoomManagementDto(AcademyRoom academyRoom);

    List<AcademyRoomManagementDto> toAcademyRoomManagementDtoList(Iterable<AcademyRoom> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}


