package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "startups")
public class Startup extends BaseEntity {

	@Column(nullable = false)
	private String startupName;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name = "intakeProgramId", nullable = false)
	private IntakeProgram intakeProgram;

	@Column
	private String jobTitle;

	@Column
	private String companyProfile;

	@Column
	private String companyPic;

	@Column
	private String companyDescription;

	@Column
	private String revenueModel;

	@Column
	private String segment;

	@Column
	private String status;

	@Column
	private String competitor;

	@Column
	private Boolean isReal;

	@Column(columnDefinition = "TEXT")
	private String profileCardJsonForm;

	@Column(columnDefinition = "TEXT")
	private String registrationJsonForm;

	@Column(columnDefinition = "TEXT")
	private String profileInfoJson;

}
