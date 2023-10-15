package com.stc.inspireu.services;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.EvaluationSummaryDto;

public interface CommonService {

	List<EvaluationSummaryDto> getES();

	ResponseEntity<?> pc();

	ResponseEntity<?> dropdownStartups(CurrentUserObject currentUserObject);

	ResponseEntity<?> dropdownFormTemplates(CurrentUserObject currentUserObject, String formTemplateType,
			String string);

	void dailyMidnight();

	ResponseEntity<?> getFF();

	ResponseEntity<?> getURP();

	ResponseEntity<?> getOE();

}
