package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetJudgeCalenderEventsDto;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.JudgeCalendar;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Mapper(componentModel = "spring", uses = {IntakeProgram.class, User.class})
public interface JudgeCalendarMapper {

    @Mapping(source = "e.createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "e.modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(target = "sessionStart", expression = "java( e.getSessionStart() !=null? e.getSessionStart()" +
        " .toInstant().toEpochMilli():null)")
    @Mapping(target = "sessionEnd", expression = "java( e.getSessionEnd() !=null? e.getSessionEnd() " +
        ".toInstant().toEpochMilli():null)")
    @Mapping(target = "eventDate", expression = "java(setEventDate(e.getSessionStart()))")
    @Mapping(source = "e.intakeProgram.id", target = "intakeProgramId")
    @Mapping(source = "e.intakeProgram.programName", target = "intakeProgramName")
    @Mapping(source = "e.judge.id", target = "judgeId")
    @Mapping(source = "e.judge.alias", target = "judgeName")
    GetJudgeCalenderEventsDto toGetJudgeCalenderEventsDto(JudgeCalendar e);

    List<GetJudgeCalenderEventsDto> toGetJudgeCalenderEventsDtoList(Iterable<JudgeCalendar> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }

    @Named("setEventDate")
    default Long setEventDate(Date dateTime) {
        if (dateTime != null) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));
            cal.setTime(Date.from(dateTime.toInstant()));
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime().toInstant().toEpochMilli();
        }
        return null;
    }
}
