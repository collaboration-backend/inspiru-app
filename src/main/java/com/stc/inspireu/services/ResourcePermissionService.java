package com.stc.inspireu.services;

import java.util.List;
import java.util.Set;

import com.stc.inspireu.models.FileFolder;
import com.stc.inspireu.models.Role;
import com.stc.inspireu.models.User;

public interface ResourcePermissionService {

	Set<Long> getManagementAcademyRoomIds(long userId);

	Object getManagementWorkshopSessionIds(long userId, Long academyRoomId);

	void createAcademyRoom(Long resourceId, Long createdUserId);

	void shareAcademyRoom(Long resourceId, Long createdUserId, List<Long> sharedUserIds, User user, Role role);

	boolean isPermit(long userId, String resource, String parentResource, String grandParentResource, Long resourceId,
			Long pId, Long gpId, String permission);

	void createResource(Long createdUserId, Long resourceId, String resourceType);

	void shareWorkshopSession(Long academyRoomId, Long workshopSessionId, Long createdUserId, List<Long> sharedUserIds);

	void createWorkshopSession(Long academyRoomId, Long workshopSessionId, Long createdUserId);

	void unShareAcademyRoom(Long academyRoomId, Long unSharedUserId);

	void unShareWorkshopSession(Long academyRoomId, Long workshopSessionId, Long unSharedUserId);

//    mark cards
	void shareMarkCardAcademyRoom(Long markCardId, Long academyRoomId, Long createdUserId, List<Long> sharedUserIds);

	void unShareMarkCardAcademyRoom(Long markCardId, Long academyRoomId, Long unSharedUserId);

	Set<Long> getManagementMarkCardIds(long userId);

	Set<Long> getManagementAcademyRoomMarkCardIds(long userId, Long markCardId);

	Set<Long> getFileFolderIds(Long userId, Long startupId);

	void createFileFolder(Long id, Long id2);

	void saveFileFolder(User user, FileFolder sf);

	void deleteFileFolder(FileFolder fileFolder);

	void shareFileFolder(Long userId, FileFolder id);

	void shareFileFolderWithIntake(Long intakeId, FileFolder sf1);

	void removeFileFolderWithIntake(FileFolder sf1);

}
