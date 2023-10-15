package com.stc.inspireu.jpa.projections;

import org.springframework.beans.factory.annotation.Value;

public interface ProjectSegmentByKeyValueLabel {
	@Value("#{target.id}")
	Long getKey();

	@Value("#{target.id}")
	Long getValue();

	@Value("#{target.segment}")
	String getLabel();
}
