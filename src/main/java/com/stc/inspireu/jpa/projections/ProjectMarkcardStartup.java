package com.stc.inspireu.jpa.projections;

public class ProjectMarkcardStartup {

	private Long startupId;

	private String startupName;

	public ProjectMarkcardStartup(Long startupId, String startupName) {
		super();
		this.startupId = startupId;
		this.startupName = startupName;
	}

	public Long getStartupId() {
		return startupId;
	}

	public void setStartupId(Long startupId) {
		this.startupId = startupId;
	}

	public String getStartupName() {
		return startupName;
	}

	public void setStartupName(String startupName) {
		this.startupName = startupName;
	}

}
