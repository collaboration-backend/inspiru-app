package com.stc.inspireu.services;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostFileSettingDto;
import com.stc.inspireu.models.File;

public interface FileService {

	File createManagmentGeneralFile(CurrentUserObject currentUserObject, MultipartFile file, Long inTakePgmId,
			String status);

	Object deleteDueDiligence(CurrentUserObject currentUserObject, Long documentId);

	Boolean deleteManagmentFile(Long id);

	Map<String, Object> getManagementFileAsset(Long fileId);

	ResponseEntity<?> updateFileStatus(Long id, String status);

	ResponseEntity<Object> listManagementFiles(CurrentUserObject currentUserObject, String filterKeyWord,
			String filterBy, Pageable paging);

	ResponseEntity<Object> getFiles(CurrentUserObject currentUserObject, String name, Pageable paging);

	ResponseEntity<?> filesAllowded(CurrentUserObject currentUserObject);

	ResponseEntity<?> fileSize(CurrentUserObject currentUserObject);

	ResponseEntity<Object> saveFileSettings(CurrentUserObject currentUserObject, PostFileSettingDto postFileSettingDto);

	ResponseEntity<?> getFileSettings(CurrentUserObject currentUserObject);

}
