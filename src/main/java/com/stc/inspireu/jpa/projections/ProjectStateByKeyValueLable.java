package com.stc.inspireu.jpa.projections;

import org.springframework.beans.factory.annotation.Value;

public interface ProjectStateByKeyValueLable {
	@Value("#{target.stateCode}")
	String getKey();

	@Value("#{target.stateCode}")
	String getValue();

	@Value("#{target.stateName}")
	String getLabel();
}
