package com.stc.inspireu.jpa.projections;

import java.io.Serializable;

public class ProjectResourceId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long resourceId;

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
