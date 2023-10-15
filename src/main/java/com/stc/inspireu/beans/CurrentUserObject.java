package com.stc.inspireu.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CurrentUserObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long userId;

	private String email;

	private Map<String, Object> metaData;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Map<String, Object> getMetaData() {
		return metaData;
	}

	public void setMetaData(Map<String, Object> metaData) {
		this.metaData = metaData;
	}

	public CurrentUserObject() {
		super();
		this.metaData = new HashMap<String, Object>();
	}

}
