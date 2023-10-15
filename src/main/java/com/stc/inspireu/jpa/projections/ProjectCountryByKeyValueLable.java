package com.stc.inspireu.jpa.projections;

import org.springframework.beans.factory.annotation.Value;

public interface ProjectCountryByKeyValueLable {
	@Value("#{target.countryCode}")
	String getKey();

	@Value("#{target.countryCode}")
	String getValue();

	@Value("#{target.countryName}")
	String getLabel();

    @Value("#{target.countryNameAr}")
    String getLabelAr();
}
