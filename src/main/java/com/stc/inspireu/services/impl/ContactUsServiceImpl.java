package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.MailMetadata;
import com.stc.inspireu.dtos.ContactUsSubjectDTO;
import com.stc.inspireu.dtos.ContactUsSubjectPublicDTO;
import com.stc.inspireu.dtos.ContactUsSubmissionDTO;
import com.stc.inspireu.dtos.ContactUsSubmissionResultDTO;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.mappers.ContactUsMapper;
import com.stc.inspireu.mappers.ContactUsSubjectMapper;
import com.stc.inspireu.models.ContactUs;
import com.stc.inspireu.models.ContactUsSubject;
import com.stc.inspireu.repositories.ContactUsRepository;
import com.stc.inspireu.repositories.ContactUsSubjectRepository;
import com.stc.inspireu.services.ContactUsService;
import com.stc.inspireu.utils.EmailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactUsServiceImpl implements ContactUsService {

    private final ContactUsSubjectRepository repository;
    private final ContactUsRepository contactUsRepository;
    private final EmailUtil emailUtil;
    private final ContactUsSubjectMapper contactUsSubjectMapper;
    private final ContactUsMapper contactUsMapper;

    @Transactional
    @Override
    public void save(List<ContactUsSubjectDTO> contactUsSubjectDTOS) {
        Set<String> subjectsSet = contactUsSubjectDTOS.stream()
            .map(ContactUsSubjectDTO::getSubject).collect(Collectors.toSet());
        if (subjectsSet.size() < contactUsSubjectDTOS.size())
            throw new CustomRunTimeException("Duplicate subjects found in the request");
        contactUsSubjectDTOS.forEach(dto -> {
            String[] emails = dto.getEmails().split(",");
            if (emails.length > 10)
                throw new CustomRunTimeException("More than 10 email are not allowed");
            for (String email : emails) {
                if (!Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$").matcher(email).matches())
                    throw new CustomRunTimeException("'" + email + "' is not a valid email");
            }
        });
        List<ContactUsSubject> subjects = contactUsSubjectDTOS.stream()
            .map(dto -> new ContactUsSubject(dto.getSubject(), dto.getEmails()))
            .collect(Collectors.toList());
        this.repository.deleteAll();
        if (!subjects.isEmpty())
            this.repository.saveAll(subjects);
    }

    @Override
    public List<ContactUsSubjectDTO> findAll() {
        return this.repository.findAll(Sort.by("subject"))
            .stream()
            .map(contactUsSubjectMapper::toContactUsSubjectDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ContactUsSubjectPublicDTO> findSubjects() {
        return this.repository.findAll(Sort.by("subject"))
            .stream()
            .map(sub -> new ContactUsSubjectPublicDTO(sub.getId(), sub.getSubject()))
            .collect(Collectors.toList());
    }

    @Override
    public void submit(ContactUsSubmissionDTO submissionDTO) throws MessagingException {
        ContactUsSubject subject = this.repository.findById(submissionDTO.getSubject())
            .orElseThrow(() -> ItemNotFoundException.builder("Subject").build());
        ContactUs contactUs = new ContactUs();
        contactUs.setSubject(subject.getSubject());
        contactUs.setMessage(submissionDTO.getMessage());
        contactUs.setName(submissionDTO.getName());
        contactUs.setMobile(submissionDTO.getMobile());
        contactUs.setEmail(submissionDTO.getEmail());
        contactUsRepository.save(contactUs);
        MailMetadata mailMetadata = new MailMetadata();
        mailMetadata.setTemplateString("<html>\n" +
            "<body>\n" +
            "    <table>\n" +
            "        <tbody>\n" +
            "            <tr>\n" +
            "                <td>Name:</td>\n" +
            "                <td>" + submissionDTO.getName() + "</td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "                <td>Email:</td>\n" +
            "                <td>" + submissionDTO.getEmail() + "</td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "                <td>Mobile:</td>\n" +
            "                <td>" + submissionDTO.getMobile() + "</td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "                <td>Message:</td>\n" +
            "                <td>" + submissionDTO.getMessage() + "</td>\n" +
            "            </tr>\n" +
            "        </tbody>\n" +
            "    </table>\n" +
            "</body>\n" +
            "</html>");
        mailMetadata.setSubject("New contact form submission received: " + subject.getSubject());
        String[] email = subject.getEmails().split(",");
        mailMetadata.setTos(new HashSet<>(Arrays.asList(email)));
        emailUtil.sendEmail(mailMetadata);
    }

    @Override
    public Page<ContactUsSubmissionResultDTO> findAllSubmissions(String subject, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContactUs> submissions;
        if (Objects.isNull(subject) || subject.isEmpty())
            submissions = this.contactUsRepository.findAllByOrderByCreatedOnDesc(pageable);
        else
            submissions = this.contactUsRepository.findAllBySubjectOrderByCreatedOnDesc(subject, pageable);
        return new PageImpl<>(submissions.map(contactUsMapper::toContactUsSubmissionResultDTO).toList(), pageable, submissions.getTotalElements());
    }

}
