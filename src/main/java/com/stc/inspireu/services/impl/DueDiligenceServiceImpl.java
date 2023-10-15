package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.beans.MailMetadata;
import com.stc.inspireu.dtos.DueDiligenceTemplate2021Dto;
import com.stc.inspireu.dtos.PostDueDiligenceTemplate2021Dto;
import com.stc.inspireu.dtos.PutDueDiligenceTemplate2021ManagementDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.mappers.DueDiligenceNote2021Mapper;
import com.stc.inspireu.mappers.DueDiligenceTemplate2021Mapper;
import com.stc.inspireu.models.DueDiligenceFile2021;
import com.stc.inspireu.models.DueDiligenceNote2021;
import com.stc.inspireu.models.DueDiligenceTemplate2021;
import com.stc.inspireu.models.User;
import com.stc.inspireu.repositories.DueDiligenceFile2021Repository;
import com.stc.inspireu.repositories.DueDiligenceNote2021Repository;
import com.stc.inspireu.repositories.DueDiligenceTemplate2021Repository;
import com.stc.inspireu.repositories.UserRepository;
import com.stc.inspireu.services.DueDiligenceService;
import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.utils.FileAdapter;
import com.stc.inspireu.utils.JwtUtil;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DueDiligenceServiceImpl implements DueDiligenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DueDiligenceTemplate2021Repository dueDiligenceTemplate2021Repository;
    private final DueDiligenceFile2021Repository dueDiligenceFile2021Repository;
    private final UserRepository userRepository;
    private final FileAdapter fileAdapter;
    private final DueDiligenceNote2021Repository dueDiligenceNote2021Repository;
    @Value("${ui.url}")
    private String uiUrl;
    @Value("${ui.startupMemberInvitationPath}")
    private String startupMemberInvitationPath;
    @Value("${ui.dueDiligencePublic}")
    private String dueDiligencePublic;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;
    private final DueDiligenceNote2021Mapper dueDiligenceNote2021Mapper;
    private final DueDiligenceTemplate2021Mapper dueDiligenceTemplate2021Mapper;

    @Override
    @Transactional
    public ResponseEntity<?> getPublicDueDiligenceFields(CurrentUserObject currentUserObject, Long dueDiligenceId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Map<String, Object> data = new HashMap<>();
        if (user.getStartup() != null) {
            DueDiligenceTemplate2021 dueDiligenceTemplate2021 = dueDiligenceTemplate2021Repository
                .findByRefDueDiligenceTemplate2021_IdAndStartup_Id(dueDiligenceId, user.getStartup().getId());
            if (dueDiligenceTemplate2021 != null && !dueDiligenceTemplate2021.getJsonForm().equals("")) {
                data.put("jsonForm", dueDiligenceTemplate2021.getJsonForm());
                data.put("jsonFieldActions", dueDiligenceTemplate2021.getJsonFieldActions());
                data.put("realDueDiligenceId", dueDiligenceTemplate2021.getId());
            }
        }
        return ResponseWrapper.response(data);
    }

    @Override
    @Transactional
    public ResponseEntity<?> getDueDiligenceFields(CurrentUserObject currentUserObject, Long dueDiligenceId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Map<String, Object> data = new HashMap<>();
        if (user.getStartup() != null) {
            DueDiligenceTemplate2021 dueDiligenceTemplate2021 = dueDiligenceTemplate2021Repository
                .findByIdAndStartup_Id(dueDiligenceId, user.getStartup().getId());
            if (dueDiligenceTemplate2021 != null && !dueDiligenceTemplate2021.getJsonForm().equals("")) {
                data.put("jsonForm", dueDiligenceTemplate2021.getJsonForm());
                data.put("jsonFieldActions", dueDiligenceTemplate2021.getJsonFieldActions());
            }
        }
        return ResponseWrapper.response(data);
    }

    @Override
    @Transactional
    public List<?> getDueDiligenceDocuments(CurrentUserObject currentUserObject, Long dueDiligenceId, String fieldId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            return dueDiligenceFile2021Repository
                .findByStartup_IdAndFieldIdAndDueDiligenceTemplate2021_Id(user.getStartup().getId(), fieldId,
                    dueDiligenceId);
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public List<?> getPublicDueDiligenceDocuments(CurrentUserObject currentUserObject, Long refDueDiligenceId,
                                                  String fieldId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            return dueDiligenceFile2021Repository
                .findByStartup_IdAndFieldIdAndDueDiligenceTemplate2021_RefDueDiligenceTemplate2021_Id(
                    user.getStartup().getId(), fieldId, refDueDiligenceId);
        }
        return Collections.emptyList();
    }

    @Transactional
    @Override
    public Object createDueDiligence(CurrentUserObject currentUserObject, MultipartFile[] multipartFiles,
                                     Long dueDiligenceId, String fieldId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<DueDiligenceTemplate2021> ddValidity = dueDiligenceTemplate2021Repository.findById(dueDiligenceId);
        if (ddValidity.isPresent()) {
            DueDiligenceTemplate2021 dd = ddValidity.get();
            for (MultipartFile multipartFile : multipartFiles) {
                createDueDiligenceTran(user, multipartFile, dd.getId(), fieldId);
            }
            if (!StringUtils.isBlank(dd.getJsonFieldActions())) {
                JSONObject jsonFieldActions = new JSONObject(dd.getJsonFieldActions());
                if (jsonFieldActions.has(fieldId)) {
                    JSONObject field = jsonFieldActions.getJSONObject(fieldId);
                    field.put("status", Constant.UPLOADED.toString());
                    jsonFieldActions.put(fieldId, field);
                    dd.setJsonFieldActions(jsonFieldActions.toString());
                    dueDiligenceTemplate2021Repository.save(dd);
                }
            }
        }
        return user;
    }

    @Transactional
    @Override
    public Object createPublicDueDiligence(CurrentUserObject currentUserObject, MultipartFile[] multipartFiles,
                                           Long dueDiligenceId, String fieldId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        DueDiligenceTemplate2021 dd = dueDiligenceTemplate2021Repository
            .findByRefDueDiligenceTemplate2021_IdAndStartup_Id(dueDiligenceId, user.getStartup().getId());
        if (dd != null) {
            for (MultipartFile multipartFile : multipartFiles) {
                createDueDiligenceTran(user, multipartFile, dd.getId(), fieldId);
            }
            if (dd.getJsonFieldActions() != null && !dd.getJsonFieldActions().isEmpty()) {
                JSONObject jSONObject = new JSONObject(dd.getJsonFieldActions());
                if (jSONObject.has(fieldId)) {
                    JSONObject field = jSONObject.getJSONObject(fieldId);
                    JSONObject jsonField = new JSONObject();
                    jsonField.put("status", Constant.UPLOADED.toString());
                    jsonField.put("comments", field.getString("comments"));
                    jsonField.put("reviewedBy", field.getString("reviewedBy"));
                    try {
                        jsonField.put("reviewerId", field.getNumber("reviewerId"));
                    } catch (Exception e) {
                        jsonField.put("reviewerId", "");
                    }
                    try {
                        jsonField.put("reviewedOn", field.getNumber("reviewedOn"));
                    } catch (Exception e) {
                        jsonField.put("reviewedOn", "");
                    }
                    jsonField.put("fieldLabel", field.getString("fieldLabel"));
                    jSONObject.put(fieldId, jsonField);
                    dd.setJsonFieldActions(jSONObject.toString());
                    dueDiligenceTemplate2021Repository.save(dd);
                }
            }
        }
        return user;
    }

    @Transactional
    void createDueDiligenceTran(User user, MultipartFile multipartFile, Long dueDiligenceId, String fieldId) {
        try {
            if (multipartFile != null) {
                Map<String, Object> data = fileAdapter.saveDuediligenceFile(
                    user.getStartup().getIntakeProgram().getId(), user.getStartup().getId(), dueDiligenceId,
                    multipartFile);
                if (data != null) {
                    dueDiligenceTemplate2021Repository.findById(dueDiligenceId).ifPresent(dd -> {
                        DueDiligenceFile2021 i = new DueDiligenceFile2021();
                        i.setFieldId(fieldId);
                        i.setName((String) data.get("fileName"));
                        i.setPath((String) data.get("filePath"));
                        i.setStatus(Constant.UPLOADED.toString());
                        i.setStartup(user.getStartup());
                        i.setCreatedUser(user);
                        i.setDueDiligenceTemplate2021(dd);
                        dueDiligenceFile2021Repository.save(i);
                    });
                }
            }
        } catch (Exception e1) {
            LOGGER.error(e1.getMessage());
        }
    }

    @Transactional
    @Override
    public Integer deleteDueDiligence(CurrentUserObject currentUserObject, Long dueDiligenceId, String fieldId,
                                      Long documentId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Integer i = null;
        if (user.getStartup() != null) {
            i = dueDiligenceFile2021Repository.removeByIdAndStartupIdAndFieldId(documentId,
                user.getStartup().getId(), fieldId);
        }
        return i;
    }

    @Transactional
    @Override
    public Integer deletePublicDueDiligence(CurrentUserObject currentUserObject, Long refDueDiligenceId, String fieldId,
                                            Long documentId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Integer i = null;
        if (user.getStartup() != null) {
            DueDiligenceFile2021 df = dueDiligenceFile2021Repository
                .findByIdAndStartup_IdAndFieldIdAndDueDiligenceTemplate2021_RefDueDiligenceTemplate2021_Id(
                    documentId, user.getStartup().getId(), fieldId, refDueDiligenceId);
            if (df != null) {
                dueDiligenceFile2021Repository.delete(df);
                i = documentId.intValue();
            }
        }
        return i;
    }

    @Transactional
    @Override
    public Object createDueDiligenceFileNote(CurrentUserObject currentUserObject, String fieldId, String note) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            DueDiligenceNote2021 e = new DueDiligenceNote2021();
            e.setStartupUser(user);
            e.setNote(note);
            e.setStartup(user.getStartup());
            e.setFieldId(fieldId);
            dueDiligenceNote2021Repository.save(e);
            return e;
        }
        return "invalid startup";
    }

    @Transactional
    @Override
    public Map<String, Object> cloneDueDiligence(CurrentUserObject currentUserObject) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Map<String, Object> data = new HashMap<>();
        if (user.getStartup() != null) {
            List<DueDiligenceTemplate2021> ls = dueDiligenceTemplate2021Repository.findByIntakeProgram_IdAndName(
                user.getStartup().getIntakeProgram().getId(), "startup_" + user.getStartup().getId(),
                Sort.by(Sort.Direction.DESC, "createdOn"));
            if (ls.size() == 0) {
                List<DueDiligenceTemplate2021> ls1 = dueDiligenceTemplate2021Repository
                    .findByIntakeProgram_IdAndStatusAndStartupIdIsNull(
                        user.getStartup().getIntakeProgram().getId(), Constant.PUBLISHED.toString(),
                        Sort.by(Sort.Direction.DESC, "createdOn"));
                if (ls1.size() > 0) {
                    DueDiligenceTemplate2021 ref = ls1.get(0);
                    DueDiligenceTemplate2021 e = new DueDiligenceTemplate2021();
                    e.setCreatedUser(user);
                    e.setIntakeProgram(ref.getIntakeProgram());
                    e.setJsonForm(ref.getJsonForm());
                    e.setName("startup_" + user.getStartup().getId());
                    e.setRefDueDiligenceTemplate2021(ref);
                    e.setStartup(user.getStartup());
                    e.setStatus(Constant.NOT_SUBMITTED.toString());
                    e.setSubmittedOn(new Date());
                    e.setSubmittedUser(user);
                    try {
                        JSONObject jsonForm = new JSONObject(ref.getJsonForm());
                        JSONArray form = jsonForm.getJSONArray("form");
                        JSONObject formObjFirst = form.getJSONObject(0);
                        JSONArray fieldGroup = formObjFirst.getJSONArray("fieldGroup");
                        JSONObject rootObj = new JSONObject();
                        for (int i = 0; i < fieldGroup.length(); i++) {
                            JSONObject field = fieldGroup.getJSONObject(i);
                            String key = field.getString("key");
                            JSONObject templateOptions = field.getJSONObject("templateOptions");
                            String title = templateOptions.getString("_title");
                            JSONObject jsonField = new JSONObject();
                            jsonField.put("status", "");
                            jsonField.put("comments", "");
                            jsonField.put("reviewedBy", "");
                            jsonField.put("reviewerId", "");
                            jsonField.put("reviewedOn", "");
                            jsonField.put("fieldLabel", title);
                            rootObj.put(key, jsonField);
                        }
                        e.setJsonFieldActions(rootObj.toString());
                    } catch (Exception e1) {
                        LOGGER.error(e1.getLocalizedMessage());
                    }
                    dueDiligenceTemplate2021Repository.save(e);
                    data.put("dueDiligenceId", e.getId());
                    data.put("dueDiligenceName", e.getName());
                    data.put("status", e.getStatus());
                }
            } else {
                data.put("dueDiligenceId", ls.get(0).getId());
                data.put("dueDiligenceName", ls.get(0).getName());
                data.put("status", ls.get(0).getStatus());
            }
        }
        return data;
    }

    @Transactional
    @Override
    public Object confirmDueDiligenceUpload(CurrentUserObject currentUserObject, Long dueDiligenceId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            DueDiligenceTemplate2021 dd = dueDiligenceTemplate2021Repository.findByIdAndStartup_Id(dueDiligenceId,
                user.getStartup().getId());
            if (dd != null) {
                if (!dd.getStatus().equals(Constant.SUBMITTED.toString())) {
                    dd.setStatus(Constant.SUBMITTED.toString());
                    dd.setSubmittedUser(user);
                    dd.setSubmittedOn(new Date());
                    dueDiligenceTemplate2021Repository.save(dd);
                    return dd;
                }
                return "Due diligence already submitted";
            }
            return "Due diligence not found";
        }
        return "Invalid startup";
    }

    @Transactional
    @Override
    public Object confirmPublicDueDiligenceUpload(CurrentUserObject currentUserObject, Long refDueDiligenceId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            DueDiligenceTemplate2021 dd = dueDiligenceTemplate2021Repository
                .findByRefDueDiligenceTemplate2021_IdAndStartup_Id(refDueDiligenceId,
                    user.getStartup().getId());
            if (dd != null) {
                if (!dd.getStatus().equals(Constant.SUBMITTED.toString())) {
                    dd.setStatus(Constant.SUBMITTED.toString());
                    dd.setSubmittedUser(user);
                    dd.setSubmittedOn(new Date());
                    dueDiligenceTemplate2021Repository.save(dd);
                    return dd;
                }
                return "Due diligence already submitted";
            }
            return "Due diligence not found";
        }
        return "Invalid startup";
    }

    @Transactional
    @Override
    public Object getDueDiligences(CurrentUserObject currentUserObject, String intakeProgramName, Pageable pageable) {
        return userRepository.findById(currentUserObject.getUserId())
            .map(user -> dueDiligenceTemplate2021Mapper.toDueDiligenceTemplate2021DTOList(dueDiligenceTemplate2021Repository
                .findAllByIntakeProgram_ProgramNameAndIsArchiveFalseAndStartupIsNull(intakeProgramName, pageable))).orElse(new ArrayList<>());
    }

    @Transactional
    @Override
    public ResponseEntity<?> getDueDiligenceSubmissions(CurrentUserObject currentUserObject, Long dueDiligenceId,
                                                        String filterBy, String filterKeyword, Pageable pageable) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<DueDiligenceTemplate2021> ddList;
        if (!filterKeyword.isEmpty()) {
            switch (filterBy) {
                case "Name": {
                    ddList = dueDiligenceTemplate2021Repository.findByStartupName(filterKeyword, dueDiligenceId,
                        pageable);
                    break;
                }
                case "SubmittedBy": {
                    ddList = dueDiligenceTemplate2021Repository.getDueDiligenceByKeyword(filterKeyword, dueDiligenceId,
                        pageable);
                    break;
                }
                default: {
                    ddList = dueDiligenceTemplate2021Repository
                        .findAllByRefDueDiligenceTemplate2021_IdAndStartupIsNotNull(dueDiligenceId, pageable);
                }
            }
        } else {
            ddList = dueDiligenceTemplate2021Repository
                .findAllByRefDueDiligenceTemplate2021_IdAndStartupIsNotNull(dueDiligenceId, pageable);
        }

        return ResponseWrapper.response(dueDiligenceTemplate2021Mapper.toDueDiligenceTemplate2021DTOList(ddList));
    }

    @Transactional
    @Override
    public ResponseEntity<?> getDueDiligenceDetail(CurrentUserObject currentUserObject, Long dueDiligenceId,
                                                   String isRealId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<DueDiligenceTemplate2021> dd;
        if (isRealId.equals("true")) {
            dd = dueDiligenceTemplate2021Repository.findById(dueDiligenceId);
        } else {
            dd = dueDiligenceTemplate2021Repository.findByIdAndStartupIdIsNull(dueDiligenceId);
        }
        return dd.map(dueDiligenceTemplate2021 -> ResponseWrapper.response(dueDiligenceTemplate2021Mapper.toDueDiligenceTemplate2021DTO(dueDiligenceTemplate2021)))
            .orElseGet(() -> ResponseWrapper.response400("no data found", "dueDiligenceId"));
    }

    @Transactional
    @Override
    public Object publishDueDiligenceTemplate(CurrentUserObject currentUserObject, Long dueDiligenceId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<DueDiligenceTemplate2021> dd = dueDiligenceTemplate2021Repository
            .findByIdAndStartupIdIsNull(dueDiligenceId);
        if (dd.isPresent() && !dd.get().isArchive()) {
            DueDiligenceTemplate2021 d = dd.get();
            d.setStatus(Constant.PUBLISHED.toString());
            d.setSubmittedUser(user);
            d.setSubmittedOn(new Date());
            dueDiligenceTemplate2021Repository.save(d);
            return d.getId();
        }
        return "Due diligence not found";
    }

    @Transactional
    @Override
    public Object deleteDueDiligenceTemplate(CurrentUserObject currentUserObject, Long dueDiligenceId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<DueDiligenceTemplate2021> dd = dueDiligenceTemplate2021Repository
            .findByIdAndStartupIdIsNull(dueDiligenceId);
        if (dd.isPresent() && !dd.get().isArchive()) {
            DueDiligenceTemplate2021 d = dd.get();
            d.setActive(false);
            dueDiligenceTemplate2021Repository.save(d);
            return d.getId();
        }
        return "Due diligence not found";
    }

    @Transactional
    @Override
    public Object updateDueDiligenceTemplate(CurrentUserObject currentUserObject, Long dueDiligenceId,
                                             PostDueDiligenceTemplate2021Dto dueDiligenceRequest) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        DueDiligenceTemplate2021 dd = dueDiligenceTemplate2021Repository.findByIdAndIntakeProgram_Id(dueDiligenceId,
            dueDiligenceRequest.getIntakeProgramId());
        if (dd == null) {
            return "Due diligence not found";
        }
        dd.setJsonForm(dueDiligenceRequest.getJsonForm());
        dueDiligenceTemplate2021Repository.save(dd);
        return dd.getId();
    }

    @Transactional
    @Override
    public ResponseEntity<?> reviewStartupDueDiligence(CurrentUserObject currentUserObject, Long dueDiligenceId,
                                                       Long startupId, PutDueDiligenceTemplate2021ManagementDto dueDiligenceRequest) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (!dueDiligenceRequest.getStatus().equals(Constant.APPROVED.toString())
            && !dueDiligenceRequest.getStatus().equals(Constant.RESUBMIT.toString())) {
            return ResponseWrapper.response400("Invalid review parameter provided", "status");
        }
        DueDiligenceTemplate2021 dd = dueDiligenceTemplate2021Repository
            .findByRefDueDiligenceTemplate2021_IdAndStartup_Id(dueDiligenceId, startupId);
        if (dd != null) {
            dd.setStatus(dueDiligenceRequest.getStatus());
            dd.setReviewComment(dueDiligenceRequest.getComments());
            dd.setReviewUser(user);
            dd.setReviewedOn(new Date());
            dueDiligenceTemplate2021Repository.save(dd);
            List<User> startupUsers = userRepository.findByRole_RoleNameAndStartupId(RoleName.ROLE_STARTUPS_ADMIN,
                dd.getStartup().getId());
            if (dd.getStatus().equals(Constant.APPROVED.toString()) && startupUsers.size() > 0) {
                User startupUser = startupUsers.get(0);
                if (dueDiligenceRequest.getStatus().equals(Constant.APPROVED.toString())) {
                    startupUser.setIsRemovable(false);
                    userRepository.save(startupUser);
                }
                long ms = (dd.getStartup().getIntakeProgram().getPeriodEnd().getTime() - new Date().getTime());
                String token = jwtUtil.genericJwtToken(createClaims(startupUser, dd), ms);
                startupUser.setInvitationStatus(Constant.INVITAION_SEND.toString());
                startupUser.setInviteToken(token);
                userRepository.save(startupUser);
                try {
                    JSONObject jsonFieldActions = new JSONObject(dd.getJsonFieldActions());
                    Iterator<String> keys = jsonFieldActions.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (jsonFieldActions.get(key) instanceof JSONObject) {
                            JSONObject field = jsonFieldActions.getJSONObject(key);
                            String status = field.getString("status");
                            if (!StringUtils.isBlank(status)) {
                                jsonFieldActions.put(key, setJsonField(dueDiligenceRequest, user, field.getString("fieldLabel")));
                            }
                        }
                    }
                    dd.setJsonFieldActions(jsonFieldActions.toString());
                    dueDiligenceTemplate2021Repository.save(dd);
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
                if (!token.equals("invalid_token")) {
                    if (dd.getStatus().equals(Constant.APPROVED.toString())) {
                        if (startupUser.getInvitationStatus() != null
                            && !startupUser.getInvitationStatus().equals(Constant.REGISTERED.toString())) {
                            sendInviteNotification(startupUser, token);
                        }
                    }
                }
            } else {
                MailMetadata mailMetadata = new MailMetadata();
                LOGGER.debug("inivte mail sending");
                Map<String, Object> props = new HashMap<>();
                props.put("toMail", dd.getSubmittedUser().getEmail());
                mailMetadata.setFrom("");
                mailMetadata.setTo(dd.getSubmittedUser().getEmail());
                mailMetadata.setProps(props);
                mailMetadata.setSubject("DueDiligence resubmit");
                mailMetadata.setTemplateFile("DueDiligence resubmit");
                try {
                    JSONObject jsonFieldActions = new JSONObject(dd.getJsonFieldActions());
                    Iterator<String> keys = jsonFieldActions.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (jsonFieldActions.get(key) instanceof JSONObject) {
                            JSONObject field = jsonFieldActions.getJSONObject(key);
                            String status = field.getString("status");
                            if (!StringUtils.isBlank(status)) {
                                jsonFieldActions.put(key, setJsonField(dueDiligenceRequest, user, field.getString("fieldLabel")));
                            }
                        }
                    }
                    dd.setJsonFieldActions(jsonFieldActions.toString());
                    dueDiligenceTemplate2021Repository.save(dd);
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
                sendDueDiligenceResubmit(dueDiligenceRequest, startupUsers.get(0), dd, mailMetadata, dueDiligenceId, user);
            }
            return ResponseWrapper.response(dd.getId());
        }
        return ResponseWrapper.response400("Due diligence not found", "dueDiligenceId");
    }

    private void sendDueDiligenceResubmit(PutDueDiligenceTemplate2021ManagementDto dueDiligenceRequest,
                                          User startupUser,
                                          DueDiligenceTemplate2021 dd,
                                          MailMetadata mailMetadata,
                                          Long dueDiligenceId, User user) {
        String link = "";
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", startupUser.getId());
            claims.put("intakeProgramId", dd.getStartup().getIntakeProgram().getId());
            claims.put("intakeProgramName", dd.getStartup().getIntakeProgram().getProgramName());
            claims.put("dueDiligenceId", dueDiligenceId);
            claims.put("startupId", startupUser.getStartup().getId());
            claims.put("email", startupUser.getEmail());
            claims.put("roleName", startupUser.getRole().getRoleName());
            claims.put("roleId", startupUser.getRole().getId());
            claims.put("message", Constant.STARTUP_DUEDILIGENCE_INVITATION.toString());
            long milliSeconds = (dd.getStartup().getIntakeProgram().getPeriodEnd().getTime()
                - new Date().getTime());
            String inviteToken = jwtUtil.genericJwtToken(claims, milliSeconds);
            link = uiUrl + dueDiligencePublic + "/" + inviteToken;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        notificationService.dueDiligenceResubmitNotification(mailMetadata,
            dueDiligenceRequest.getComments(), user.getId(), dd.getStartup().getId(), link);
    }

    private JSONObject setJsonField(PutDueDiligenceTemplate2021ManagementDto dueDiligenceRequest, User user, String fieldLabel) {
        JSONObject jsonField = new JSONObject();
        jsonField.put("status", dueDiligenceRequest.getStatus());
        jsonField.put("comments", dueDiligenceRequest.getComments());
        jsonField.put("reviewedBy", user.getAlias());
        jsonField.put("reviewerId", user.getId());
        jsonField.put("reviewedOn", new Date().toInstant().toEpochMilli());
        jsonField.put("fieldLabel", fieldLabel);
        return jsonField;
    }

    private void sendInviteNotification(User startupUser, String token) {
        MailMetadata mailMetadata = new MailMetadata();
        LOGGER.debug("inivte mail sending");
        Map<String, Object> props = new HashMap<>();
        String link = uiUrl + startupMemberInvitationPath + "/" + token;
        props.put("inviteLink", link);
        props.put("toMail", startupUser.getEmail());
        mailMetadata.setFrom("");
        mailMetadata.setTo(startupUser.getEmail());
        mailMetadata.setProps(props);
        mailMetadata.setSubject("User invitation mail");
        mailMetadata.setTemplateFile("user-invitation-mail");
        notificationService.sendInviteNotification(mailMetadata, "",
            startupUser.getRole().getRoleName());
    }

    private Map<String, Object> createClaims(User startupUser, DueDiligenceTemplate2021 dd) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("invitedStartup", dd.getStartup().getId());
        claims.put("startupName", dd.getStartup().getStartupName());
        claims.put("programName", dd.getStartup().getIntakeProgram().getProgramName());
        claims.put("intakeNumber", dd.getStartup().getIntakeProgram().getId());
        claims.put("email", startupUser.getEmail());
        claims.put("roleName", startupUser.getRole().getRoleName());
        claims.put("roleId", startupUser.getRole().getId());
        claims.put("message", Constant.STARTUP_INVITATION.toString());
        return claims;
    }

    @Transactional
    @Override
    public ResponseEntity<?> reviewStartupDueDiligenceFields(CurrentUserObject currentUserObject, Long dueDiligenceId,
                                                             Long startupId, String fieldId, PutDueDiligenceTemplate2021ManagementDto dueDiligenceRequest) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        JSONObject jSONObject;
        if (!dueDiligenceRequest.getStatus().equals(Constant.APPROVED.toString())
            && !dueDiligenceRequest.getStatus().equals(Constant.RESUBMIT.toString())) {
            return ResponseWrapper.response400("Invalid status", "status");
        }
        DueDiligenceTemplate2021 dd = dueDiligenceTemplate2021Repository
            .findByRefDueDiligenceTemplate2021_IdAndStartup_Id(dueDiligenceId, startupId);
        if (dd != null) {
            dd.setReviewComment(dueDiligenceRequest.getComments());
            dd.setReviewUser(user);
            dd.setReviewedOn(new Date());
            if (dd.getJsonFieldActions() != null) {
                jSONObject = new JSONObject(dd.getJsonFieldActions());
                if (jSONObject.has(fieldId)) {
                    JSONObject obj = jSONObject.getJSONObject(fieldId);
                    JSONObject temp = new JSONObject();
                    temp.put("status", dueDiligenceRequest.getStatus());
                    temp.put("comments", dueDiligenceRequest.getComments());
                    temp.put("reviewedBy", user.getAlias());
                    temp.put("reviewerId", user.getId());
                    temp.put("reviewedOn", new Date().toInstant().toEpochMilli());
                    temp.put("fieldLabel", obj.getString("fieldLabel"));
                    jSONObject.put(fieldId, temp);
                    dd.setJsonFieldActions(jSONObject.toString());
                }
            }
            dueDiligenceTemplate2021Repository.save(dd);
            if (dueDiligenceRequest.getStatus().equals(Constant.RESUBMIT.toString())) {
                try {
                    List<User> startupUsers = userRepository
                        .findByRole_RoleNameAndStartupId(RoleName.ROLE_STARTUPS_ADMIN, dd.getStartup().getId());
                    if (startupUsers != null && startupUsers.size() > 0) {
                        sendDueDiligenceMail(startupUsers.get(0), dueDiligenceId, dd, user, fieldId, dueDiligenceRequest);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
            return ResponseWrapper.response(dd.getId(), "review has been done");
        }
        return ResponseWrapper.response400("Due diligence not found", "dueDiligenceId");
    }

    private void sendDueDiligenceMail(User startupUser, Long dueDiligenceId, DueDiligenceTemplate2021 dd, User user, String fieldId, PutDueDiligenceTemplate2021ManagementDto dueDiligenceRequest) {
        String link = "";
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", startupUser.getId());
        claims.put("intakeProgramId", dd.getStartup().getIntakeProgram().getId());
        claims.put("intakeProgramName", dd.getStartup().getIntakeProgram().getProgramName());
        claims.put("dueDiligenceId", dueDiligenceId);
        claims.put("startupId", startupUser.getStartup().getId());
        claims.put("email", startupUser.getEmail());
        claims.put("roleName", startupUser.getRole().getRoleName());
        claims.put("roleId", startupUser.getRole().getId());
        claims.put("message", Constant.STARTUP_DUEDILIGENCE_INVITATION.toString());
        long milliSeconds = (dd.getStartup().getIntakeProgram().getPeriodEnd().getTime()
            - new Date().getTime());
        String inviteToken = jwtUtil.genericJwtToken(claims, milliSeconds);
        link = uiUrl + dueDiligencePublic + "/" + dueDiligenceId + "/upload/" + fieldId + "/"
            + inviteToken;
        MailMetadata mailMetadata = new MailMetadata();
        LOGGER.debug("invite mail sending");
        Map<String, Object> props = new HashMap<>();
        props.put("toMail", dd.getSubmittedUser().getEmail());
        mailMetadata.setFrom("");
        mailMetadata.setTo(dd.getSubmittedUser().getEmail());
        mailMetadata.setProps(props);
        mailMetadata.setSubject("DueDiligence resubmit");
        mailMetadata.setTemplateFile("DueDiligence resubmit");
        notificationService.dueDiligenceResubmitNotification(mailMetadata,
            dueDiligenceRequest.getComments(), user.getId(), dd.getStartup().getId(),
            link);
    }

    @Transactional
    @Override
    public Object createManagementDueDiligenceFileNote(CurrentUserObject currentUserObject, Long dueDiligenceId,
                                                       Long startupId, String fieldId, String note) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        DueDiligenceTemplate2021 dd = dueDiligenceTemplate2021Repository
            .findByRefDueDiligenceTemplate2021_IdAndStartup_Id(dueDiligenceId, startupId);
        if (dd == null) {
            return "Invalid startup related due diligence";
        }
        DueDiligenceNote2021 e = new DueDiligenceNote2021();
        e.setManagementUser(user);
        e.setReplyNote(note);
        e.setStartup(dd.getStartup());
        e.setFieldId(fieldId);
        DueDiligenceNote2021 x = dueDiligenceNote2021Repository.save(e);
        notificationService.sendDueNotes(x, user, startupId);
        return e;
    }

    @Transactional
    @Override
    public Object getManagementDueDiligenceFileNotes(CurrentUserObject currentUserObject, Long dueDiligenceId,
                                                     Long startupId, String fieldId, Pageable pageable) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        DueDiligenceTemplate2021 dd = dueDiligenceTemplate2021Repository
            .findByRefDueDiligenceTemplate2021_IdAndStartup_Id(dueDiligenceId, startupId);
        if (dd == null) {
            return "Invalid startup related due diligence";
        }
        return dueDiligenceNote2021Mapper.toDueDiligenceNote2021DtoList(dueDiligenceNote2021Repository.findByStartup_IdAndFieldId(startupId,
            fieldId, pageable)); // ResponseWrapper.response();
    }

    @Transactional
    @Override
    public Map<String, Object> getManagementDueDiligenceFields(CurrentUserObject currentUserObject,
                                                               Long refDueDiligenceId, Long startupId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, Object> data2 = new HashMap<>();
        DueDiligenceTemplate2021 dueDiligenceTemplate2021 = dueDiligenceTemplate2021Repository
            .findByRefDueDiligenceTemplate2021_IdAndStartup_Id(refDueDiligenceId, startupId);
        if (dueDiligenceTemplate2021 != null && !dueDiligenceTemplate2021.getJsonForm().equals("")) {
            DueDiligenceTemplate2021Dto startupDueDiligenceTemplate2021 = dueDiligenceTemplate2021Mapper
                .toDueDiligenceTemplate2021DTO(dueDiligenceTemplate2021);
            JSONObject jsonForm = new JSONObject(dueDiligenceTemplate2021.getJsonForm());
            JSONArray formX = jsonForm.getJSONArray("form");
            JSONObject first = formX.getJSONObject(0);
            JSONArray form = first.getJSONArray("fieldGroup");
            Set<String> fieldIds = new HashSet<String>();
            Map<String, String> keyValues = new HashMap<String, String>();
            Map<String, String> keyValues2 = new HashMap<String, String>();
            for (int i = 0; i < form.length(); i++) {
                JSONObject field = form.getJSONObject(i);
                JSONObject templateOptions = (JSONObject) field.get("templateOptions");
                String title = (String) templateOptions.get("_title");
                keyValues.put((String) field.get("key"), title);
                String description = (String) templateOptions.get("_description");
                keyValues2.put((String) field.get("key"), description);
                fieldIds.add((String) field.get("key"));
            }
            List<DueDiligenceFile2021> dists = dueDiligenceFile2021Repository
                .findByStartup_IdAndFieldIdInAndAndDueDiligenceTemplate2021_RefDueDiligenceTemplate2021_Id(
                    startupId, fieldIds, refDueDiligenceId);
            Map<String, String> kv = new HashMap<String, String>();
            for (DueDiligenceFile2021 i : dists) {
                if (kv.containsKey(i.getFieldId())) {
                    if (kv.get(i.getFieldId()).equals(Constant.UPLOADED.toString())
                        && !i.getStatus().equals(Constant.UPLOADED.toString())) {
                        kv.put(i.getFieldId(), i.getStatus());
                    }
                } else {
                    kv.put(i.getFieldId(), i.getStatus());
                }
            }
            for (String key : keyValues.keySet()) {
                JSONObject jsonFieldActions = new JSONObject(dueDiligenceTemplate2021.getJsonFieldActions());
                Iterator<String> keys = jsonFieldActions.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    if (jsonFieldActions.get(k) instanceof JSONObject) {
                        JSONObject jsonObjParent = (JSONObject) jsonFieldActions.get(k);
                        if (jsonObjParent.has(key)) {
                            JSONObject jsonField = jsonObjParent.getJSONObject(key);
                            kv.put(key, jsonField.get("status").toString());
                        }
                    }
                }
                if (kv.containsKey(key)) {
                    Map<String, String> d = new HashMap<String, String>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("fieldId", key);
                            put("fieldName", keyValues.get(key));
                            put("fieldDescription", keyValues2.get(key));
                            put("status", kv.get(key));
                        }
                    };
                    data.add(d);
                } else {
                    Map<String, String> d = new HashMap<String, String>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("fieldId", key);
                            put("fieldName", keyValues.get(key));
                            put("fieldDescription", keyValues2.get(key));
                            put("status", null);
                        }
                    };
                    data.add(d);
                }
            }

            data2 = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("dueDiligence", startupDueDiligenceTemplate2021);
                    put("fields", data);
                }
            };
        }
        return data2;
    }

    @Transactional
    @Override
    public List<?> getManagementDueDiligenceDocuments(CurrentUserObject currentUserObject, Long dueDiligenceId,
                                                      Long startupId, String fieldId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return dueDiligenceFile2021Repository
            .findByStartup_IdAndFieldIdAndDueDiligenceTemplate2021_RefDueDiligenceTemplate2021_Id(startupId,
                fieldId, dueDiligenceId);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getDueDiligenceFileNotes(CurrentUserObject currentUserObject, String fieldId,
                                                      Pageable pageable) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (Objects.nonNull(user.getStartup())) {
            Page<DueDiligenceNote2021> list = dueDiligenceNote2021Repository
                .findByStartup_IdAndFieldId(user.getStartup().getId(), fieldId, pageable);
            return ResponseWrapper.response(dueDiligenceNote2021Mapper.toDueDiligenceNote2021DtoList(list));
        }
        return ResponseWrapper.response(Page.empty());
    }
}
