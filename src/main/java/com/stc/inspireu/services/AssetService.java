package com.stc.inspireu.services;

import java.util.Map;

public interface AssetService {

	Map<String, Object> getProfilPicByUserId(Long userId);

	Map<String, Object> getSignaturePic(Long userId);

	Map<String, Object> getManagementFileAsset(Long intakeProgramId, Long fileId);

	Map<String, Object> getCompanyPicByUserId(Long startupId);

	Map<String, Object> getManagementTrainingFileAsset(Long academyRoomId, Long workshopSessionId, Long trainingFileId);

	Map<String, Object> getStartupsTrainingFileAsset(Long workshopSessionId, Long trainingFileId);

	Map<String, Object> getStartupsAssignmentAsset(Long materialId);

	Map<String, Object> getManagementAssignmentFileAsset(Long workshopSessionId, Long assignmentFileId);

	Map<String, Object> getManagementDueDiligenceFileAsset(Long dueDiligenceId, Long startupId, String fieldId,
			Long documentId);

	Map<String, Object> getProgressReportFile(Long startupId, Long progressReportId, String fileName);

	Map<String, Object> getRegFormFile(Long intakeProgramId, String email, String fileName2);

	Map<String, Object> getFileFolders(String fileFolderId);

	Map<String, Object> getPartnerLogo(String fileName);
}
