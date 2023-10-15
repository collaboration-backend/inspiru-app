package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.AttendanceDto;
import com.stc.inspireu.dtos.AttendanceEventDetailDto;
import com.stc.inspireu.dtos.UserAttendanceDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.models.Attendance;
import com.stc.inspireu.models.OneToOneMeeting;
import com.stc.inspireu.models.Startup;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", uses = {User.class, OneToOneMeeting.class, Startup.class})
public interface AttendanceMapper {

    @Mapping(source = "member.alias", target = "memberName")
    @Mapping(source = "oneToOneMeeting.meetingName", target = "sessionName")
    @Mapping(source = "oneToOneMeeting.startup.startupName", target = "startupName")
    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "attendanceDate.time", target = "attendanceDate")
    @Mapping(source = "attendance", target = "attendance", qualifiedByName = "checkAttendanceStatusInString")
    AttendanceDto toAttendanceDto(Attendance attendance);

    List<AttendanceDto> toAttendanceDtoList(Iterable<Attendance> list);

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "member.alias", target = "memberName")
    @Mapping(source = "member.id", target = "memberID")
    @Mapping(source = "member.startup.startupName", target = "startupName")
    @Mapping(source = "member.startup.id", target = "startupId")
    @Mapping(source = "attendanceDate.time", target = "attendanceLongDate")
    @Mapping(source = "attendance", target = "attendance", qualifiedByName = "checkAttendanceStatusInString")
    @Mapping(source = "attendance", target = "slot", qualifiedByName = "setSlotData")
    @Mapping(source = "attendance", target = "oneToOneMeeting", qualifiedByName = "setOneToOneMeetingData")
    @Mapping(source = "attendance", target = "trainingSession", qualifiedByName = "setTrainingSession")
    AttendanceEventDetailDto toAttendanceEventDetailDto(Attendance attendance);

    List<AttendanceEventDetailDto> toAttendanceEventDetailDtoList(Iterable<Attendance> list);

    @Mapping(target = "attendanceDate", source = "attendanceDate.time")
    @Mapping(target = "day", expression = "java(formatDate(attendance.getAttendanceDate()))")
    @Mapping(target = "status", expression = "java(mapStatus(attendance))")
    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.alias", target = "memberName")
    @Mapping(source = "member.jobTitle", target = "memberTitle")
    UserAttendanceDto toUserAttendanceDto(Attendance attendance);

    List<UserAttendanceDto> toUserAttendanceDtoList(Iterable<Attendance> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }

    @Named("checkAttendanceStatusInString")
    default String checkAttendanceStatusInString(Attendance attendance) {

        String status;

        if (Boolean.FALSE.equals(attendance.getIsPresent())) {
            status = Constant.ABSENT.toString();
        } else if (attendance.getIsPresent() && attendance.getIsLate()) {
            status = Constant.LATE.toString();
        } else {
            status = Constant.PRESENT.toString();
        }
        return status;
    }

    @Named("setSlotData")
    default Map<String, Object> setSlotData(Attendance attendance) {
        Map<String, Object> data = null;
        if (attendance.getSlot() != null) {
            data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("description", attendance.getSlot().getDescription());
                    put("title", attendance.getSlot().getReason());
                    put("id", attendance.getSlot().getId());
                    put("sessionEnd", attendance.getSlot().getSessionEnd().toInstant().toEpochMilli());
                    put("sessionStart", attendance.getSlot().getSessionStart().toInstant().toEpochMilli());
                    put("qrCodeId", attendance.getSlot().getQrCodeId());
                    put("status", attendance.getSlot().getStatus());
                }
            };

        }
        return data;
    }

    @Named("setOneToOneMeetingData")
    default Map<String, Object> setOneToOneMeetingData(Attendance attendance) {
        Map<String, Object> data = null;
        if (attendance.getOneToOneMeeting() != null) {
            data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("description", attendance.getOneToOneMeeting().getDescription());
                    put("title", attendance.getOneToOneMeeting().getMeetingName());
                    put("id", attendance.getOneToOneMeeting().getId());
                    put("sessionEnd", attendance.getOneToOneMeeting().getSessionEnd().toInstant().toEpochMilli());
                    put("sessionStart",
                        attendance.getOneToOneMeeting().getSessionStart().toInstant().toEpochMilli());
                    put("willOnline", attendance.getOneToOneMeeting().getWillOnline());
                    put("invitationStatus", attendance.getOneToOneMeeting().getInvitationStatus());
                    put("meetingLink", attendance.getOneToOneMeeting().getMeetingLink());
                    put("trainerId", null);
                    put("trainerName", null);
                    if (attendance.getOneToOneMeeting().getTrainer() != null) {
                        put("trainerId", attendance.getOneToOneMeeting().getTrainer().getId());
                        put("trainerName", attendance.getOneToOneMeeting().getTrainer().getAlias());
                    }
                }
            };
        }
        return data;
    }

    @Named("setTrainingSession")
    default Map<String, Object> setTrainingSession(Attendance attendance) {
        Map<String, Object> data = null;
        if (attendance.getTrainingSession() != null) {
            data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("id", attendance.getTrainingSession().getId());

                }
            };

        }
        return data;
    }

    default String mapStatus(Attendance attendance) {
        if (Boolean.TRUE.equals(attendance.getIsPresent())) {
            return Boolean.TRUE.equals(attendance.getIsLate()) ? Constant.LATE.toString() : Constant.PRESENT.toString();
        } else {
            return Constant.ABSENT.toString();
        }
    }

    default String formatDate(Date date) {
        return date != null ? new SimpleDateFormat("dd").format(date) : null;
    }
}
