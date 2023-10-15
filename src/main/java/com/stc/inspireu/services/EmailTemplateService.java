package com.stc.inspireu.services;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.GetEmailTemplateDto2;
import com.stc.inspireu.dtos.PostEmailTemplatesDto;
import com.stc.inspireu.dtos.PostEmailTemplatesTypeDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface EmailTemplateService {
	ResponseEntity<?> getEmailTypes(CurrentUserObject userObject, String name, Pageable page);

	ResponseEntity<?> postEmailTemplateTypes(PostEmailTemplatesTypeDto postEmailTemplatesTypeDto,
			CurrentUserObject userObject);

	ResponseEntity<?> deleteEmailTemplateType(Long Id, CurrentUserObject userObject);

	ResponseEntity<?> getEmail(CurrentUserObject userObject, String name, String emailContentType,
			Long emailTemplateTypeId, Pageable page);

	ResponseEntity<?> postEmailTemplate(PostEmailTemplatesDto postEmailTemplateDto, CurrentUserObject userObject);

	ResponseEntity<?> updateEmailTemplate(PostEmailTemplatesDto postEmailTemplateDto, Long emailTemplateId,
                                          CurrentUserObject userObject);

	ResponseEntity<?> deleteEmailTemplate(Long Id, CurrentUserObject userObject);

	ResponseEntity<?> getEmailById(Long id, CurrentUserObject userObject);

	ResponseEntity<?> getTemplatesByType(CurrentUserObject currentUserObject, Long emailTemplatesTypeId, String name,
			Pageable paging);

    ResponseEntity<?> getTemplatesByTypeAndKey(CurrentUserObject currentUserObject, Long emailTemplatesTypeId, String key,
                                         Pageable paging);

    ResponseEntity<?> activateTemplate(Long emailTemplatesTypeId,String key,Long templateId);

    Optional<GetEmailTemplateDto2> findActiveEmailTemplateByKeyAndLanguage(String key,String language);
}
