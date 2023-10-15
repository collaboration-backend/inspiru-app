package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostPartnerDto;
import com.stc.inspireu.jpa.projections.ProjectPartner;
import com.stc.inspireu.models.Partner;
import com.stc.inspireu.repositories.PartnerRepository;
import com.stc.inspireu.repositories.UserRepository;
import com.stc.inspireu.services.PartnerService;
import com.stc.inspireu.utils.FileAdapter;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.invoke.MethodHandles;

@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final FileAdapter fileAdapter;

    @Transactional
    @Override
    public ResponseEntity<?> getPartners(CurrentUserObject currentUserObject, Pageable paging) {
        Page<ProjectPartner> ls = partnerRepository.findByUser_Id(currentUserObject.getUserId(), paging);
        return ResponseWrapper.response(ls);
    }

    @Transactional
    @Override
    public ResponseEntity<?> postPartner(CurrentUserObject currentUserObject,
                                         PostPartnerDto postPartnerDto) {
        return userRepository.findById(currentUserObject.getUserId())
            .map(user -> {
                Partner partner = new Partner();
                partner.setDetails(postPartnerDto.getDetails());
                partner.setEmail(postPartnerDto.getEmail());
                partner.setLink(postPartnerDto.getLink());
                partner.setName(postPartnerDto.getName());
                partner.setPhoneCountryCodeIso2(postPartnerDto.getPhoneCountryCodeIso2());
                partner.setPhoneDialCode(postPartnerDto.getPhoneDialCode());
                partner.setPhoneNumber(postPartnerDto.getPhoneNumber());
                partner.setUser(user);
                Partner pt = partnerRepository.save(partner);
                if (postPartnerDto.getLogo() != null) {
                    String logoPicPath = fileAdapter.savePartnerLogo(pt.getId(), postPartnerDto.getLogo());
                    pt.setLogo(logoPicPath);
                    partnerRepository.save(partner);
                }
                return ResponseWrapper.response(null);
            }).orElse(ResponseWrapper.response(null));
    }
}
