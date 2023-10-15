package com.stc.inspireu.jpa.projections;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;

public interface ProjectNotification {
	Long getId();

	String getMessage();

	String getKeywords();

	@Value("#{target.targetStartup != null ? target.targetStartup.id : null}")
	Long getTargetStartupId();

	@Value("#{target.sourceUser != null ? target.sourceUser.id : null}")
	Long getSourceUserId();

	@Value("#{target.targetUser != null ? target.targetUser.id : null}")
	Long getTargetUserId();

	@Value("#{target.intakeProgram != null ? target.intakeProgram.id : null}")
	Long getIntakeProgramId();

	@Value("#{target.showToAdmin}")
	Boolean getShowToAdmin();

    LocalDateTime getCreatedOn();

	Date getTargetDate();

}
