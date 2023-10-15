package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetOpenEventDto;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.OpenEvent;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {IntakeProgram.class, User.class})
public interface OpenEventMapper {

    @Mapping(source = "descriptionSection", target = "description")
    @Mapping(source = "intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "createdUser.id", target = "createdUserId")
    @Mapping(source = "createdUser.alias", target = "createdUserName")
    @Mapping(target = "sessionStart", expression = "java( e.getSessionStart() !=null? e.getSessionStart() " +
        ".toInstant().toEpochMilli():null)")
    @Mapping(target = "sessionEnd", expression = "java( e.getSessionEnd() !=null? e.getSessionEnd() " +
        ".toInstant().toEpochMilli():null)")
    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    GetOpenEventDto toGetOpenEventDto(OpenEvent e);

    List<GetOpenEventDto> toGetOpenEventDtoList(Iterable<OpenEvent> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
