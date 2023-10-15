package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.EvaluationSummaryDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.mappers.EvaluationSummaryMapper;
import com.stc.inspireu.mappers.FileFolderMapper;
import com.stc.inspireu.mappers.OpenEventMapper;
import com.stc.inspireu.models.EvaluationSummary;
import com.stc.inspireu.models.IntakeProgramSubmission;
import com.stc.inspireu.models.KeyValue;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.CommonService;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EvaluationSummaryRepository evaluationSummaryRepository;
    private final IntakeProgramSubmissionRepository intakeProgramSubmissionRepository;
    private final StartupRepository startupRepository;
    private final KeyValueRepository keyValueRepository;
    private final NotifiedUserRepository notifiedUserRepository;
    private final NotificationRepository notificationRepository;
    private final UserResourcePermissionRepository userResourcePermissionRepository;
    private final FileFolderRepository fileFolderRepository;
    private final OpenEventRepository openEventRepository;
    private final OpenEventMapper openEventMapper;
    private final EvaluationSummaryMapper evaluationSummaryMapper;
    private final FileFolderMapper fileFolderMapper;

    @Transactional
    @Override
    public List<EvaluationSummaryDto> getES() {
        return evaluationSummaryMapper.toEvaluationSummaryDtoList(evaluationSummaryRepository.findAll());
    }

    @Transactional
    @Override
    public ResponseEntity<?> pc() {
        return ResponseWrapper.response(intakeProgramSubmissionRepository.findAll());
    }

    @Transactional
    @Override
    public ResponseEntity<?> dropdownStartups(CurrentUserObject currentUserObject) {
        List<Object> list = new ArrayList<>();
        startupRepository.findAll().forEach(t -> {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", t.getId());
            map.put("name", t.getStartupName());
            list.add(map);
        });
        return ResponseWrapper.response(list);
    }

    @Transactional
    @Override
    public ResponseEntity<?> dropdownFormTemplates(CurrentUserObject currentUserObject, String formTemplateType,
                                                   String string) {
        return ResponseWrapper.response(new ArrayList<>());
    }

    @Transactional
    @Async("asyncExecutor")
    @Override
    public void dailyMidnight() {
        KeyValue midnightCronLock = keyValueRepository.findByKeyName(Constant.MIDNIGHT_CRON_LOCK.toString());
        if (midnightCronLock != null) {
            if (midnightCronLock.getValueName().equals(Constant.UNLOCKED.toString())) {
                midnightCronLock.setValueName(Constant.LOCKED.toString());
                keyValueRepository.save(midnightCronLock);
                performCleanup();
            }
        }
    }

    @Transactional
    void performCleanup() {
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.setTime(today);
        cal.add(Calendar.MONTH, -2);
        Date pm = cal.getTime();
        notificationRepository.removeOlderThan(pm);
        notifiedUserRepository.removeOlderThan(pm);
        unlockMidnightLockAfter60Sec();
    }

    @Transactional
    @Async("asyncExecutor")
    void unlockMidnightLockAfter60Sec() {
        try {
            Thread.sleep(60000);
            KeyValue midnightCronLock = keyValueRepository.findByKeyName(Constant.MIDNIGHT_CRON_LOCK.toString());
            if (midnightCronLock != null) {
                if (midnightCronLock.getValueName().equals(Constant.LOCKED.toString())) {
                    midnightCronLock.setValueName(Constant.UNLOCKED.toString());
                    keyValueRepository.save(midnightCronLock);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage() + " will try after 30sec");
            try {
                Thread.sleep(30000);
                KeyValue midnightCronLock = keyValueRepository.findByKeyName(Constant.MIDNIGHT_CRON_LOCK.toString());
                if (midnightCronLock != null) {
                    if (midnightCronLock.getValueName().equals(Constant.LOCKED.toString())) {
                        midnightCronLock.setValueName(Constant.UNLOCKED.toString());
                        keyValueRepository.save(midnightCronLock);
                    }
                }
            } catch (InterruptedException e1) {
                LOGGER.error(e1.getMessage());
            }
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> getFF() {
        return ResponseWrapper.response(fileFolderMapper.toFileFolderDtoList(fileFolderRepository.findAll()));
    }

    @Transactional
    @Override
    public ResponseEntity<?> getURP() {
        return ResponseWrapper.response(userResourcePermissionRepository.findAll());
    }

    @Override
    public ResponseEntity<?> getOE() {
        return ResponseWrapper.response(openEventMapper.toGetOpenEventDtoList(openEventRepository.findAll()));
    }

}
