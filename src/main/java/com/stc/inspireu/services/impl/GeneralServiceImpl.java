package com.stc.inspireu.services.impl;

import com.stc.inspireu.enums.Activity;
import com.stc.inspireu.models.ActivityLog;
import com.stc.inspireu.repositories.ActivityLogRepository;
import com.stc.inspireu.services.GeneralService;
import com.stc.inspireu.utils.FileAdapter;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GeneralServiceImpl implements GeneralService {

    private final FileAdapter fileAdapter;

    private final ActivityLogRepository activityLogRepository;

    @Override
    public File findFile(String path) {
        return fileAdapter.getFile(path, (path.contains("/") ? path.split("/")[1] : path));
    }

    @Override
    public void saveActivityLog(HttpServletRequest request) {
        activityLogRepository.save(new ActivityLog(LocalDateTime.now(), Activity.LOGIN_PAGE_LOADED,
            Utility.getClientIpAddress(request)));
    }
}
