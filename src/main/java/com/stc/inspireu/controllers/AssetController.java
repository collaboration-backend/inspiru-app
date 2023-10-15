package com.stc.inspireu.controllers;

import com.stc.inspireu.services.AssetService;
import com.stc.inspireu.utils.JwtUtil;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AssetController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INPUT_STREAM = "inputStream";
    private final AssetService assetService;
    private final ResourceLoader resourceLoader;
    private final JwtUtil jwtUtil;

    @GetMapping("/api/${api.version}/assetToken")
    @ResponseBody
    public ResponseEntity<Object> getAssetToken(HttpServletRequest httpServletRequest) {
        LOGGER.info("getAssetToken");

        Map<String, String> data = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;

            {
                put("assetToken", jwtUtil.generateAssetJwtToken(null));
            }
        };

        return ResponseWrapper.response(data);
    }

    @GetMapping(value = {"/private/assets/users/profilePic/{userId}", "/general/assets/users/profilePic/{userId}"},
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getUserProfilePicAsset(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                       @PathVariable Long userId) throws IOException {

        Map<String, Object> data = assetService.getProfilPicByUserId(userId);

        if (data != null) {
            try {

                InputStream in = (InputStream) data.get(INPUT_STREAM);

                httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

                StreamUtils.copy(in, httpServletResponse.getOutputStream());

            } catch (Exception e) {
                Resource resource = resourceLoader.getResource("classpath:static/images/default_profile.png");
                InputStream in = resource.getInputStream();
                httpServletResponse.setContentType(MediaType.IMAGE_PNG_VALUE);
                StreamUtils.copy(in, httpServletResponse.getOutputStream());
            }

        } else {
            Resource resource = resourceLoader.getResource("classpath:static/images/default_profile.png");
            InputStream in = resource.getInputStream();
            httpServletResponse.setContentType(MediaType.IMAGE_PNG_VALUE);
            StreamUtils.copy(in, httpServletResponse.getOutputStream());
        }
    }

    @GetMapping(value = "/private/assets/users/signaturePic/{userId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getUserSignaturePicAsset(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                         @PathVariable Long userId) throws IOException {

        Map<String, Object> data = assetService.getSignaturePic(userId);

        if (data != null) {
            try {

                InputStream in = (InputStream) data.get(INPUT_STREAM);

                httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

                StreamUtils.copy(in, httpServletResponse.getOutputStream());

            } catch (Exception e) {
                Resource resource = resourceLoader.getResource("classpath:static/images/default_signature.png");
                InputStream in = resource.getInputStream();
                httpServletResponse.setContentType(MediaType.IMAGE_PNG_VALUE);
                StreamUtils.copy(in, httpServletResponse.getOutputStream());
            }

        } else {
            Resource resource = resourceLoader.getResource("classpath:static/images/default_signature.png");
            InputStream in = resource.getInputStream();
            httpServletResponse.setContentType(MediaType.IMAGE_PNG_VALUE);
            StreamUtils.copy(in, httpServletResponse.getOutputStream());
        }
    }

    @GetMapping(value = "/private/assets/startups/companyPic/{userId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getCompanyPicAsset(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                   @PathVariable Long userId) throws IOException {

        Map<String, Object> data = assetService.getCompanyPicByUserId(userId);

        if (data != null) {
            try {

                Path filePath = (Path) data.get("filePath");
                File file = (File) data.get("file");

                InputStream in = new FileInputStream(file);

                httpServletResponse.setContentType(Files.probeContentType(filePath));

                StreamUtils.copy(in, httpServletResponse.getOutputStream());
            } catch (Exception e) {
                Resource resource = resourceLoader.getResource("classpath:static/images/default_logo.png");
                InputStream in = resource.getInputStream();
                httpServletResponse.setContentType(MediaType.IMAGE_PNG_VALUE);
                StreamUtils.copy(in, httpServletResponse.getOutputStream());
            }

        } else {
            Resource resource = resourceLoader.getResource("classpath:static/images/default_logo.png");
            InputStream in = resource.getInputStream();
            httpServletResponse.setContentType(MediaType.IMAGE_PNG_VALUE);
            StreamUtils.copy(in, httpServletResponse.getOutputStream());
        }
    }

    @GetMapping(value = "/private/assets/files/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getManagementFileAsset(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                       @PathVariable Long fileId,
                                       @RequestParam(defaultValue = "0") Long intakeProgramId) throws IOException {

        Map<String, Object> data = assetService.getManagementFileAsset(intakeProgramId, fileId);

        if (data != null) {

            String fileName = (String) data.get("fileName");
            InputStream in = (InputStream) data.get(INPUT_STREAM);

            httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);

            StreamUtils.copy(in, httpServletResponse.getOutputStream());
        } else {
            ResponseWrapper.createResponse404(httpServletResponse);
        }
    }

    @GetMapping(value = "/private/assets/management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/TrainingMaterials/{trainingFileId}",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getManagementTrainingFileAsset(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                               @PathVariable Long academyRoomId,
                                               @PathVariable Long workshopSessionId,
                                               @PathVariable Long trainingFileId) throws IOException {

        Map<String, Object> data = assetService.getManagementTrainingFileAsset(academyRoomId, workshopSessionId,
            trainingFileId);

        if (data != null) {

            String fileName = (String) data.get("fileName");
            InputStream in = (InputStream) data.get(INPUT_STREAM);

            httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);

            StreamUtils.copy(in, httpServletResponse.getOutputStream());

        } else {
            ResponseWrapper.createResponse404(httpServletResponse);
        }
    }

    @GetMapping(value = "/private/assets/startups/academyRooms/workshopSessions/{workshopSessionId}/TrainingMaterials/{trainingFileId}",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getStartupsTrainingFileAsset(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                             @PathVariable Long workshopSessionId,
                                             @PathVariable Long trainingFileId) throws IOException {

        Map<String, Object> data = assetService.getStartupsTrainingFileAsset(workshopSessionId, trainingFileId);

        if (data != null) {

            String fileName = (String) data.get("fileName");
            InputStream in = (InputStream) data.get(INPUT_STREAM);

            httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);

            StreamUtils.copy(in, httpServletResponse.getOutputStream());

        } else {
            ResponseWrapper.createResponse404(httpServletResponse);
        }
    }

    @GetMapping(value = "/private/assets/startups/academyRooms/workshopSessions/assignments/materials/{materialId}",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getStartupsAssignmentAsset(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                           @PathVariable Long materialId) throws IOException {

        Map<String, Object> data = assetService.getStartupsAssignmentAsset(materialId);

        if (data != null) {

            String fileName = (String) data.get("fileName");
            InputStream in = (InputStream) data.get(INPUT_STREAM);

            httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);

            StreamUtils.copy(in, httpServletResponse.getOutputStream());

        } else {
            ResponseWrapper.createResponse404(httpServletResponse);
        }
    }

    @GetMapping(value = "/private/assets/management/workshopSessions/assignments/{assignmentId}/assignmentFiles/{assignmentFileId}",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getManagementAssignmentFileAsset(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                                 @PathVariable Long assignmentId,
                                                 @PathVariable Long assignmentFileId) throws IOException {

        Map<String, Object> data = assetService.getManagementAssignmentFileAsset(assignmentId, assignmentFileId);

        if (data != null) {

            String fileName = (String) data.get("fileName");
            InputStream in = (InputStream) data.get(INPUT_STREAM);

            httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);

            StreamUtils.copy(in, httpServletResponse.getOutputStream());

        } else {
            ResponseWrapper.createResponse404(httpServletResponse);
        }
    }

    @GetMapping(value = "/private/assets/management/_dueDiligences/{dueDiligenceId}/startups/{startupId}/fields/{fieldId}/documents/{documentId}",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getManagementDueDiligenceFileAsset(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                                   @PathVariable Long dueDiligenceId, @PathVariable Long startupId,
                                                   @PathVariable String fieldId, @PathVariable Long documentId) throws IOException {

        Map<String, Object> data = assetService.getManagementDueDiligenceFileAsset(dueDiligenceId, startupId, fieldId,
            documentId);

        if (data != null) {

            String fileName = (String) data.get("fileName");
            InputStream in = (InputStream) data.get(INPUT_STREAM);

            httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);

            StreamUtils.copy(in, httpServletResponse.getOutputStream());

        } else {
            ResponseWrapper.createResponse404(httpServletResponse);
        }
    }

    @GetMapping(value = "/private/assets/startups/{startupId}/progressReports/{progressReportId}/files/{fileName}",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getProgressReportFile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                      @PathVariable Long startupId,
                                      @PathVariable Long progressReportId,
                                      @PathVariable String fileName) throws IOException {

        Map<String, Object> data = assetService.getProgressReportFile(startupId, progressReportId, fileName);

        if (data != null) {
            try {

                String fileName1 = (String) data.get("fileName");
                InputStream in = (InputStream) data.get(INPUT_STREAM);

                httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName1);

                StreamUtils.copy(in, httpServletResponse.getOutputStream());
            } catch (Exception e) {
                ResponseWrapper.createResponse404(httpServletResponse);
            }
        } else {
            ResponseWrapper.createResponse404(httpServletResponse);
        }
    }

    @GetMapping(value = "/private/assets/intakePrograms/{intakeProgramId}/files/{fileName}",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getRegFormFile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                               @PathVariable Long intakeProgramId,
                               @PathVariable String fileName,
                               @RequestParam(defaultValue = "") String email) throws IOException {

        Map<String, Object> data = assetService.getRegFormFile(intakeProgramId, email, fileName);

        if (data != null) {
            try {

                String fileName1 = (String) data.get("fileName");
                InputStream in = (InputStream) data.get(INPUT_STREAM);

                httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName1);

                StreamUtils.copy(in, httpServletResponse.getOutputStream());

            } catch (Exception e) {
                LOGGER.warn(e.getMessage());
                ResponseWrapper.createResponse404(httpServletResponse);
            }
        } else {
            ResponseWrapper.createResponse404(httpServletResponse);
        }
    }

    @GetMapping(value = "/general/assets/intakePrograms/{intakeProgramId}/files/{fileName}",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getRegFormFileForPublic(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        @PathVariable Long intakeProgramId,
                                        @PathVariable String fileName,
                                        @RequestParam(defaultValue = "") String email) throws IOException {

        Map<String, Object> data = assetService.getRegFormFile(intakeProgramId, email, fileName);

        if (data != null) {
            try {

                String fileName1 = (String) data.get("fileName");
                InputStream in = (InputStream) data.get(INPUT_STREAM);

                httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName1);

                StreamUtils.copy(in, httpServletResponse.getOutputStream());
            } catch (Exception e) {
                LOGGER.warn(e.getMessage());
                ResponseWrapper.createResponse404(httpServletResponse);
            }
        } else {
            ResponseWrapper.createResponse404(httpServletResponse);
        }
    }

    @GetMapping(value = "/private/assets/fileFolders/{fileFolderId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getFileFolders(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                               @PathVariable String fileFolderId) throws IOException {

        Map<String, Object> data = assetService.getFileFolders(fileFolderId);

        if (data != null) {
            try {


                String fileName = (String) data.get("fileName");
                InputStream in = (InputStream) data.get(INPUT_STREAM);

                httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);

                StreamUtils.copy(in, httpServletResponse.getOutputStream());
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
                ResponseWrapper.createResponse404(httpServletResponse);
            }
        } else {
            ResponseWrapper.createResponse404(httpServletResponse);
        }
    }


    @GetMapping(value = "/private/assets/partners/logo/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getPartnerLogo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                               @PathVariable String fileName) throws IOException {

        Map<String, Object> data = assetService.getPartnerLogo(fileName);

        if (data != null) {
            try {

                InputStream in = (InputStream) data.get(INPUT_STREAM);

                httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

                StreamUtils.copy(in, httpServletResponse.getOutputStream());

            } catch (Exception e) {
                Resource resource = resourceLoader.getResource("classpath:static/images/default_logo.png");
                InputStream in = resource.getInputStream();
                httpServletResponse.setContentType(MediaType.IMAGE_PNG_VALUE);
                StreamUtils.copy(in, httpServletResponse.getOutputStream());
            }

        } else {
            Resource resource = resourceLoader.getResource("classpath:static/images/default_logo.png");
            InputStream in = resource.getInputStream();
            httpServletResponse.setContentType(MediaType.IMAGE_PNG_VALUE);
            StreamUtils.copy(in, httpServletResponse.getOutputStream());
        }
    }

}
