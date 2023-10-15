package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.CreateFileFolderDto;
import com.stc.inspireu.dtos.ShareFileFolderDto;
import com.stc.inspireu.dtos.SharedMemberDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.jpa.projections.ProjectIntake;
import com.stc.inspireu.mappers.FileFolderMapper;
import com.stc.inspireu.models.FileFolder;
import com.stc.inspireu.models.User;
import com.stc.inspireu.models.UserResourcePermission;
import com.stc.inspireu.repositories.FileFolderRepository;
import com.stc.inspireu.repositories.IntakeProgramRepository;
import com.stc.inspireu.repositories.UserRepository;
import com.stc.inspireu.repositories.UserResourcePermissionRepository;
import com.stc.inspireu.services.FileFolderService;
import com.stc.inspireu.services.ResourcePermissionService;
import com.stc.inspireu.utils.FileAdapter;
import com.stc.inspireu.utils.ResourceUtil;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileFolderServiceImpl implements FileFolderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final FileFolderRepository fileFolderRepository;
    private final ResourcePermissionService resourcePermissionService;
    private final UserRepository userRepository;
    private final FileAdapter fileAdapter;
    private final IntakeProgramRepository intakeProgramRepository;
    private final UserResourcePermissionRepository userResourcePermissionRepository;
    private final Utility utility;
    private final FileFolderMapper fileFolderMapper;

    @Transactional
    @Override
    public ResponseEntity<?> listFileFolders(CurrentUserObject currentUserObject, String rootFolderId,
                                             String parentFolderId, String filterBy, String filterKeyword, Pageable paging) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<FileFolder> ls = Page.empty();
        Set<Long> rIds = userResourcePermissionRepository.getFileFolderResources(currentUserObject.getUserId(),
            ResourceUtil.mff);
        if (Objects.nonNull(parentFolderId) && parentFolderId.equals("0")) {
            ls = fileFolderRepository.findFileFolder(null, user.getId(), rIds, filterBy, filterKeyword,
                paging);
        } else {
            FileFolder ff = fileFolderRepository.findByUid(parentFolderId).orElseThrow(() -> new CustomRunTimeException("invalid parentFolderId"));
            ;
            if (parentFolderId.equals(rootFolderId)) {
                ls = fileFolderRepository.findFileFolder(ff.getId(), user.getId(), rIds, filterBy,
                    filterKeyword, paging);
            } else {
                FileFolder rff = fileFolderRepository.findByUid(rootFolderId).orElseThrow(() -> new CustomRunTimeException("invalid rootFolderId"));
                ;
                if (rff.getIsPublic() || rff.getCreatedUser().getId().equals(user.getId())) {
                    ls = fileFolderRepository.findFileFolder(ff.getId(), user.getId(), rIds, filterBy,
                        filterKeyword, paging);
                } else {
                    UserResourcePermission urp = userResourcePermissionRepository
                        .findTopByUserIdAndResourceIdAndResource(rff.getId(), currentUserObject.getUserId(),
                            ResourceUtil.mff);
                    if (urp != null) {
                        ls = fileFolderRepository.findFileFolder(ff.getId(), user.getId(), rIds, filterBy,
                            filterKeyword, paging);
                    }
                }
            }
        }
        return ResponseWrapper.response(ls.map(fileFolderMapper::toFileFolderDto));
    }

    @Transactional
    @Override
    public ResponseEntity<?> getFileFolder(CurrentUserObject currentUserObject, String fileFolderId) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileFolder", null);
        data.put("intakePrograms", Collections.emptyList());
        FileFolder ff = fileFolderRepository.findByUid(fileFolderId).orElseThrow(() -> new CustomRunTimeException("invalid fileFolderId"));
        data.put("fileFolder", fileFolderMapper.toFileFolderDto(ff));
        Set<Long> ipIds = userResourcePermissionRepository.getIntakeIdsFileFolderResources(ff.getId(),
            ResourceUtil.mff);
        if (ipIds != null && !ipIds.isEmpty()) {
            Set<ProjectIntake> ls = intakeProgramRepository.findByIdIn(ipIds);
            data.put("intakePrograms", ls != null ? ls : Collections.emptyList());
        }
        return ResponseWrapper.response(data);
    }

    @Transactional
    @Override
    public ResponseEntity<?> postFileFolder(CurrentUserObject currentUserObject,
                                            CreateFileFolderDto createFileFolderDto) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        String tags = "";
        if (createFileFolderDto.getTags() != null && !createFileFolderDto.getTags().isEmpty()) {
            tags = String.join(",", createFileFolderDto.getTags());
            if (tags.length() > 200) {
                return ResponseWrapper.response400("total tag length must not be grater than 200", "tags");
            }
        }
        if (createFileFolderDto.getIsFile()) {
            if (createFileFolderDto.getFiles() == null || createFileFolderDto.getFiles().length == 0) {
                return ResponseWrapper.response400("At least one file", "files");
            }
            if (createFileFolderDto.getFiles().length > 5) {
                return ResponseWrapper.response400("maximum 5 file at a time", "files");
            }
            FileFolder ref = null;
            if (createFileFolderDto.getParentFolderId() != null
                && !createFileFolderDto.getParentFolderId().equals("0")) {
                FileFolder ff = fileFolderRepository.findByUid(createFileFolderDto.getParentFolderId()).orElseThrow(() -> new CustomRunTimeException("invalid parentFolderId"));
                ref = ff;
                long c = fileFolderRepository.countByRefFileFolder_Id(ff.getId());
                if (c >= 200) {
                    return ResponseWrapper.response400("maximum 200 file/folder in dir", "parentFolderId");
                }
            } else {
                long c = fileFolderRepository.countByRefFileFolderIsNull();
                if (c >= 200) {
                    return ResponseWrapper.response400("maximum 200 file/folder in dir", "parentFolderId");
                }
            }
            for (MultipartFile f : createFileFolderDto.getFiles()) {
                FileFolder sf = new FileFolder();
                sf.setUid(utility.getAlhpaNumeric(30));
                sf.setIsFile(true);
                sf.setName(f.getOriginalFilename());
                sf.setDescription(createFileFolderDto.getDescription());
                sf.setParentFolder("/");
                sf.setCreatedUser(user);
                sf.setIsPublic(false);
                sf.setTags(tags);
                if (ref != null) {
                    FileFolder sf1 = fileFolderRepository.save(sf);
                    String pre = ref.getParentFolder().equals("/") ? "/" + ref.getId()
                        : ref.getParentFolder() + "/" + ref.getId();
                    String[] parts = (pre).split("/");
                    fileAdapter.saveFileFolder(f, parts);
                    sf1.setRefFileFolder(ref);
                    sf1.setParentFolder(pre);
                    FileFolder sf11 = fileFolderRepository.save(sf1);
                    if (createFileFolderDto.getIntakeProgramIds() != null
                        && !createFileFolderDto.getIntakeProgramIds().isEmpty()) {
                        for (Long ip : createFileFolderDto.getIntakeProgramIds()) {
                            resourcePermissionService.shareFileFolderWithIntake(ip, sf11);
                        }
                    }
                } else {
                    fileAdapter.saveFileFolder(f, "");
                    FileFolder sf1 = fileFolderRepository.save(sf);
                    if (createFileFolderDto.getIntakeProgramIds() != null
                        && !createFileFolderDto.getIntakeProgramIds().isEmpty()) {
                        for (Long ip : createFileFolderDto.getIntakeProgramIds()) {
                            resourcePermissionService.shareFileFolderWithIntake(ip, sf1);
                        }
                    }
                }
            }
            return ResponseWrapper.response("uploaded");
        } else {
            FileFolder sf = new FileFolder();
            sf.setUid(utility.getAlhpaNumeric(30));
            sf.setIsFile(false);
            sf.setName(createFileFolderDto.getName());
            sf.setDescription(createFileFolderDto.getDescription());
            sf.setParentFolder("/");
            sf.setCreatedUser(user);
            sf.setIsPublic(false);
            sf.setTags(tags);
            if (createFileFolderDto.getParentFolderId() != null
                && !createFileFolderDto.getParentFolderId().equals("0")) {
                FileFolder ff = fileFolderRepository.findByUid(createFileFolderDto.getParentFolderId()).orElseThrow(() -> new CustomRunTimeException("invalid parentFolderId"));
                if (ff.getIsFile()) {
                    return ResponseWrapper.response400("invalid parentFolderId", "parentFolderId");
                }
                long cc = fileFolderRepository.countByRefFileFolder_Id(ff.getId());
                if (cc >= 200) {
                    return ResponseWrapper.response400("maximum 200 file/folder in dir", "parentFolderId");
                }
                int c = StringUtils.countMatches(ff.getParentFolder(), "/");
                if (c > 4) {
                    return ResponseWrapper.response400("maximum 5 nested folder", "parentFolderId");
                }
                sf.setRefFileFolder(ff);
                String pre = ff.getParentFolder().equals("/") ? "/" + ff.getId()
                    : ff.getParentFolder() + "/" + ff.getId();
                sf.setParentFolder(pre);
            } else {
                long c = fileFolderRepository.countByRefFileFolderIsNull();
                if (c >= 200) {
                    return ResponseWrapper.response400("maximum 200 file/folder in dir", "parentFolderId");
                }
            }
            FileFolder sf1 = fileFolderRepository.save(sf);
            if (createFileFolderDto.getIntakeProgramIds() != null
                && !createFileFolderDto.getIntakeProgramIds().isEmpty()) {
                for (Long ip : createFileFolderDto.getIntakeProgramIds()) {
                    resourcePermissionService.shareFileFolderWithIntake(ip, sf1);
                }
            }
            return ResponseWrapper.response(fileFolderMapper.toFileFolderDto(sf1));
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> putFileFolder(CurrentUserObject currentUserObject, String fileFolderId,
                                           CreateFileFolderDto createFileFolderDto) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        FileFolder ff = fileFolderRepository.findByUid(fileFolderId).orElseThrow(() -> new CustomRunTimeException("invalid fileFolderId"));
        String tags = "";
        if (createFileFolderDto.getTags() != null && !createFileFolderDto.getTags().isEmpty()) {
            tags = String.join(",", createFileFolderDto.getTags());
            if (tags.length() > 200) {
                return ResponseWrapper.response400("total tag length must not be grater than 200", "tags");
            }
        }
        ff.setName(createFileFolderDto.getName());
        ff.setDescription(createFileFolderDto.getDescription());
        FileFolder sf1 = fileFolderRepository.save(ff);
        resourcePermissionService.removeFileFolderWithIntake(sf1);
        if (createFileFolderDto.getIntakeProgramIds() != null
            && !createFileFolderDto.getIntakeProgramIds().isEmpty()) {
            for (Long ip : createFileFolderDto.getIntakeProgramIds()) {
                resourcePermissionService.shareFileFolderWithIntake(ip, sf1);
            }
        }
        return ResponseWrapper.response(null, "Successfully Updated");
    }

    @Transactional
    @Override
    public ResponseEntity<?> listStartupsFileFolders(CurrentUserObject currentUserObject, String rootFolderId,
                                                     String parentFolderId, String filterBy, String filterKeyword, Pageable paging) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<FileFolder> ls = Page.empty();
        if (user.getStartup() != null) {
            Set<Long> rIds = userResourcePermissionRepository
                .getIntakeFileFolderResources(user.getStartup().getIntakeProgram().getId(), ResourceUtil.mff);
            if (parentFolderId.equals("0")) {
                ls = fileFolderRepository.findStartupFileFolder(null, user.getId(), rIds, filterBy, filterKeyword,
                    paging);
            } else {
                FileFolder ff = fileFolderRepository.findByUid(parentFolderId).orElseThrow(() -> new CustomRunTimeException("ParentFolder not found"));
                if (parentFolderId.equals(rootFolderId)) {
                    ls = fileFolderRepository.findStartupFileFolder(ff.getId(), user.getId(), rIds, filterBy,
                        filterKeyword, paging);
                } else {
                    FileFolder rff = fileFolderRepository.findByUid(rootFolderId).orElseThrow(() -> new CustomRunTimeException("RootFolder not found"));
                    if (rff.getIsPublic() || rff.getCreatedUser().getId().equals(user.getId())) {
                        ls = fileFolderRepository.findStartupFileFolder(ff.getId(), user.getId(), rIds,
                            filterBy, filterKeyword, paging);
                    } else {
                        UserResourcePermission urp = userResourcePermissionRepository
                            .findTopByUserIdAndResourceIdAndResource(rff.getId(), currentUserObject.getUserId(),
                                ResourceUtil.mff);
                        if (urp != null) {
                            ls = fileFolderRepository.findStartupFileFolder(ff.getId(), user.getId(), rIds,
                                filterBy, filterKeyword, paging);
                        }
                    }
                }
            }
        }
        return ResponseWrapper.response(ls.map(fileFolderMapper::toFileFolderDto));
    }

    @Transactional
    @Override
    public ResponseEntity<?> toggleStatus(CurrentUserObject currentUserObject, String fileFolderId) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        FileFolder fileFolder = fileFolderRepository.findByUid(fileFolderId).orElseThrow(() -> new CustomRunTimeException("invalid fileFolderId"));
        fileFolder.setIsPublic(!fileFolder.getIsPublic());
        FileFolder f = fileFolderRepository.save(fileFolder);
        return ResponseWrapper.response(fileFolderMapper.toFileFolderDto(f));
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteFileFolder(CurrentUserObject currentUserObject, String fileFolderId) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        FileFolder fileFolder = fileFolderRepository.findByUid(fileFolderId).orElseThrow(() -> new CustomRunTimeException("invalid fileFolderId"));
        String pre = (!fileFolder.getParentFolder().equals("/") ?
            fileFolder.getParentFolder() : "") + "/" + (fileFolder.getIsFile() ? fileFolder.getName() : fileFolder.getId());
        fileAdapter.removeNestedMinioFilesAndFolders(pre);
        fileFolderRepository.removeById(fileFolder.getId());
        resourcePermissionService.deleteFileFolder(fileFolder);
        return ResponseWrapper.response("fileFolder " + fileFolderId + " removed");
    }

    @Transactional
    @Override
    public ResponseEntity<?> sharedMembersFileFolders(CurrentUserObject currentUserObject, String parentFolderId) {
        List<SharedMemberDto> list = new ArrayList<>();
        FileFolder ff = fileFolderRepository.findByUid(parentFolderId).orElseThrow(() -> new CustomRunTimeException("invalid fileFolderId"));
        Set<Long> rIds = userResourcePermissionRepository.sharedMembersFileFolders(ff.getId(), ResourceUtil.mff);
        if (!rIds.isEmpty()) {
            userRepository.findByIdIn(rIds).forEach(t -> {
                SharedMemberDto sh = new SharedMemberDto();
                sh.setName(t.getAlias());
                sh.setId(t.getId());
                sh.setSharedStatus(true);
                if (t.getRole() != null) {
                    sh.setRoleName(t.getRole().getRoleAlias());
                }
                list.add(sh);
            });
            return ResponseWrapper.response(list);
        }
        return ResponseWrapper.response(Collections.emptyList());
    }

    @Transactional
    @Override
    public ResponseEntity<?> shareMembersFileFolders(CurrentUserObject currentUserObject, String fileFolderId,
                                                     List<String> roles) {
        List<SharedMemberDto> list = new ArrayList<>();
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        FileFolder ff = fileFolderRepository.findByUid(fileFolderId).orElseThrow(() -> new CustomRunTimeException("invalid fileFolderId"));
        Set<Long> rIds = userResourcePermissionRepository.sharedMembersFileFolders(ff.getId(), ResourceUtil.mff);
        userRepository.findByUserRoles(roles, Constant.REGISTERED.toString()).forEach(t -> {
            if (!ff.getCreatedUser().getId().equals(t.getId())
                && rIds.stream().noneMatch(e -> e != null && e.equals(t.getId()))) {
                SharedMemberDto sh = new SharedMemberDto();
                sh.setName(t.getAlias());
                sh.setId(t.getId());
                sh.setSharedStatus(false);
                if (t.getRole() != null) {
                    sh.setRoleName(t.getRole().getRoleAlias());
                }
                list.add(sh);
            }
        });
        return ResponseWrapper.response(list);
    }

    @Transactional
    @Override
    public ResponseEntity<?> sharingMembersFileFolders(CurrentUserObject currentUserObject, String fileFolderId,
                                                       ShareFileFolderDto shareFileFolderDto) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        FileFolder ff = fileFolderRepository.findByUid(fileFolderId).orElseThrow(() -> new CustomRunTimeException("invalid fileFolderId"));
        shareFileFolderDto.getMembers().forEach(userId -> resourcePermissionService.shareFileFolder(userId, ff));
        return ResponseWrapper.response(null, "shared");
    }

    @Transactional
    @Override
    public ResponseEntity<?> unshareMemberFileFolders(CurrentUserObject currentUserObject, String fileFolderId,
                                                      Long memberId) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        FileFolder ff = fileFolderRepository.findByUid(fileFolderId).orElseThrow(() -> new CustomRunTimeException("invalid fileFolderId"));
        userResourcePermissionRepository.removeByUserIdAndResourceId(memberId, ff.getId(), ResourceUtil.mff);
        return ResponseWrapper.response(null, "unshare ok");
    }

    @Transactional
    @Override
    public ResponseEntity<?> intakeProgramsFileFolders(CurrentUserObject currentUserObject) {
        return ResponseWrapper.response(intakeProgramRepository.findAllByOrderByCreatedOnAsc());
    }

}
