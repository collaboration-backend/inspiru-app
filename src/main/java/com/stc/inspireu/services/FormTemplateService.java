package com.stc.inspireu.services;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostBootCampFormDto;
import com.stc.inspireu.dtos.PostEvaluationFormTemplate;
import com.stc.inspireu.dtos.PostProfileCardDto;
import com.stc.inspireu.dtos.RegistrationFormDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;

public interface FormTemplateService {

	ResponseEntity<?> createEvaluationFormTemplate(CurrentUserObject currentUserObject,
			@Valid PostEvaluationFormTemplate postEvaluationFormTemplate, String string);

    ResponseEntity<?> createScreeningEvaluationFormTemplate(CurrentUserObject currentUserObject,
                                                            PostEvaluationFormTemplate postEvaluationFormTemplate, String string);

    ResponseEntity<?> updateScreeningEvaluationFormTemplate(CurrentUserObject currentUserObject,
                                                            PostEvaluationFormTemplate postEvaluationFormTemplate, Long formId);

	ResponseEntity<?> updateEvaluationFormTemplate(CurrentUserObject currentUserObject,
			PostEvaluationFormTemplate postEvaluationFormTemplate, Long id);

	ResponseEntity<?> updateEvaluationFormTemplateStatus(CurrentUserObject currentUserObject, String status, Long id);

    ResponseEntity<?> updateScreeningEvaluationFormTemplateStatus(CurrentUserObject currentUserObject, String status, Long id);

	ResponseEntity<?> createRegistrationForm(CurrentUserObject currentUserObject,
			@Valid RegistrationFormDto registrationFormDto, String string);

	ResponseEntity<?> updateRegistrationForm(CurrentUserObject currentUserObject,
			RegistrationFormDto registrationFormDto, Long id);

	ResponseEntity<?> updateRegistrationFormStatus(CurrentUserObject currentUserObject, String status, Long id);

	ResponseEntity<?> createBootCampForm(CurrentUserObject currentUserObject, PostBootCampFormDto bootCampFormDto,
			String string);

	ResponseEntity<?> updateBootCampForm(CurrentUserObject currentUserObject, PostBootCampFormDto bootCampFormDto,
			Long id);

	ResponseEntity<?> updateBootCampFormStatus(CurrentUserObject currentUserObject, String status, Long id);

	ResponseEntity<?> getBootCampForms(CurrentUserObject currentUserObject, String name, Pageable paging);

	ResponseEntity<?> createProfileCard(CurrentUserObject currentUserObject, PostProfileCardDto postProfileCardDto,
			String status);

	ResponseEntity<?> getProfileCards(CurrentUserObject currentUserObject, String name, Pageable paging);

	ResponseEntity<?> updateProfileCard(CurrentUserObject currentUserObject, PostProfileCardDto profileCardDto,
			Long id);

	ResponseEntity<?> updateProfileCardStatus(CurrentUserObject currentUserObject, String status, Long id);

	ResponseEntity<?> getRegistrationForm(CurrentUserObject currentUserObject, Long registrationFormId);

	ResponseEntity<?> getRegistrationForms(CurrentUserObject currentUserObject, String name, Pageable paging);

	ResponseEntity<?> getBootCampForm(CurrentUserObject currentUserObject, Long bootCampFormId);

	ResponseEntity<?> getEvaluationFormTemplates(CurrentUserObject currentUserObject, Long evaluationFormId);

    ResponseEntity<?> getScreeningEvaluationFormTemplates(CurrentUserObject currentUserObject, Long evaluationFormId);

	ResponseEntity<?> getEvaluationTemplates(CurrentUserObject currentUserObject, String name, Pageable paging);

    ResponseEntity<?> getScreeningEvaluationTemplates(CurrentUserObject currentUserObject, String name, Pageable paging);

	ResponseEntity<?> getProfileCard(CurrentUserObject currentUserObject, Long profileCardId);

	ResponseEntity<?> deleteProfileCard(CurrentUserObject currentUserObject, Long profileCardId);

	ResponseEntity<?> deleteBootCampForm(CurrentUserObject currentUserObject, Long bootCampFormId);

	ResponseEntity<?> deleteRegistrationForm(CurrentUserObject currentUserObject, Long registrationFormId);

    ResponseEntity<?> deleteScreeningForm(CurrentUserObject currentUserObject, Long formId);

	ResponseEntity<Object> deleteEvaluationFormTemplates(CurrentUserObject currentUserObject, Long evaluationFormId);
}
