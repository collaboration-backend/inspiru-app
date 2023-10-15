package com.stc.inspireu.services;

import java.util.List;
import java.util.Map;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.BookSlotDto;
import com.stc.inspireu.dtos.CalendarEventDto;
import com.stc.inspireu.dtos.EditStartupMemberDto;
import com.stc.inspireu.dtos.GetStartupProfileDto;
import com.stc.inspireu.dtos.InviteStartupMembersDto;
import com.stc.inspireu.dtos.PostOne2OneMeetingDto;
import com.stc.inspireu.dtos.RoleDto;
import com.stc.inspireu.dtos.StartupCompanyProfileDto;
import com.stc.inspireu.dtos.StartupProfileDto;
import com.stc.inspireu.dtos.StartupsRegistrationDto;
import com.stc.inspireu.dtos.UserDto;
import com.stc.inspireu.models.Slot;
import com.stc.inspireu.models.Startup;
import com.stc.inspireu.models.User;
import org.springframework.http.ResponseEntity;

public interface StartupService {

	GetStartupProfileDto getMyProfile(CurrentUserObject currentUserObject);

	Object startupsRegistration(StartupsRegistrationDto startupsRegistrationDto, Map<String, Object> claims, User user);

	Object inviteMembers(CurrentUserObject currentUserObject, InviteStartupMembersDto inviteStartupMembersDto);

	List<UserDto> getMembers(CurrentUserObject currentUserObject);

	UserDto getMember(CurrentUserObject currentUserObject, Long memberId);

	ResponseEntity<?> deleteMember(CurrentUserObject currentUserObject, Long memberId);

	User updateMember(CurrentUserObject currentUserObject, EditStartupMemberDto editStartupMemberDto, Long memberId);

	User updateMyProfile(CurrentUserObject currentUserObject, StartupProfileDto startupProfileDto);

	StartupCompanyProfileDto getCompanyProfile(CurrentUserObject currentUserObject);

	Object bookSlot(CurrentUserObject currentUserObject, BookSlotDto bookSlotDto);

	Slot cancelSlot(CurrentUserObject currentUserObject, Long slotId);

	Object createOne2OneMeeting(CurrentUserObject currentUserObject, PostOne2OneMeetingDto one2OneMeetingDto);

	List<RoleDto> getRoles(Boolean b);

	List<CalendarEventDto> getCalendarEventsByStartupIdAndMonth(CurrentUserObject currentUserObject, Integer month,
			Integer year, String timezone);

	List<CalendarEventDto> getCalendarEventsByStartupIdAndDay(CurrentUserObject currentUserObject, Integer date,
			Integer month, Integer year, String timezone);

	Startup updateStartupCompanyProfile(CurrentUserObject currentUserObject,
			StartupCompanyProfileDto startupCompanyProfileDto);

}
