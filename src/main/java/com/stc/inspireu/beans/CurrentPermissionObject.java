package com.stc.inspireu.beans;

import java.io.Serializable;
import java.util.Set;

public class CurrentPermissionObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long roleId;

	private String roleName;

	private Set<Long> academyRoomIds;

	private Set<Long> workshopSessionIds;

	private Long resourceId;

	public CurrentPermissionObject() {
		super();
	}

	public CurrentPermissionObject(Long roleId, String roleName, Set<Long> academyRoomIds, Set<Long> workshopSessionIds,
			Long resourceId) {
		super();
		this.roleId = roleId;
		this.roleName = roleName;
		this.academyRoomIds = academyRoomIds;
		this.workshopSessionIds = workshopSessionIds;
		this.resourceId = resourceId;
	}

	public CurrentPermissionObject(Long roleId, String roleName) {
		super();
		this.roleId = roleId;
		this.roleName = roleName;
	}

	public CurrentPermissionObject(Long roleId, String roleName, Set<Long> academyRoomIds) {
		super();
		this.roleId = roleId;
		this.roleName = roleName;
		this.academyRoomIds = academyRoomIds;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Set<Long> getAcademyRoomIds() {
		return academyRoomIds;
	}

	public void setAcademyRoomIds(Set<Long> academyRoomIds) {
		this.academyRoomIds = academyRoomIds;
	}

	public Set<Long> getWorkshopSessionIds() {
		return workshopSessionIds;
	}

	public void setWorkshopSessionIds(Set<Long> workshopSessionIds) {
		this.workshopSessionIds = workshopSessionIds;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
