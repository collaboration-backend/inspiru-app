package com.stc.inspireu.jpa.projections;

import java.util.Date;

public interface ProjectIdNameStatusDueDateSubmittedOnSurvey {
	Long getId();

	String getName();

	String getStatus();

	Date getDueDate();

	Date getSubmittedOn();
}
