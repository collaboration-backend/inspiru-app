package com.stc.inspireu.jpa.projections;

import java.io.Serializable;

public class ProjectIdAndTypeDueDiligenceFileType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private String fileType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
