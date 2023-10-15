package com.stc.inspireu.utils;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class FileAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${minio.bucket}")
    private String minioBucket;

    @Value("${minio.management.folder}")
    private String minioManagementFolder;

    @Lazy
    @Autowired
    private MinioClient minioClient;

    @Value("${inspireu.fileDir}")
    private String inspireuFileDir;

    @Value("${inspireu.profilePics}")
    private String inspireuProfilePics;
    @Value("${inspireu.signaturePics}")
    private String inspireuSignaturePics;

    @Value("${inspireu.management.folder}")
    private String inspireuManagementFolder;

    @Autowired
    private Utility utility;

    private String minioFileFolder = "files_folders";

    private String minioProfilePics = "profile_pics";

    private String minioSignaturePics = "signature_pics";

    private String minioRegFiles = "registration_files";

    private String minioDueDiligence = "due_diligence";

    private String minioAssignments = "assignments";

    private String minioProgressReports = "progress_reports";

    private String minioPartnerLogo = "partner_logo";

    private Path userHome = Paths.get(System.getProperty("user.home"));

    public String saveProfilePic(long userId, MultipartFile profilePic) {

        Path dir = Paths.get(minioProfilePics);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        try {

            String fileName = StringUtils.cleanPath(profilePic.getOriginalFilename());

            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

            String forMinio = "/" + dirPath + "/user_" + userId + "." + extension;

            ObjectWriteResponse owr = minioClient.putObject(PutObjectArgs.builder().bucket(minioBucket).object(forMinio)
                .stream(profilePic.getInputStream(), profilePic.getSize(), -1).build());

            return forMinio;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return "";
        }

//        return saveFile(userId + "", profilePic, inspireuProfilePics);
    }

    public String saveSignaturePic(long userId, MultipartFile signaturePic) {

        Path dir = Paths.get(minioSignaturePics);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        try {

            String fileName = StringUtils.cleanPath(signaturePic.getOriginalFilename());

            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

            String forMinio = "/" + dirPath + "/user_" + userId + "." + extension;

            ObjectWriteResponse owr = minioClient.putObject(PutObjectArgs.builder().bucket(minioBucket).object(forMinio)
                .stream(signaturePic.getInputStream(), signaturePic.getSize(), -1).build());

            return forMinio;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return "";
        }

//        return saveFile(userId + "", signaturePic, inspireuSignaturePics);
    }

    public String saveFile(String name, MultipartFile multipartFile, String dir) {

        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

        Path filePath = Paths.get(userHome.toString(), inspireuFileDir, name + "." + extension);

        if (dir != null) {
            filePath = Paths.get(userHome.toString(), inspireuFileDir, dir, name + "." + extension);
        }

        try {

            File file = new File(filePath.toString());

            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write(multipartFile.getBytes());
                fos.close();
            }

            return filePath.toString();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return "";
        }

    }

    public Map<String, Object> getProfilPic(String path) {

        Map<String, Object> data = null;

        String path1 = "";

        if (path == null || path.equals("")) {
            return data;
        } else {
            path1 = path;
        }

        try {
            InputStream stream = minioClient
                .getObject(GetObjectArgs.builder().bucket(minioBucket).object(path1).build());

            data = new HashMap<String, Object>();

            String fileName = new File(path1).getName();

            data.put("file", "");
            data.put("filePath", path1);
            data.put("inputStream", stream);
            data.put("fileName", fileName);

            return data;

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return data;
        }


    }

    public Map<String, Object> getSignaturePic(String path) {

        Map<String, Object> data = null;

        String path1 = "";

        if (path == null || path.equals("")) {
            return data;
        } else {
            path1 = path;
        }

        try {
            InputStream stream = minioClient
                .getObject(GetObjectArgs.builder().bucket(minioBucket).object(path1).build());

            data = new HashMap<String, Object>();

            String fileName = new File(path1).getName();

            data.put("file", "");
            data.put("filePath", path1);
            data.put("inputStream", stream);
            data.put("fileName", fileName);

            return data;

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return data;
        }

//        Map<String, Object> data = null;
//
//        try {
//            Path p = Paths.get(StringUtils.cleanPath(path));
//            String fileName = p.getFileName().toString();
//            Path filePath = Paths.get(userHome.toString(), inspireuFileDir, inspireuSignaturePics, fileName);
//            System.out.println(filePath.toString());
//            File file = new File(filePath.toString());
//
//            data = new HashMap<String, Object>();
//            data.put("file", file);
//            data.put("filePath", filePath);
//            return data;
//        } catch (Exception e) {
//            return data;
//        }

    }

    public Map<String, Object> getManagementFileAsset(String path) {

        Map<String, Object> data = null;

        String path1 = "";

        if (path == null || path.equals("")) {
            return data;
        } else {
            path1 = path;
        }

        try {
            InputStream stream = minioClient
                .getObject(GetObjectArgs.builder().bucket(minioBucket).object(path1).build());

            data = new HashMap<String, Object>();

            String fileName = new File(path1).getName();

            data.put("file", "");
            data.put("filePath", path1);
            data.put("inputStream", stream);
            data.put("fileName", fileName);

            return data;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return data;
        }

//        Map<String, Object> data = null;
//
//        try {
//            Path p = Paths.get(StringUtils.cleanPath(path));
//            String fileName = p.getFileName().toString();
//            Path filePath = Paths.get(userHome.toString(), inspireuFileDir, inspireuManagementFolder, fileName);
//            System.out.println(filePath.toString());
//            File file = new File(filePath.toString());
//
//            data = new HashMap<String, Object>();
//            data.put("file", file);
//            data.put("filePath", filePath);
//            return data;
//        } catch (Exception e) {
//            return data;
//        }
    }

    public String saveCompanyPic(Long startupId, MultipartFile companyPic) {

        Path dirPath = Paths.get(userHome.toString(), inspireuFileDir, "startup_" + startupId);

        System.out.println(dirPath.toString());

        File directory = new File(dirPath.toString());

        if (!directory.exists()) {
            directory.mkdir();
        }

        return saveFile("company_pic" + startupId, companyPic, "startup_" + startupId);

    }

    public Map<String, Object> getCompanyPic(Long startupId, String path) {

        Map<String, Object> data = null;

        try {
            Path p = Paths.get(StringUtils.cleanPath(path));
            String fileName = p.getFileName().toString();
            Path filePath = Paths.get(userHome.toString(), inspireuFileDir, "startup_" + startupId, fileName);
            System.out.println(filePath.toString() + "------------------");
            File file = new File(filePath.toString());

            data = new HashMap<String, Object>();
            data.put("file", file);
            data.put("filePath", filePath);
            return data;
        } catch (Exception e) {
            return data;
        }
    }

    public String saveManagementFile(Long intakeId, MultipartFile multipartFile) {

        String[] dirs = new String[]{"intake_" + intakeId, "files"};

        Path dir = Paths.get("", dirs);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        try {

            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

            String forMinio = "/" + dirPath + "/" + fileName;

            ObjectWriteResponse owr = minioClient.putObject(PutObjectArgs.builder().bucket(minioBucket).object(forMinio)
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1).build());

            return forMinio;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());

            return "";
        }

//        Path dirPath = Paths.get(userHome.toString(), inspireuManagementFolder);
//
//        System.out.println(dirPath.toString());
//
//        return saveFile(multipartFile.getOriginalFilename(), multipartFile, inspireuManagementFolder);
    }

    public Map<String, Object> saveDuediligenceFile(Long intakeId, Long startupId, Long dueDiligenceId,
                                                    MultipartFile multipartFile) {

        String[] dirs = new String[]{"intake_" + intakeId, "startup_" + startupId, minioDueDiligence};

        Path dir = Paths.get("", dirs);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        try {

            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

//            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

//            String fn = fileName;// utility.getAlhpaNumeric(5); //StringUtils.cleanPath(multipartFile.getOriginalFilename());

//            String forMinio = "/" + dirPath + "/" + fn + "." + extension;
            String forMinio = "/" + dirPath + "/" + fileName;

            ObjectWriteResponse owr = minioClient.putObject(PutObjectArgs.builder().bucket(minioBucket).object(forMinio)
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1).build());

            Map<String, Object> data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("filePath", forMinio);
                    put("fileName", fileName);
                }
            };

            return data;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());

            return null;
        }

//        Path dirPath = Paths.get(userHome.toString(), inspireuFileDir, "startup_" + startupId);
//
//        System.out.println(dirPath.toString());
//
//        File directory = new File(dirPath.toString());
//
//        if (!directory.exists()) {
//            directory.mkdir();
//        }
//
//        String fn = "due_diligence_" + utility.getAlhpaNumeric(5);
//
//        String fp = saveFile(fn, multipartFile, "startup_" + startupId);
//
//        Map<String, Object> data = new HashMap<String, Object>() {
//            private static final long serialVersionUID = 1L;
//            {
//                put("filePath", fp);
//                put("fileName", fn);
//            }
//        };
//
//        return data;
    }

    public Map<String, Object> saveDuediligencePublicFile(Long dueDiligencePublicId, MultipartFile multipartFile) {
        Path dirPath = Paths.get(userHome.toString(), inspireuFileDir, "dueDiligencePublic_" + dueDiligencePublicId);

        System.out.println(dirPath.toString());

        File directory = new File(dirPath.toString());

        if (!directory.exists()) {
            directory.mkdir();
        }

        String fn = "due_diligence_" + utility.getAlhpaNumeric(5);

        String fp = saveFile(fn, multipartFile, "dueDiligencePublic_" + dueDiligencePublicId);

        Map<String, Object> data = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("filePath", fp);
                put("fileName", fn);
            }
        };

        return data;
    }

    public String saveTrainingFile(Long intakeId, Long academyRoomId, Long workshopSessionId,
                                   MultipartFile trainingFile) {

        String[] dirs = new String[]{"intake_" + intakeId, "academy_room_" + academyRoomId,
            "workshop_session_" + workshopSessionId, "training_materials"};

        Path dir = Paths.get("", dirs);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        try {

            String fileName = StringUtils.cleanPath(trainingFile.getOriginalFilename());

            String forMinio = "/" + dirPath + "/" + fileName;

            ObjectWriteResponse owr = minioClient.putObject(PutObjectArgs.builder().bucket(minioBucket).object(forMinio)
                .stream(trainingFile.getInputStream(), trainingFile.getSize(), -1).build());

            return forMinio;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return "";
        }

//        String subFolderName = "academyRoom_" + academyRoomId + "workshopSession_" + workshopSessionId + "_training";
//        Path dirPath = Paths.get(userHome.toString(), inspireuFileDir, subFolderName);
//
//        System.out.println(dirPath.toString());
//
//        File directory = new File(dirPath.toString());
//
//        if (!directory.exists()) {
//            directory.mkdir();
//        }
//        return saveFile(trainingFile.getOriginalFilename(), trainingFile, subFolderName);

    }

    public Map<String, Object> getManagementTrainingFileAsset(Long academyRoomId, Long workshopSessionId, String path) {

        Map<String, Object> data = null;

        String path1 = "";

        if (path == null || path.equals("")) {
            return data;
        } else {
            path1 = path;
        }

        try {
            InputStream stream = minioClient
                .getObject(GetObjectArgs.builder().bucket(minioBucket).object(path1).build());

            data = new HashMap<String, Object>();

            String fileName = new File(path1).getName();

            data.put("file", "");
            data.put("filePath", path1);
            data.put("inputStream", stream);
            data.put("fileName", fileName);

            return data;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return data;
        }

//        Map<String, Object> data = null;
//
//        try {
//            String subFolderName = "academyRoom_" + academyRoomId + "workshopSession_" + workshopSessionId
//                    + "_training";
//            Path p = Paths.get(StringUtils.cleanPath(trainingFilePath));
//            String fileName = p.getFileName().toString();
//            Path filePath = Paths.get(userHome.toString(), inspireuFileDir, subFolderName, fileName);
//            System.out.println(filePath.toString());
//            File file = new File(filePath.toString());
//
//            data = new HashMap<String, Object>();
//            data.put("file", file);
//            data.put("filePath", filePath);
//            return data;
//        } catch (Exception e) {
//            return data;
//        }
    }

    public Map<String, Object> getTrainingFileAsset(String path) {
        Map<String, Object> data = null;

        try {
            Path p = Paths.get(StringUtils.cleanPath(path));
            String fileName = p.getFileName().toString();
            Path filePath = Paths.get(userHome.toString(), inspireuFileDir, inspireuManagementFolder, fileName);
            System.out.println(filePath.toString());
            File file = new File(filePath.toString());

            data = new HashMap<String, Object>();
            data.put("file", file);
            data.put("filePath", filePath);
            return data;
        } catch (Exception e) {
            return data;
        }
    }

//    public Map<String, Object> getStartupsAssignmentAsset(AssignmentFile assignmentFile) {
//        Map<String, Object> data = null;
//
//        try {
//            Path p = Paths.get(StringUtils.cleanPath(assignmentFile.getPath()));
//            String fileName = p.getFileName().toString();
//            // Path filePath = Paths.get(userHome.toString(), inspireuFileDir,
//            // inspireuManagementFolder, fileName);
//            Path filePath = Paths.get(userHome.toString(), inspireuFileDir,
//                    "startup_" + assignmentFile.getAssignment().getSubmittedStartup().getId(),
//                    "assignment_" + assignmentFile.getAssignment().getId(), fileName);
//            System.out.println(filePath.toString());
//            File file = new File(filePath.toString());
//
//            data = new HashMap<String, Object>();
//            data.put("file", file);
//            data.put("filePath", filePath);
//            return data;
//        } catch (Exception e) {
//            return data;
//        }
//    }

    public String saveAssignmentFile(Long intakeId, Long academyRoomId, Long workshopSessionId, Long assignmentId,
                                     MultipartFile assignmentFile) {

        String[] dirs = new String[]{"intake_" + intakeId, "academy_room_" + academyRoomId,
            "workshop_session_" + workshopSessionId, "assignments"};

        Path dir = Paths.get("", dirs);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        try {

            String fileName = StringUtils.cleanPath(assignmentFile.getOriginalFilename());

            String forMinio = "/" + dirPath + "/" + fileName;

            ObjectWriteResponse owr = minioClient.putObject(PutObjectArgs.builder().bucket(minioBucket).object(forMinio)
                .stream(assignmentFile.getInputStream(), assignmentFile.getSize(), -1).build());

            return forMinio;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return "";
        }

//        String subFolderName = "academyRoom_" + academyRoomId + "workshopSession_" + workshopSessionId + "_assignment_"
//                + assignmentId;
//        Path dirPath = Paths.get(userHome.toString(), inspireuFileDir, inspireuManagementFolder, subFolderName);
//
//        // System.out.println(dirPath.toString());
//
//        File directory = new File(dirPath.toString());
//
//        if (!directory.exists()) {
//            directory.mkdir();
//        }
//        // return
//        // saveFile(FilenameUtils.removeExtension(assignmentFile.getOriginalFilename()),
//        // assignmentFile, inspireuManagementFolder+"\\"+subFolderName);
//        return saveFile(FilenameUtils.removeExtension(assignmentFile.getOriginalFilename()), assignmentFile,
//                inspireuManagementFolder + "/" + subFolderName);

    }

    public Map<String, Object> getManagementAssignmentFileAsset(Long academyRoomId, Long workshopSessionId,
                                                                Long assignmentId, String path) {

        Map<String, Object> data = null;

        String path1 = "";

        if (path == null || path.equals("")) {
            return data;
        } else {
            path1 = path;
        }

        try {
            InputStream stream = minioClient
                .getObject(GetObjectArgs.builder().bucket(minioBucket).object(path1).build());

            data = new HashMap<String, Object>();

            String fileName = new File(path1).getName();

            data.put("file", "");
            data.put("filePath", path1);
            data.put("inputStream", stream);
            data.put("fileName", fileName);

            return data;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return data;
        }

//        Map<String, Object> data = null;
//        try {
//            String subFolderName = "academyRoom_" + academyRoomId + "workshopSession_" + workshopSessionId
//                    + "_assignment_" + assignmentId;
//            Path p = Paths.get(StringUtils.cleanPath(assignmentFilePath));
//            String fileName = p.getFileName().toString();
//            Path filePath = Paths.get(userHome.toString(), inspireuFileDir, inspireuManagementFolder, subFolderName,
//                    fileName);
//            System.out.println(filePath.toString());
//            File file = new File(filePath.toString());
//
//            data = new HashMap<String, Object>();
//            data.put("file", file);
//            data.put("filePath", filePath);
//            return data;
//        } catch (Exception e) {
//            return data;
//        }
    }

    public Map<String, Object> updateAssignments(Long intakeId, Long startupId, Long assignmentId,
                                                 MultipartFile multipartFile) {

        String[] dirs = new String[]{"intake_" + intakeId, "startup_" + startupId, "assignments_" + assignmentId};

        Path dir = Paths.get("", dirs);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        try {

            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

            String fn = utility.getAlhpaNumeric(5);

            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

            String forMinio = "/" + dirPath + "/" + fn + "." + extension;

            ObjectWriteResponse owr = minioClient.putObject(PutObjectArgs.builder().bucket(minioBucket).object(forMinio)
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1).build());

            Map<String, Object> data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("filePath", forMinio);
                    put("fileName", fn);
                }
            };

            return data;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return null;
        }

//        Path dirPath = Paths.get(userHome.toString(), inspireuFileDir, "startup_" + startupId,
//                "assignment_" + assignment.getId());
//
//        File directory = new File(dirPath.toString());
//        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
//        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
//
//        if (!directory.exists()) {
////            directory.mkdir();
//            directory.mkdirs();
//        }
//
//        String fn = "assignment_" + utility.getAlhpaNumeric(5) + "." + extension;
//
//        // String fp = saveFile(fn, multipartFile, "startup_" + startupId +"\\"+
//        // "assignment_" + assignment.getId());
//        String fp = saveFile(fn, multipartFile, "startup_" + startupId + "/" + "assignment_" + assignment.getId());
//
//        Map<String, Object> data = new HashMap<String, Object>() {
//            private static final long serialVersionUID = 1L;
//            {
//                put("filePath", fp);
//                put("fileName", fn);
//            }
//        };
//
//        return data;

    }

    public boolean deleteFile(String path) {
        Path p = Paths.get(StringUtils.cleanPath(path));
        String fileName = p.getFileName().toString();
        Path filePath = Paths.get(userHome.toString(), inspireuFileDir, inspireuManagementFolder, fileName);
        File file = new File(filePath.toString());
        return file.delete();
    }

    public Map<String, Object> getManagementDueDiligenceFileAsset(Long startupId, String path) {

        Map<String, Object> data = null;

        String path1 = "";

        if (path == null || path.equals("")) {
            return data;
        } else {
            path1 = path;
        }

        try {
            InputStream stream = minioClient
                .getObject(GetObjectArgs.builder().bucket(minioBucket).object(path1).build());

            data = new HashMap<String, Object>();

            String fileName = new File(path1).getName();

            data.put("file", "");
            data.put("filePath", path1);
            data.put("inputStream", stream);
            data.put("fileName", fileName);

            return data;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return data;
        }

//        Map<String, Object> data = null;
//        try {
//            String subFolderName = "startup_" + startupId;
//            Path p = Paths.get(StringUtils.cleanPath(documentPath));
//            String fileName = p.getFileName().toString();
//            Path filePath = Paths.get(userHome.toString(), inspireuFileDir, subFolderName, fileName);
//            System.out.println(filePath.toString());
//            File file = new File(filePath.toString());
//
//            data = new HashMap<String, Object>();
//            data.put("file", file);
//            data.put("filePath", filePath);
//            return data;
//        } catch (Exception e) {
//            return data;
//        }
    }

//	public Map<String, Object> copyDuediligencePublicFile(Long startupId, Long dueDiligencePublicId,
//			String documentPath) {
//
//		Path p = Paths.get(StringUtils.cleanPath(documentPath));
//		String fileName = p.getFileName().toString();
//
//		Path sourceDirPath = Paths.get(userHome.toString(), inspireuFileDir,
//				"dueDiligencePublic_" + dueDiligencePublicId, fileName);
//
//		Path destDirPath = Paths.get(userHome.toString(), inspireuFileDir, "startup_" + startupId, fileName);
//
////		System.out.println(destDirPath.toString());
////
////		File directory = new File(destDirPath.toString());
////
////		if (!directory.exists()) {
////			directory.mkdir();
////		}
//
//		//String fn = "due_diligence_" + utility.getAlhpaNumeric(5);
//		String fn =fileName.substring(0, fileName.lastIndexOf('.'));
//		String fp = copyFile(sourceDirPath.toString(), destDirPath.toString());
//
//		Map<String, Object> data = new HashMap<String, Object>() {
//			private static final long serialVersionUID = 1L;
//			{
//				put("filePath", fp);
//				put("fileName", fn);
//			}
//		};
//
//		return data;
//	}

//    public String copyFile(String sourceFilePath,  String destFilePath) {
//    	try {
//    	File source = new File(sourceFilePath);
//    	File dest = new File(destFilePath);
//    	//FileUtils.copyDirectory(source, dest);
//    	FileUtils.copyFile(source, dest);
//    	 return destFilePath;
//    	} catch (IOException e) {
//            LOGGER.error(e.getMessage());
//            return "";
//        }
//    }

    public String saveProgressReportFile(Long intakeId, Long startupId, Long prId, MultipartFile multipartFile) {

        String[] dirs = new String[]{"intake_" + intakeId, "startup_" + startupId, "progress_reports_" + prId};

        Path dir = Paths.get("", dirs);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        try {

            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

            String forMinio = "/" + dirPath + "/" + fileName;

            ObjectWriteResponse owr = minioClient.putObject(PutObjectArgs.builder().bucket(minioBucket).object(forMinio)
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1).build());

            return forMinio;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return "";
        }

//        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
//
//        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
//
//        Path dir = Paths.get(userHome.toString(), inspireuFileDir, startupDir, prDir);
//
//        File directory = dir.toFile();
//
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//
//        Path filePath = Paths.get(userHome.toString(), inspireuFileDir, startupDir, prDir, fileName);
//
//        System.out.println(filePath.toString() + "----------------------------");
//
//        try {
//
//            File file = new File(filePath.toString());
//
//            try (FileOutputStream fos = new FileOutputStream(file, false)) {
//                fos.write(multipartFile.getBytes());
//                fos.close();
//            }
//
//            return filePath.toString();
//
//        } catch (IOException e) {
//            LOGGER.error(e.getMessage());
//            return "";
//        }

    }

    public Map<String, Object> getProgressReportFile(Long intakeId, Long startupId, Long progressReportId,
                                                     String fileName) {

        Map<String, Object> data = null;

        String[] dirs = new String[]{"intake_" + intakeId, "startup_" + startupId,
            "progress_reports_" + progressReportId};

        Path dir = Paths.get("", dirs);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        dirPath = "/" + dirPath + "/" + fileName;

        try {
            InputStream stream = minioClient
                .getObject(GetObjectArgs.builder().bucket(minioBucket).object(dirPath).build());

            data = new HashMap<String, Object>();

            data.put("file", "");
            data.put("filePath", dirPath);
            data.put("inputStream", stream);
            data.put("fileName", fileName);

            return data;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return data;
        }

//        Map<String, Object> data = null;
//        try {
//
//            Path dir = Paths.get(userHome.toString(), inspireuFileDir, "startup_" + startupId,
//                    "progress_report_files_" + progressReportId);
//
//            File directory = dir.toFile();
//
//            if (!directory.exists()) {
//                return null;
//            }
//
//            Path filePath = Paths.get(userHome.toString(), inspireuFileDir, "startup_" + startupId,
//                    "progress_report_files_" + progressReportId, fileName);
//
//            System.out.println(filePath.toString());
//
//            File file = new File(filePath.toString());
//
//            data = new HashMap<String, Object>();
//            data.put("file", file);
//            data.put("filePath", filePath);
//            return data;
//        } catch (Exception e) {
//            return data;
//        }
    }

    public String saveRegFormFile(MultipartFile multipartFile, Long ipId, String emailDomain) {

        String[] dirs = new String[]{"intake_" + ipId, minioRegFiles};

        Path dir = Paths.get("", dirs);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        try {

            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

            String forMinio = "/" + dirPath + "/" + fileName;

            ObjectWriteResponse owr = minioClient.putObject(PutObjectArgs.builder().bucket(minioBucket).object(forMinio)
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1).build());

            return forMinio;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return "";
        }

//        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
//
//        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
//
//        Path dir = Paths.get(userHome.toString(), inspireuFileDir, startupDir, prDir);
//
//        File directory = dir.toFile();
//
//        if (!directory.exists()) {
//            directory.mkdirs();
//            System.out.println(directory.getAbsolutePath() + "create");
//        } else {
//            System.out.println(directory.getAbsolutePath() + "already create");
//        }
//
//        Path filePath = Paths.get(userHome.toString(), inspireuFileDir, startupDir, prDir, fileName);
//
//        System.out.println(filePath.toString() + "----------------------------");
//
//        try {
//
//            File file = new File(filePath.toString());
//
//            try (FileOutputStream fos = new FileOutputStream(file, false)) {
//                fos.write(multipartFile.getBytes());
//                fos.close();
//            }
//
//            return filePath.toString();
//
//        } catch (IOException e) {
//            LOGGER.error(e.getMessage());
//            return "";
//        }

    }

    public Map<String, Object> getRegFormFile(Long intakeId, String email, String fileName) {

        Map<String, Object> data = null;

//        String emailDomain = "";
//
//        try {
//            emailDomain = email.substring(email.indexOf("@") + 1);
//            emailDomain = emailDomain.substring(0, emailDomain.indexOf("."));
//        } catch (Exception e11) {
//        }

        String[] dirs = new String[]{"intake_" + intakeId, minioRegFiles};

        Path dir = Paths.get("", dirs);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        dirPath = "/" + dirPath + "/" + fileName;

        try {
            InputStream stream = minioClient
                .getObject(GetObjectArgs.builder().bucket(minioBucket).object(dirPath).build());

            data = new HashMap<String, Object>();

            data.put("file", "");
            data.put("filePath", dirPath);
            data.put("inputStream", stream);
            data.put("fileName", fileName);

            return data;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return data;
        }

//        Map<String, Object> data = null;
//        try {
//
//            String emailDomain = "";
//
//            try {
//                emailDomain = email.substring(email.indexOf("@") + 1);
//                emailDomain = emailDomain.substring(0, emailDomain.indexOf("."));
//            } catch (Exception e11) {
//            }
//
//            Path dir = Paths.get(userHome.toString(), inspireuFileDir, "intake_" + intakeId, "registration_files");
//
//            File directory = dir.toFile();
//
//            if (!directory.exists()) {
//                return null;
//            }
//
//            Path filePath = Paths.get(userHome.toString(), inspireuFileDir, "intake_" + intakeId, "registration_files",
//                    fileName);
//
//            System.out.println(filePath.toString());
//
//            File file = new File(filePath.toString());
//
//            data = new HashMap<String, Object>();
//            data.put("file", file);
//            data.put("filePath", filePath);
//            return data;
//        } catch (Exception e) {
//            return data;
//        }
    }

    public String saveFileFolder(MultipartFile multipartFile, String... dirs) {

        Path dir = Paths.get(minioFileFolder, dirs);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        Path dir1 = Paths.get("", dirs);

        String dirPath1 = dir1.toString();

        dirPath1 = dirPath1.replace("\\", "/");

        try {

            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

            String forMinio = "/" + dirPath + "/" + fileName;

            String forDb = "/" + dirPath1 + "/" + fileName;

            forDb = forDb.startsWith("//") ? "/" + fileName : forDb;

            ObjectWriteResponse owr = minioClient.putObject(PutObjectArgs.builder().bucket(minioBucket).object(forMinio)
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1).build());

            return forDb;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return "";
        }

//        String[] full = ArrayUtils.addAll(new String[] { inspireuFileDir, minioFileFolder }, dirs);
//
//        Path dir = Paths.get(userHome.toString(), full);
//
//        File directory = dir.toFile();
//
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//
//        if (multipartFile == null) {
//            return dir.toString();
//        }
//
//        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
//
//        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
//
//        String[] fullWithFile = ArrayUtils.addAll(full, new String[] { fileName });
//
//        Path filePath = Paths.get(userHome.toString(), fullWithFile);
//
//        try {
//
//            File file = new File(filePath.toString());
//
//            try (FileOutputStream fos = new FileOutputStream(file, false)) {
//                fos.write(multipartFile.getBytes());
//                fos.close();
//            }
//
//            return filePath.toString();
//
//        } catch (IOException e) {
//            LOGGER.error(e.getMessage());
//            return "";
//        }

    }

    public void removeNestedFilesAndFolders(String... dirs) {

        try {

            String[] full = ArrayUtils.addAll(new String[]{inspireuFileDir, minioFileFolder}, dirs);

            Path filePath = Paths.get(userHome.toString(), full);

            File file = new File(filePath.toString());

            FileUtils.deleteDirectory(file);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public Map<String, Object> getFileFolders(String path) {
        Map<String, Object> data = null;

        String path1 = "";

        if (path == null || path.equals("")) {
            return data;
        } else {
            path1 = "/" + minioFileFolder + path;
        }

        try {
            InputStream stream = minioClient
                .getObject(GetObjectArgs.builder().bucket(minioBucket).object(path1).build());

            data = new HashMap<String, Object>();

            String fileName = new File(path1).getName();

            data.put("file", "");
            data.put("filePath", path1);
            data.put("inputStream", stream);
            data.put("fileName", fileName);

            return data;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return data;
        }
    }

    public void removeNestedMinioFilesAndFolders(String pre) {

        String path = "/" + minioFileFolder + pre;

        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(minioBucket).object(path).build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String savePartnerLogo(Long partnerId, MultipartFile logo) {

        Path dir = Paths.get(minioPartnerLogo);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        try {

            String fileName = StringUtils.cleanPath(logo.getOriginalFilename());

            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

            String forMinio = "/" + dirPath + "/" + partnerId + "." + extension;

            String forDb = partnerId + "." + extension;

            ObjectWriteResponse owr = minioClient.putObject(PutObjectArgs.builder().bucket(minioBucket).object(forMinio)
                .stream(logo.getInputStream(), logo.getSize(), -1).build());

            return forDb;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return "";
        }
    }

    public Map<String, Object> getPartnerLogo(String fileName) {

        Map<String, Object> data = null;

        Path dir = Paths.get(minioPartnerLogo);

        String dirPath = dir.toString();

        dirPath = dirPath.replace("\\", "/");

        dirPath = "/" + dirPath + "/" + fileName;

        try {
            InputStream stream = minioClient
                .getObject(GetObjectArgs.builder().bucket(minioBucket).object(dirPath).build());

            data = new HashMap<String, Object>();

            data.put("file", "");
            data.put("filePath", dirPath);
            data.put("inputStream", stream);
            data.put("fileName", fileName);

            return data;

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            return data;
        }
    }


    public void uploadFiles(Collection<MultipartFile> files, String directory) {
        try {
            files.forEach(file ->
            {
                try {
                    minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioBucket)
                        .object(directory + "/" + StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())))
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .build());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String uploadFile(MultipartFile file, String directory, String name) {
        try {
            String fileName = directory + "/" + name + "." + FilenameUtils.getExtension(file.getOriginalFilename());
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioBucket)
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .build());
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String uploadFile(MultipartFile file, String directory, String name, String extension) {
        try {
            String fileName = directory + "/" + name + "." + extension;
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioBucket)
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .build());
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteFiles(Collection<String> files) {
        try {
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(minioBucket)
                .objects(files.stream().map(DeleteObject::new).collect(Collectors.toSet()))
                .build());
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                System.out.println(
                    "Error in deleting object " + error.objectName() + "; " + error.message());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getFile(String filePath, String fileName) {
        InputStream stream = null;
        OutputStream outputStream = null;
        try {
            stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(minioBucket)
                .object(filePath)
                .build());

            File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
            outputStream = Files.newOutputStream(file.toPath());
            IOUtils.copy(stream, outputStream);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (Objects.nonNull(stream))
                    stream.close();
                if (Objects.nonNull(outputStream))
                    outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
