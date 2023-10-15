package com.stc.inspireu.services.impl;

import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.AssetService;
import com.stc.inspireu.utils.FileAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final FileAdapter fileAdapter;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final WorkshopSessionRepository workshopSessionRepository;
    private final TrainingMaterialRepository trainingMaterialRepository;
    private final AssignmentFileRepository assignmentFileRepository;
    private final DueDiligenceFile2021Repository dueDiligenceFile2021Repository;
    private final FileFolderRepository fileFolderRepository;
    private final StartupRepository startupRepository;

    @Transactional
    @Override
    public Map<String, Object> getProfilPicByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return fileAdapter.getProfilPic(user.getProfilePic());
    }

    @Transactional
    @Override
    public Map<String, Object> getSignaturePic(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return fileAdapter.getSignaturePic(user.getSignaturePic());
    }

    @Transactional
    @Override
    public Map<String, Object> getManagementFileAsset(Long intakeProgramId, Long fileId) {
        Optional<File> file = fileRepository.findById(fileId != null ? fileId : (long) 0);
        return file.map(value -> fileAdapter.getManagementFileAsset(value.getPath())).orElse(null);

    }

    @Transactional
    @Override
    public Map<String, Object> getCompanyPicByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (Objects.nonNull(user.getStartup())) {
            return fileAdapter.getCompanyPic(user.getStartup().getId(),
                user.getStartup().getCompanyPic());
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public Map<String, Object> getManagementTrainingFileAsset(Long academyRoomId, Long workshopSessionId,
                                                              Long trainingFileId) {
        Optional<TrainingMaterial> trainingMaterial = trainingMaterialRepository
            .findById(trainingFileId != null ? trainingFileId : (long) 0);
        if (trainingMaterial.isPresent()) {
            if (Objects.equals(trainingMaterial.get().getWorkshopSession().getId(), workshopSessionId)
                && Objects.equals(trainingMaterial.get().getWorkshopSession().getAcademyRoom().getId(), academyRoomId)) {
                return fileAdapter.getManagementTrainingFileAsset(academyRoomId, workshopSessionId,
                    trainingMaterial.get().getPath());
            }
        }
        return null;
    }

    @Transactional
    @Override
    public Map<String, Object> getStartupsTrainingFileAsset(Long workshopSessionId, Long trainingFileId) {
        Optional<WorkshopSession> workshopSession = workshopSessionRepository.findById(workshopSessionId);
        if (workshopSession.isPresent()) {
            Optional<TrainingMaterial> trainingMaterial = trainingMaterialRepository.findByIdAndWorkshopSession_Id(
                trainingFileId, workshopSession.get().getRefWorkshopSession().getId());
            if (trainingMaterial.isPresent()) {
                return fileAdapter.getManagementTrainingFileAsset(
                    workshopSession.get().getRefWorkshopSession().getAcademyRoom().getId(),
                    workshopSession.get().getRefWorkshopSession().getId(), trainingMaterial.get().getPath());
            }
        }
        return null;
    }

    @Transactional
    @Override
    public Map<String, Object> getStartupsAssignmentAsset(Long materialId) {
        return assignmentFileRepository.findById(materialId).map(assignmentFile -> {
            Assignment assignment = assignmentFile.getAssignment();
            return fileAdapter.getManagementAssignmentFileAsset(
                assignment.getWorkshopSession().getAcademyRoom().getId(),
                assignment.getWorkshopSession().getId(),
                assignment.getId(), assignmentFile.getPath());
        }).orElse(null);
    }

    @Transactional
    @Override
    public Map<String, Object> getManagementAssignmentFileAsset(Long assignmentId, Long assignmentFileId) {
        Optional<AssignmentFile> assignmentFile = assignmentFileRepository.findById(assignmentFileId);
        if (assignmentFile.isPresent() && Objects.equals(assignmentFile.get().getAssignment().getId(), assignmentId)) {
            return fileAdapter.getManagementAssignmentFileAsset(
                assignmentFile.get().getAssignment().getWorkshopSession().getAcademyRoom().getId(),
                assignmentFile.get().getAssignment().getWorkshopSession().getId(),
                assignmentFile.get().getAssignment().getId(), assignmentFile.get().getPath());
        }
        return null;
    }

    @Transactional
    @Override
    public Map<String, Object> getManagementDueDiligenceFileAsset(Long dueDiligenceId, Long startupId, String fieldId,
                                                                  Long documentId) {
        return Optional.ofNullable(dueDiligenceFile2021Repository
            .findByIdAndStartup_IdAndFieldIdAndDueDiligenceTemplate2021_Id(documentId, startupId, fieldId,
                dueDiligenceId)).map(dueDiligenceFile2021 -> fileAdapter.getManagementDueDiligenceFileAsset(startupId,
            dueDiligenceFile2021.getPath())).orElse(null);
    }

    @Transactional
    @Override
    public Map<String, Object> getProgressReportFile(Long startupId, Long progressReportId, String fileName) {
        return startupRepository.findById(startupId).map(startup -> fileAdapter.getProgressReportFile(startup.getIntakeProgram().getId(), startupId, progressReportId,
            fileName)).orElse(null);
    }

    @Transactional
    @Override
    public Map<String, Object> getRegFormFile(Long intakeProgramId, String email, String fileName) {
        return fileAdapter.getRegFormFile(intakeProgramId, email, fileName);
    }

    @Override
    public Map<String, Object> getFileFolders(String fileFolderId) {
        FileFolder ff = fileFolderRepository.findByUid(fileFolderId).orElse(null);
        if (ff != null && ff.getIsFile()) {
            String p = ff.getParentFolder() + "/" + ff.getName();
            if (ff.getParentFolder().equals("/")) {
                p = "/" + ff.getName();
            }
            return fileAdapter.getFileFolders(p);
        }
        return null;
    }

    @Override
    public Map<String, Object> getPartnerLogo(String fileName) {
        return fileAdapter.getPartnerLogo(fileName);
    }
}
