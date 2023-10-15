package com.stc.inspireu.repositories;

import com.stc.inspireu.models.ContactUs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactUsRepository extends JpaRepository<ContactUs, Long> {

    Page<ContactUs> findAllBySubjectOrderByCreatedOnDesc(String subject, Pageable pageable);

    Page<ContactUs> findAllByOrderByCreatedOnDesc(Pageable pageable);
}
