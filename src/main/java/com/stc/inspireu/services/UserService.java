package com.stc.inspireu.services;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.ChangePasswordDto;
import com.stc.inspireu.dtos.GetManagementProfileDto;
import com.stc.inspireu.dtos.GetUserDto;
import com.stc.inspireu.dtos.ManagementProfileDto;
import com.stc.inspireu.dtos.UpdateNotificationDto;
import com.stc.inspireu.dtos.UserUpdateDto;
import com.stc.inspireu.models.User;

public interface UserService {

	Object changePassword(CurrentUserObject currentUserObject, ChangePasswordDto changePasswordDto);

	Object updateNotification(CurrentUserObject currentUserObject, @Valid UpdateNotificationDto updateNotificationDto);

	Object getNotification(CurrentUserObject currentUserObject);

	GetManagementProfileDto getManagementMyProfile(CurrentUserObject currentUserObject);

	User updateManagementMyProfile(CurrentUserObject currentUserObject, ManagementProfileDto managementProfileDto);

	ResponseEntity<Object> updateSignature(CurrentUserObject currentUserObject, MultipartFile signaturePic);

	Page<GetUserDto> getUsers(Pageable paging);

	User getUserRoles(long l);

	Object userUpdateMangement(UserUpdateDto userUpdateDto, Long userId);

	Page<GetUserDto> searchUsers(String string, String filterKeyword, Pageable paging);

	GetUserDto getUser(Long userId);

	User toggleUserStatus(Long userId, String string);

	User findByEmailAndInvitationStatus(String registratedEmailAddress, String string);

	List<Object> dropdownTrainers(String string);

	Object userContext(CurrentUserObject currentUserObject);
    User findUserByEmailId(String email);

}
