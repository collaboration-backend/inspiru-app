package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetOpenEventSlotDto;
import com.stc.inspireu.models.OpenEvent;
import com.stc.inspireu.models.OpenEventSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {OpenEvent.class})
public interface OpenEventSlotMapper {

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(target = "day", expression = "java( e.getDay() !=null? e.getDay() .toInstant().toEpochMilli():null)")
    @Mapping(source = "openEvent.id", target = "openEventId")
    GetOpenEventSlotDto toGetOpenEventSlotDto(OpenEventSlot e);

    List<GetOpenEventSlotDto> toGetOpenEventSlotDtoList(Iterable<OpenEventSlot> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }

}
