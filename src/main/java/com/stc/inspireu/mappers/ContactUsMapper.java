package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.ContactUsSubmissionResultDTO;
import com.stc.inspireu.models.ContactUs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.ocpsoft.prettytime.PrettyTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Mapper(componentModel = "spring")
public interface ContactUsMapper {

    @Mapping(source = "createdOn", target = "createdOn", qualifiedByName = "convertDateToString")
    ContactUsSubmissionResultDTO toContactUsSubmissionResultDTO(ContactUs contactUs);

    @Named("convertDateToString")
    default String convertDateToString(LocalDateTime date) {
        if (date != null) {
            return new PrettyTime().format(new Date(ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli()));
        } else {
            return null;
        }
    }
}
