package com.stc.inspireu.services;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostDueDiligenceTemplate2021Dto;
import com.stc.inspireu.dtos.PutDueDiligenceTemplate2021ManagementDto;

public interface DueDiligenceService {

	ResponseEntity<?> getPublicDueDiligenceFields(CurrentUserObject currentUserObject, Long dueDiligenceId);

	ResponseEntity<?> getDueDiligenceFields(CurrentUserObject currentUserObject, Long dueDiligenceId);

	List<?> getDueDiligenceDocuments(CurrentUserObject currentUserObject, Long dueDiligenceId, String fieldId);

	List<?> getPublicDueDiligenceDocuments(CurrentUserObject currentUserObject, Long refDueDiligenceId, String fieldId);

	Object createDueDiligence(CurrentUserObject currentUserObject, MultipartFile[] multipartFiles, Long dueDiligenceId,
			String fieldId);

	Object createPublicDueDiligence(CurrentUserObject currentUserObject, MultipartFile[] multipartFiles,
			Long dueDiligenceId, String fieldId);

	Integer deleteDueDiligence(CurrentUserObject currentUserObject, Long dueDiligenceId, String fieldId,
			Long documentId);

	Integer deletePublicDueDiligence(CurrentUserObject currentUserObject, Long refDueDiligenceId, String fieldId,
			Long documentId);

	Object createDueDiligenceFileNote(CurrentUserObject currentUserObject, String fieldId, String note);

	Map<String, Object> cloneDueDiligence(CurrentUserObject currentUserObject);

	Object confirmDueDiligenceUpload(CurrentUserObject currentUserObject, Long dueDiligenceId);

	Object confirmPublicDueDiligenceUpload(CurrentUserObject currentUserObject, Long refDueDiligenceId);

	Object getDueDiligences(CurrentUserObject currentUserObject, String intakeProgramName, Pageable pageable);

	ResponseEntity<?> getDueDiligenceSubmissions(CurrentUserObject currentUserObject, Long dueDiligenceId,
			String filterBy, String searchBy, Pageable pageable);

	ResponseEntity<?> getDueDiligenceDetail(CurrentUserObject currentUserObject, Long dueDiligenceId, String isRealId);

	Object publishDueDiligenceTemplate(CurrentUserObject currentUserObject, Long dueDiligenceId);

	Object deleteDueDiligenceTemplate(CurrentUserObject currentUserObject, Long dueDiligenceId);

	Object updateDueDiligenceTemplate(CurrentUserObject currentUserObject, Long dueDiligenceId,
			PostDueDiligenceTemplate2021Dto dueDiligenceRequest);

	ResponseEntity<?> reviewStartupDueDiligence(CurrentUserObject currentUserObject, Long dueDiligenceId,
			Long startupId, PutDueDiligenceTemplate2021ManagementDto dueDiligenceRequest);

	ResponseEntity<?> reviewStartupDueDiligenceFields(CurrentUserObject currentUserObject, Long dueDiligenceId,
			Long startupId, String fieldId, PutDueDiligenceTemplate2021ManagementDto dueDiligenceRequest);

	Map<String, Object> getManagementDueDiligenceFields(CurrentUserObject currentUserObject, Long dueDiligenceId,
			Long startupId);

	List<?> getManagementDueDiligenceDocuments(CurrentUserObject currentUserObject, Long dueDiligenceId, Long startupId,
			String fieldId);

	Object createManagementDueDiligenceFileNote(CurrentUserObject currentUserObject, Long dueDiligenceId,
			Long startupId, String fieldId, String note);

	Object getManagementDueDiligenceFileNotes(CurrentUserObject currentUserObject, Long dueDiligenceId, Long startupId,
			String fieldId, Pageable pageable);

	ResponseEntity<?> getDueDiligenceFileNotes(CurrentUserObject currentUserObject, String fieldId, Pageable pageable);

}
