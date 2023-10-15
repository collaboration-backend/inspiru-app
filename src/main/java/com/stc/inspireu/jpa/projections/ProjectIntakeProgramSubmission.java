package com.stc.inspireu.jpa.projections;

public interface ProjectIntakeProgramSubmission {
	Long getId();

	String getEmail();

	String getPhase();

	ProjectIntakeProgram getIntakeProgram();

	interface ProjectIntakeProgram {
		Long getId();

		String getProgramName();
	}
}
