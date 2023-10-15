package com.stc.inspireu.jpa.projections;

import org.springframework.beans.factory.annotation.Value;

public interface ProjectCityByKeyValueLable {
	@Value("#{target.id}")
	Long getKey();

	@Value("#{target.id}")
	Long getValue();

	@Value("#{target.cityName}")
	String getLabel();

    @Value("#{target.cityNameAr}")
    String getLabelAr();
}
