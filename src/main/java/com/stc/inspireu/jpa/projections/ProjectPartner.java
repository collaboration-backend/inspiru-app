package com.stc.inspireu.jpa.projections;

import org.springframework.beans.factory.annotation.Value;

public interface ProjectPartner {
	Long getId();
	
	String getName();
	
	String getDetails();
	
	String getPhoneNumber();

	String getPhoneDialCode();

	String getPhoneCountryCodeIso2();
	
	String getEmail();
	
	String getLink();
	
	String getLogo();
	
}
