package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.One2OneMeetingDto;
import com.stc.inspireu.models.OneToOneMeeting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {})
public interface OneToOneMeetingMapper {

    @Mapping(source = "sessionStart", target = "sessionStart", qualifiedByName = "convertDateToLong")
    @Mapping(source = "sessionEnd", target = "sessionEnd", qualifiedByName = "convertDateToLong")
    One2OneMeetingDto toOne2OneMeetingDto(OneToOneMeeting oneToOneMeeting);

    List<One2OneMeetingDto> toOne2OneMeetingDtoList(Iterable<OneToOneMeeting> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
