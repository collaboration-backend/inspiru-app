package com.stc.inspireu.jpa.projections;

import org.springframework.beans.factory.annotation.Value;

public interface ProjectRevenueModelByKeyValueLabel {
	@Value("#{target.id}")
	Long getKey();

	@Value("#{target.id}")
	Long getValue();

	@Value("#{target.revenueModel}")
	String getLabel();
}
