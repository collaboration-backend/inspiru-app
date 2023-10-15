package com.stc.inspireu.jpa.projections;

import org.springframework.beans.factory.annotation.Value;

public interface ProjectStartupIdAndAmountPaid {

	Float getAmountPaid();

	@Value("#{target.startup.id}")
	String getStartupId();
}
