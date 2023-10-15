package com.stc.inspireu.jpa.projections;

import java.io.Serializable;
import java.util.Set;

import com.stc.inspireu.models.Role;

public class ProjectUserRoleObject implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private String email;

	private Set<Role> roles;

	public ProjectUserRoleObject(Long id, String email, Set<Role> roles) {
		super();
		this.id = id;
		this.email = email;
		this.roles = roles;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
