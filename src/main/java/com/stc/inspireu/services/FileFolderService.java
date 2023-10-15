package com.stc.inspireu.services;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.CreateFileFolderDto;
import com.stc.inspireu.dtos.ShareFileFolderDto;

public interface FileFolderService {

	ResponseEntity<?> listFileFolders(CurrentUserObject currentUserObject, String rootFolderId, String parentFolderId,
			String filterBy, String filterKeyword, Pageable p);

	ResponseEntity<?> getFileFolder(CurrentUserObject currentUserObject, String fileFolderId);

	ResponseEntity<?> postFileFolder(CurrentUserObject currentUserObject, CreateFileFolderDto createFileFolderDto);

	ResponseEntity<?> putFileFolder(CurrentUserObject currentUserObject, String fileFolderId,
			CreateFileFolderDto createFileFolderDto);

	ResponseEntity<?> listStartupsFileFolders(CurrentUserObject currentUserObject, String rootFolderId,
			String parentFolderId, String filterBy, String filterKeyword, Pageable p);

	ResponseEntity<?> toggleStatus(CurrentUserObject currentUserObject, String fileFolderId);

	ResponseEntity<?> deleteFileFolder(CurrentUserObject currentUserObject, String fileFolderId);

	ResponseEntity<?> sharedMembersFileFolders(CurrentUserObject currentUserObject, String parentFolderId);

	ResponseEntity<?> shareMembersFileFolders(CurrentUserObject currentUserObject, String fileFolderId,
			List<String> roles);

	ResponseEntity<?> sharingMembersFileFolders(CurrentUserObject currentUserObject, String fileFolderId,
			ShareFileFolderDto shareFileFolderDto);

	ResponseEntity<?> unshareMemberFileFolders(CurrentUserObject currentUserObject, String fileFolderId, Long memberId);

	ResponseEntity<?> intakeProgramsFileFolders(CurrentUserObject currentUserObject);

}
