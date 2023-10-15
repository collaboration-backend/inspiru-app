package com.stc.inspireu.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.services.ResourcePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.FileFolder;
import com.stc.inspireu.models.Role;
import com.stc.inspireu.models.User;
import com.stc.inspireu.models.UserResourcePermission;
import com.stc.inspireu.repositories.UserResourcePermissionRepository;
import com.stc.inspireu.utils.PermissionUtil;
import com.stc.inspireu.utils.ResourceUtil;

@Service
@RequiredArgsConstructor
public class ResourcePermissionServiceImpl implements ResourcePermissionService {
	private final UserResourcePermissionRepository userResourcePermissionRepository;
    private final NotificationService notificationService;

	@Transactional
	@Override
	public Set<Long> getManagementAcademyRoomIds(long userId) {
        return userResourcePermissionRepository.getAcademyRoomResourceIds(userId, ResourceUtil.mar);
	}

	@Transactional
	@Override
	public Object getManagementWorkshopSessionIds(long userId, Long academyRoomId) {
		Set<String> permissions = userResourcePermissionRepository.getAcademyRoomResourceResources(userId,
				academyRoomId, ResourceUtil.mar, PermissionUtil.all);
		if (permissions.size() > 0) {
			return true;
		}
        return userResourcePermissionRepository.getWorkshpSessionResourceIds(userId, ResourceUtil.mws);
	}

	@Transactional
	@Override
	public void createAcademyRoom(Long createdUserId, Long academyRoomId) {

		UserResourcePermission upo = new UserResourcePermission();

		upo.setActionsOrMethods(PermissionUtil.all);
//        upo.setIsUrl(false);
		upo.setResource(ResourceUtil.mar);
		upo.setResourceId(academyRoomId);
		upo.setUserId(createdUserId);

		userResourcePermissionRepository.save(upo);
	}

	@Transactional
	@Override
	public void shareAcademyRoom(Long academyRoomId, Long createdUserId, List<Long> sharedUserIds, User currentUser,
			Role currentRole) {
		Set<Long> sharedIds = new HashSet<>(sharedUserIds);
		sharedUserIds.clear();
		sharedUserIds.addAll(sharedIds);
		for (Long sharedUserId : sharedUserIds) {
			System.out.println(createdUserId + " " + sharedUserId);
			userResourcePermissionRepository.removeAllAcademyRoomChildResource(sharedUserId, academyRoomId);
		}
		List<UserResourcePermission> ls = new ArrayList<>();
		for (Long sharedUserId : sharedUserIds) {
			UserResourcePermission upo1 = new UserResourcePermission();
			upo1.setActionsOrMethods(PermissionUtil.all);
			upo1.setResource(ResourceUtil.mar);
			upo1.setResourceId(academyRoomId);
			upo1.setUserId(sharedUserId);
			ls.add(upo1);
		}
		notificationService.academyRoomShareNotification(academyRoomId, sharedUserIds, currentUser, currentRole);
		userResourcePermissionRepository.saveAll(ls);
	}

	@Transactional
	@Override
	public void createWorkshopSession(Long academyRoomId, Long workshopSessionId, Long createdUserId) {
		UserResourcePermission upo1 = new UserResourcePermission();
		upo1.setActionsOrMethods(PermissionUtil.all);
//        upo1.setIsUrl(false);
		upo1.setResource(ResourceUtil.mws);
		upo1.setResourceId(workshopSessionId);
		upo1.setUserId(createdUserId);
		upo1.setParentResourceId(academyRoomId);
		userResourcePermissionRepository.save(upo1);
	}

	@Transactional
	@Override
	public void shareWorkshopSession(Long academyRoomId, Long workshopSessionId, Long createdUserId,
			List<Long> sharedUserIds) {
		Set<Long> sharedIds = new HashSet<>(sharedUserIds);
		sharedUserIds.clear();
		sharedUserIds.addAll(sharedIds);
		for (Long sharedUserId : sharedUserIds) {
			UserResourcePermission urp = userResourcePermissionRepository
					.findByUserIdAndResourceIdAndResource(sharedUserId, academyRoomId, ResourceUtil.mar);
			if (urp != null) {
				if (!urp.getActionsOrMethods().equals(PermissionUtil.all)) {
					// check whether same shared user exists for the selected workshopSession
					UserResourcePermission wsUrp = userResourcePermissionRepository
							.findByUserIdAndResourceIdAndResource(sharedUserId, workshopSessionId, ResourceUtil.mws);

					UserResourcePermission upo11 = new UserResourcePermission();
					if (wsUrp != null) {
						upo11 = wsUrp;
					}
					upo11.setActionsOrMethods(PermissionUtil.all);
//                    upo11.setIsUrl(false);
					upo11.setResource(ResourceUtil.mws);
					upo11.setResourceId(workshopSessionId);
					upo11.setUserId(sharedUserId);
					upo11.setParentResourceId(academyRoomId);
					userResourcePermissionRepository.save(upo11);
				}
			} else {
				UserResourcePermission upo1 = new UserResourcePermission();
				upo1.setActionsOrMethods(PermissionUtil.get);
//                upo1.setIsUrl(false);
				upo1.setResource(ResourceUtil.mar);
				upo1.setResourceId(academyRoomId);
				upo1.setUserId(sharedUserId);
				userResourcePermissionRepository.save(upo1);
				UserResourcePermission upo11 = new UserResourcePermission();
				upo11.setActionsOrMethods(PermissionUtil.all);
//                upo11.setIsUrl(false);
				upo11.setResource(ResourceUtil.mws);
				upo11.setResourceId(workshopSessionId);
				upo11.setUserId(sharedUserId);
				upo11.setParentResourceId(academyRoomId);
				userResourcePermissionRepository.save(upo11);
			}
		}
	}

	@Transactional
	@Override
	public void createResource(Long createdUserId, Long resourceId, String resourceType) {
		UserResourcePermission upo1 = new UserResourcePermission();
		upo1.setActionsOrMethods(PermissionUtil.all);
//        upo1.setIsUrl(false);
		upo1.setResource(resourceType);
		upo1.setResourceId(resourceId);
		upo1.setUserId(createdUserId);
		userResourcePermissionRepository.save(upo1);
	}

	@Transactional
	@Override
	public boolean isPermit(long userId, String resource, String parentResource, String grandParentResource,
			Long resourceId, Long pId, Long gpId, String permission) {
		Set<String> permissions = userResourcePermissionRepository.getPermissions(userId, resourceId, resource);
		for (String per : permissions) {
			if (per.contains(PermissionUtil.all) || per.contains(permission)) {
				return true;
			}
		}
		if (pId > 0) {
			Set<String> permissions1 = userResourcePermissionRepository.getPermissions(userId, pId, parentResource);
			for (String per : permissions1) {
				if (per.contains(PermissionUtil.all) || per.contains(permission)) {
					return true;
				}
			}
		}
		if (gpId > 0) {
			Set<String> permissions1 = userResourcePermissionRepository.getPermissions(userId, gpId,
					grandParentResource);
			for (String per : permissions1) {
				if (per.contains(PermissionUtil.all) || per.contains(permission)) {
					return true;
				}
			}
		}
		return false;
	}

	@Transactional
	@Override
	public void unShareAcademyRoom(Long academyRoomId, Long unSharedUserId) {
		UserResourcePermission academyRoomPermissions = userResourcePermissionRepository
				.findByUserIdAndResourceIdAndResource(unSharedUserId, academyRoomId, ResourceUtil.mar);
		List<UserResourcePermission> shareRemovalList = new ArrayList<>();
		if (academyRoomPermissions != null) {
			shareRemovalList.add(academyRoomPermissions);
			// check the user is having workshop sessison permissions for the provided
			// academy room
			List<UserResourcePermission> workshopSessionPermissions = userResourcePermissionRepository
					.findByUserIdAndResourceAndParentResourceId(unSharedUserId, ResourceUtil.mws, academyRoomId);
			if (workshopSessionPermissions.size() > 0) {
				shareRemovalList.addAll(workshopSessionPermissions);
			}
		}
		if (shareRemovalList.size() > 0) {
			userResourcePermissionRepository.deleteAll(shareRemovalList);
		}
	}

	@Transactional
	@Override
	public void unShareWorkshopSession(Long academyRoomId, Long workshopSessionId, Long unSharedUserId) {
		UserResourcePermission academyRoomPermissions = userResourcePermissionRepository
				.findByUserIdAndResourceIdAndResource(unSharedUserId, academyRoomId, ResourceUtil.mar);
		UserResourcePermission workshopSessionPermissions = userResourcePermissionRepository
				.findByUserIdAndResourceIdAndResource(unSharedUserId, workshopSessionId, ResourceUtil.mws);
		List<UserResourcePermission> shareRemovalList = new ArrayList<>();
		if (workshopSessionPermissions != null) {
			shareRemovalList.add(workshopSessionPermissions);
		}
		if (academyRoomPermissions != null
				&& !academyRoomPermissions.getActionsOrMethods().equals(PermissionUtil.all)) {
			// before removing academyRoom permission check whether any other
			// workshopSession has been shared or not for that particular academy room
			List<UserResourcePermission> otherWorshopSessionPermissions = userResourcePermissionRepository
					.findByUserIdAndResourceAndParentResourceIdAndResourceIdNot(unSharedUserId, ResourceUtil.mws,
							academyRoomId, workshopSessionId);
			if (otherWorshopSessionPermissions.size() == 0) {
				shareRemovalList.add(academyRoomPermissions);
			}
		}
		if (shareRemovalList.size() > 0) {
			userResourcePermissionRepository.deleteAll(shareRemovalList);
		}
	}

	@Transactional
	@Override
	public void shareMarkCardAcademyRoom(Long markCardId, Long academyRoomId, Long createdUserId,
			List<Long> sharedUserIds) {
		Set<Long> sharedIds = new HashSet<>(sharedUserIds);
		sharedUserIds.clear();
		sharedUserIds.addAll(sharedIds);
		for (Long sharedUserId : sharedUserIds) {
			UserResourcePermission mcUrp = userResourcePermissionRepository
					.findByUserIdAndResourceIdAndResource(sharedUserId, markCardId, ResourceUtil.mmc);
			if (mcUrp != null) {
				if (mcUrp.getActionsOrMethods().equals(PermissionUtil.get)) {
					// check whether same shared user exists for the selected workshopSession
					UserResourcePermission mcarUrp = userResourcePermissionRepository
							.findByUserIdAndResourceIdAndResourceAndParentResourceId(sharedUserId, academyRoomId,
									ResourceUtil.mmcar, markCardId);
					UserResourcePermission upo11 = new UserResourcePermission();
					if (mcarUrp != null) {
						upo11 = mcarUrp;
					}
					upo11.setActionsOrMethods(PermissionUtil.all);
//                    upo11.setIsUrl(false);
					upo11.setResource(ResourceUtil.mmcar);
					upo11.setResourceId(academyRoomId);
					upo11.setParentResourceId(markCardId);
					upo11.setUserId(sharedUserId);
					userResourcePermissionRepository.save(upo11);
				}
			} else {
				UserResourcePermission upo1 = new UserResourcePermission();
				upo1.setActionsOrMethods(PermissionUtil.get);
//                upo1.setIsUrl(false);
				upo1.setResource(ResourceUtil.mmc);
				upo1.setResourceId(markCardId);
				upo1.setUserId(sharedUserId);
				userResourcePermissionRepository.save(upo1);
				UserResourcePermission upo11 = new UserResourcePermission();
				upo11.setActionsOrMethods(PermissionUtil.all);
//                upo11.setIsUrl(false);
				upo11.setResource(ResourceUtil.mmcar);
				upo11.setResourceId(academyRoomId);
				upo11.setUserId(sharedUserId);
				upo11.setParentResourceId(markCardId);
				userResourcePermissionRepository.save(upo11);
			}
		}
	}

	@Transactional
	@Override
	public void unShareMarkCardAcademyRoom(Long markCardId, Long academyRoomId, Long unSharedUserId) {
		UserResourcePermission mcUrp = userResourcePermissionRepository
				.findByUserIdAndResourceIdAndResource(unSharedUserId, markCardId, ResourceUtil.mmc);
		UserResourcePermission mcarUrp = userResourcePermissionRepository
				.findByUserIdAndResourceIdAndResourceAndParentResourceId(unSharedUserId, academyRoomId,
						ResourceUtil.mmcar, markCardId);
		List<UserResourcePermission> shareRemovalList = new ArrayList<UserResourcePermission>();
		if (mcarUrp != null) {
			shareRemovalList.add(mcarUrp);
		}
		if (mcUrp != null && mcUrp.getActionsOrMethods().equals(PermissionUtil.get)) {
			// before removing markcard permission check whether any other
			// academy room has been shared or not for that particular mark card
			List<UserResourcePermission> otherMarkCardAcademyRoomPermissions = userResourcePermissionRepository
					.findByUserIdAndResourceAndParentResourceIdAndResourceIdNot(unSharedUserId, ResourceUtil.mmcar,
							markCardId, academyRoomId);
			if (otherMarkCardAcademyRoomPermissions.size() == 0) {
				shareRemovalList.add(mcUrp);
			}
		}
		if (shareRemovalList.size() > 0) {
			userResourcePermissionRepository.deleteAll(shareRemovalList);
		}
	}

	@Transactional
	@Override
	public Set<Long> getManagementMarkCardIds(long userId) {
		return userResourcePermissionRepository.getAcademyRoomResourceIds(userId, ResourceUtil.mmc);
	}

	@Transactional
	@Override
	public Set<Long> getManagementAcademyRoomMarkCardIds(long userId, Long markCardId) {
		return userResourcePermissionRepository.getMarkCardAcademyRoomResourceIds(userId, ResourceUtil.mmcar,
				markCardId);
	}

	@Transactional
	@Override
	public Set<Long> getFileFolderIds(Long userId, Long startupId) {
		return userResourcePermissionRepository.getFileFolderIds(userId, startupId, ResourceUtil.mff);
	}

	@Transactional
	@Override
	public void createFileFolder(Long userId, Long resourceId) {
		UserResourcePermission upo1 = new UserResourcePermission();
		upo1.setActionsOrMethods(PermissionUtil.all);
//        upo1.setIsUrl(false);
		upo1.setResource(ResourceUtil.mff);
		upo1.setResourceId(resourceId);
		upo1.setUserId(userId);
		userResourcePermissionRepository.save(upo1);
	}

	@Transactional
	@Override
	public void saveFileFolder(User user, FileFolder sf) {
		UserResourcePermission upo1 = new UserResourcePermission();
		upo1.setActionsOrMethods(PermissionUtil.all);
//        upo1.setIsUrl(false);
		upo1.setResource(ResourceUtil.mff);
		upo1.setResourceId(sf.getId());
		upo1.setUserId(user.getId());
		userResourcePermissionRepository.save(upo1);
	}

	@Transactional
	@Override
	public void deleteFileFolder(FileFolder fileFolder) {
		userResourcePermissionRepository.removeById(fileFolder.getId());
	}

	@Transactional
	@Override
	public void shareFileFolder(Long userId, FileFolder ff) {
		UserResourcePermission upo1 = new UserResourcePermission();
		upo1.setActionsOrMethods(PermissionUtil.all);
//        upo1.setIsUrl(false);
		upo1.setResource(ResourceUtil.mff);
		upo1.setResourceId(ff.getId());
		upo1.setUserId(userId);
		String p = ff.getParentFolder() + "/" + ff.getName();
		if (ff.getParentFolder().equals("/")) {
			p = "/" + ff.getName();
		}
		upo1.setUrlPath(p + "**");
		userResourcePermissionRepository.save(upo1);
	}

	@Transactional
	@Override
	public void shareFileFolderWithIntake(Long intakeId, FileFolder ff) {
		UserResourcePermission upo1 = new UserResourcePermission();
		upo1.setActionsOrMethods(PermissionUtil.all);
//        upo1.setIsUrl(false);
		upo1.setResource(ResourceUtil.mff);
		upo1.setResourceId(ff.getId());
		upo1.setIntakeProgramId(intakeId);
		String p = ff.getParentFolder() + "/" + ff.getId();
		if (ff.getParentFolder().equals("/")) {
			p = "/" + ff.getId();
		}
		upo1.setUrlPath(p + "**");
		userResourcePermissionRepository.save(upo1);
	}

	@Transactional
	@Override
	public void removeFileFolderWithIntake(FileFolder sf1) {
		userResourcePermissionRepository.removeFileFolderWithIntake(sf1.getId(), ResourceUtil.mff);
	}

}
