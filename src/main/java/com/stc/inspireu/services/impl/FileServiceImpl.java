package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostFileSettingDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.mappers.FileMapper;
import com.stc.inspireu.models.File;
import com.stc.inspireu.models.KeyValue;
import com.stc.inspireu.models.User;
import com.stc.inspireu.repositories.FileRepository;
import com.stc.inspireu.repositories.IntakeProgramRepository;
import com.stc.inspireu.repositories.KeyValueRepository;
import com.stc.inspireu.repositories.UserRepository;
import com.stc.inspireu.services.FileService;
import com.stc.inspireu.utils.FileAdapter;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final FileRepository fileRepository;
    private final IntakeProgramRepository intakeProgramRepository;
    private final FileAdapter fileAdapter;
    private final UserRepository userRepository;
    private final KeyValueRepository keyValueRepository;
    private final FileMapper fileMapper;

    @Override
    @Transactional
    public File createManagmentGeneralFile(CurrentUserObject currentUserObject, MultipartFile multipartFile,
                                           Long inTakePgmId, String status) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        String filePath = "";
        try {
            if (Objects.nonNull(multipartFile)) {
                filePath = fileAdapter.saveManagementFile(inTakePgmId, multipartFile);
            }
            File file = new File();
            file.setName(multipartFile.getOriginalFilename());
            file.setPath(filePath);
            file.setCreatedUser(user);
            file.setIntakeProgram(intakeProgramRepository.findById(inTakePgmId).orElseThrow(() -> new CustomRunTimeException("Intake not found")));
            file.setStatus(status);
            fileRepository.save(file);
            return file;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    @Transactional
    @Override
    public Object deleteDueDiligence(CurrentUserObject currentUserObject, Long documentId) {
        return null;
    }

    @Transactional
    @Override
    public Boolean deleteManagmentFile(Long id) {
        return fileRepository.findById(id).map(file -> {
            boolean isDeleted = fileAdapter.deleteFile(file.getPath());
            if (isDeleted) {
                try {
                    return fileRepository.removeFileById(id) == 1;
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }).orElse(false);
    }

    @Transactional
    @Override
    public Map<String, Object> getManagementFileAsset(Long fileId) {
        return fileRepository.findById(fileId != null ? fileId : (long) 0)
            .map(file -> fileAdapter.getManagementFileAsset(file.getPath()))
            .orElse(null);
    }

    @Transactional
    @Override
    public ResponseEntity<?> updateFileStatus(Long id, String status) {
        File file = fileRepository.findById(id).orElseThrow(() -> new CustomRunTimeException("File not found"));
        Map<String, String> response = new HashMap<>();
        file.setStatus(status);
        response.put("status", fileRepository.save(file).getStatus());
        return ResponseWrapper.response(response);
    }

    @Transactional
    @Override
    public ResponseEntity<Object> listManagementFiles(CurrentUserObject currentUserObject, String filterKeyWord,
                                                      String filterBy, Pageable paging) {
        Page<File> list;
        if (!filterKeyWord.isEmpty()) {
            switch (filterBy) {
                case "File": {
                    list = fileRepository.findByNameContainingIgnoreCase(filterKeyWord, paging);
                    break;
                }
                case "UploadedBy": {
                    list = fileRepository.getFileByKeyword(filterKeyWord, paging);
                    break;
                }
                default: {
                    list = fileRepository.findAll(paging);
                }
            }
        } else {
            list = fileRepository.findAll(paging);
        }
        return ResponseWrapper.response(list.map(fileMapper::toFileDto));
    }

    @Transactional
    @Override
    public ResponseEntity<Object> getFiles(CurrentUserObject currentUserObject, String name, Pageable paging) {
        Page<File> list = Page.empty();
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            Long i = 0L;
            if (user.getStartup().getIntakeProgram() != null) {
                i = user.getStartup().getIntakeProgram().getId();
            }
            if (!name.isEmpty()) {
                list = fileRepository.findByIntakeProgram_IdAndStatusAndNameContainingIgnoreCase(i,
                    Constant.PUBLISHED.toString(), name, paging);
            } else {
                list = fileRepository.findByIntakeProgram_IdAndStatus(i, Constant.PUBLISHED.toString(), paging);
            }
        }
        return ResponseWrapper.response(fileMapper.toFileDtoList(list));
    }

    @Transactional
    @Override
    public ResponseEntity<?> filesAllowded(CurrentUserObject currentUserObject) {
        KeyValue filesAllowed = keyValueRepository.findByKeyName(Constant.FILES_ALLOWDED.toString());
        if (filesAllowed != null) {
            return ResponseWrapper.response(filesAllowed.getValueName());
        }
        return ResponseWrapper.response("");
    }

    @Transactional
    @Override
    public ResponseEntity<?> fileSize(CurrentUserObject currentUserObject) {
        KeyValue fileSize = keyValueRepository.findByKeyName(Constant.FILE_SIZE.toString());
        if (fileSize != null) {
            return ResponseWrapper.response(Integer.parseInt(fileSize.getValueName()));
        }
        return ResponseWrapper.response(0);
    }

    @Transactional
    @Override
    public ResponseEntity<Object> saveFileSettings(CurrentUserObject currentUserObject,
                                                   PostFileSettingDto postFileSettingDto) {
        KeyValue filesAllowed = keyValueRepository.findByKeyName(Constant.FILES_ALLOWDED.toString());
        if (filesAllowed != null) {
            String[] fileTypes = postFileSettingDto.getFileTypes().split(",");
            Pattern pattern = Pattern.compile("^[.][a-zA-Z0-9.]+$");
            for (String type : fileTypes) {
                if (!pattern.matcher(type).matches())
                    throw new CustomRunTimeException(type + " is an unsupported file type.");
            }
            filesAllowed.setValueName(postFileSettingDto.getFileTypes());
            keyValueRepository.save(filesAllowed);
        }
        KeyValue fileSize = keyValueRepository.findByKeyName(Constant.FILE_SIZE.toString());
        if (fileSize != null) {
            fileSize.setValueName(postFileSettingDto.getFileSize() + "");
            keyValueRepository.save(fileSize);
        }
        return ResponseWrapper.response("Successfully updated");
    }

    @Transactional
    @Override
    public ResponseEntity<?> getFileSettings(CurrentUserObject currentUserObject) {
        Map<String, Object> d = new HashMap<>();
        d.put("fileTypes", "");
        d.put("fileSize", 0);
        KeyValue filesAllowed = keyValueRepository.findByKeyName(Constant.FILES_ALLOWDED.toString());
        if (filesAllowed != null) {
            d.put("fileTypes", filesAllowed.getValueName());
        }
        KeyValue fileSize = keyValueRepository.findByKeyName(Constant.FILE_SIZE.toString());
        if (fileSize != null) {
            d.put("fileSize", Integer.parseInt(fileSize.getValueName()));
        }
        return ResponseWrapper.response(d);
    }
}
