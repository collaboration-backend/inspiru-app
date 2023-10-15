package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.CalendarEventDto;
import com.stc.inspireu.models.CalendarEvent;
import com.stc.inspireu.models.Startup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Mapper(componentModel = "spring", uses = {Startup.class})
public interface CalendarEventMapper {

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "sessionStart", target = "sessionStart", qualifiedByName = "convertDateToLong")
    @Mapping(source = "sessionEnd", target = "sessionEnd", qualifiedByName = "convertDateToLong")
    @Mapping(source = "startup.id", target = "startupId")
    @Mapping(source = "startup.startupName", target = "startupName")
    @Mapping(source = "calendarEvent", target = "slot", qualifiedByName = "setSlotData")
    @Mapping(source = "calendarEvent", target = "oneToOneMeeting", qualifiedByName = "setOneToOneMeetingData")
    @Mapping(source = "calendarEvent", target = "trainingSession", qualifiedByName = "setTrainingSession")
    @Mapping(source = "calendarEvent.sessionStart", target = "eventDate", qualifiedByName = "setEventDate")
    CalendarEventDto toCalendarEventDto(CalendarEvent calendarEvent);


    List<CalendarEventDto> toCalendarEventDtoList(List<CalendarEvent> list);

    @Named("setSlotData")
    default Map<String, Object> setSlotData(CalendarEvent calendarEvent) {
        Map<String, Object> data = null;
        if (calendarEvent.getSlot() != null) {
            data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("description", calendarEvent.getSlot().getDescription());
                    put("title", calendarEvent.getSlot().getReason());
                    put("id", calendarEvent.getSlot().getId());
                    put("sessionEnd", calendarEvent.getSlot().getSessionEnd().toInstant().toEpochMilli());
                    put("sessionStart", calendarEvent.getSlot().getSessionStart().toInstant().toEpochMilli());
                    put("qrCodeId", calendarEvent.getSlot().getQrCodeId());
                    put("status", calendarEvent.getSlot().getStatus());
                }
            };

        }
        return data;
    }

    @Named("setOneToOneMeetingData")
    default Map<String, Object> setOneToOneMeetingData(CalendarEvent calendarEvent) {
        Map<String, Object> data = null;
        if (calendarEvent.getOneToOneMeeting() != null) {
            data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("description", calendarEvent.getOneToOneMeeting().getDescription());
                    put("title", calendarEvent.getOneToOneMeeting().getMeetingName());
                    put("id", calendarEvent.getOneToOneMeeting().getId());
                    put("sessionEnd",
                        calendarEvent.getOneToOneMeeting().getSessionEnd().toInstant().toEpochMilli());
                    put("sessionStart",
                        calendarEvent.getOneToOneMeeting().getSessionStart().toInstant().toEpochMilli());
                    put("willOnline", calendarEvent.getOneToOneMeeting().getWillOnline());
                    put("invitationStatus", calendarEvent.getOneToOneMeeting().getInvitationStatus());
                    put("meetingLink", calendarEvent.getOneToOneMeeting().getMeetingLink());
                    put("trainerId", null);
                    put("trainerName", null);
                    if (calendarEvent.getOneToOneMeeting().getTrainer() != null) {
                        put("trainerId", calendarEvent.getOneToOneMeeting().getTrainer().getId());
                        put("trainerName", calendarEvent.getOneToOneMeeting().getTrainer().getAlias());
                    }
                }
            };
        }
        return data;
    }

    @Named("setTrainingSession")
    default Map<String, Object> setTrainingSession(CalendarEvent calendarEvent) {
        Map<String, Object> data = null;
        if (calendarEvent.getTrainingSession() != null) {
            data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("id", calendarEvent.getId());
                    put("description", calendarEvent.getTrainingSession().getDescription());
                    put("title", calendarEvent.getTrainingSession().getMeetingName());
                    put("sessionEndDate",
                        calendarEvent.getTrainingSession().getSessionEnd().toInstant().toEpochMilli());
                    put("sessionStartDate",
                        calendarEvent.getTrainingSession().getSessionStart().toInstant().toEpochMilli());
                    put("sessionEndTime", calendarEvent.getTrainingSession().getSessionEndTime());
                    put("sessionStartTime", calendarEvent.getTrainingSession().getSessionStartTime());
                    put("isOnline", calendarEvent.getTrainingSession().getWillOnline());
                    put("workshopSessionId", calendarEvent.getTrainingSession().getWorkshopSession().getId());
                    put("workshopSessionName", calendarEvent.getTrainingSession().getWorkshopSession().getName());
                    put("meetingRoomOrLink", calendarEvent.getTrainingSession().getMeetingRoomOrLink());
                    put("isRecurring", calendarEvent.getTrainingSession().getIsRecurring());
                }
            };
        }
        return data;
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

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }


}
