package com.stc.inspireu.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "assessment_evaluation_forms")
public class AssessmentEvaluationForm extends BaseEntity{

	@Column(nullable = false)
	private String formName;

	@Column
	private String status;

	@Column
	private String description;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String jsonForm;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "createdUserId", nullable = false)
	private User createdUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "publishedBy")
	private User publishedUser;

	@Column
	private Date publishedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "intakeProgramId")
	private IntakeProgram intakeProgram;

	@Column
	private String evaluationPhase;
}
