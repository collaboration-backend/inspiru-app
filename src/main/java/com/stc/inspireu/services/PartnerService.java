package com.stc.inspireu.services;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostPartnerDto;

public interface PartnerService {

	ResponseEntity<?> getPartners(CurrentUserObject currentUserObject, Pageable paging);

	ResponseEntity<?> postPartner(CurrentUserObject currentUserObject, PostPartnerDto postPartnerDto);

}
