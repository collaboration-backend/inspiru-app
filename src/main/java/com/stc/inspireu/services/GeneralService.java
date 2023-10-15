package com.stc.inspireu.services;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

public interface GeneralService {

    /**
     * Method to find and return file from minio
     *
     * @param path
     * @return
     */
    File findFile(String path);

    void saveActivityLog(HttpServletRequest request);
}
