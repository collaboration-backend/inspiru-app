package com.stc.inspireu.services;

import com.stc.inspireu.dtos.ContactUsSubjectDTO;
import com.stc.inspireu.dtos.ContactUsSubjectPublicDTO;
import com.stc.inspireu.dtos.ContactUsSubmissionDTO;
import com.stc.inspireu.dtos.ContactUsSubmissionResultDTO;
import org.springframework.data.domain.Page;

import javax.mail.MessagingException;
import java.util.List;

public interface ContactUsService {

    void save(List<ContactUsSubjectDTO> contactUsSubjectDTOS);

    List<ContactUsSubjectDTO> findAll();

    List<ContactUsSubjectPublicDTO> findSubjects();

    void submit(ContactUsSubmissionDTO submissionDTO) throws MessagingException;

    Page<ContactUsSubmissionResultDTO> findAllSubmissions(String subject, int page, int size);
}
