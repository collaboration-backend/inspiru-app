package com.stc.inspireu.repositories;

import com.stc.inspireu.models.EmailTemplate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface EmailTemplateRepository extends PagingAndSortingRepository<EmailTemplate, Long> {
	Page<EmailTemplate> findByNameContainingIgnoreCaseAndEmailContentTypeAndEmailTemplatesTypes_Id(String name,
                                                                                                   String emailContentType, Long emailTemplateTypesId, Pageable paging);

	Page<EmailTemplate> findByEmailContentTypeAndEmailTemplatesTypes_Id(String emailContentType,
                                                                        Long emailTemplateTypesId, Pageable paging);

	Page<EmailTemplate> findByEmailTemplatesTypes_IdAndStatus(Long emailTemplatesTypeId, String status, Pageable paging);

    /**
     * Method to fetch templates by type and template key
     * @param emailTemplatesTypeId
     * @param key
     * @param paging
     * @return
     */
    Page<EmailTemplate> findByEmailTemplatesTypes_IdAndKey(Long emailTemplatesTypeId, String key, Pageable paging);

    @Query(value = "from EmailTemplate where key=:key and status='ACTIVE' and language=:language")
    Optional<EmailTemplate> findByKeyAndLanguage(String key,String language);


    /**
     * Method to find a template by key and name
     * @param key
     * @param name
     * @return
     */
    EmailTemplate findByKeyAndNameIgnoreCase(String key, String name);

    @Modifying
    @Query(value = "update email_templates set status='INACTIVE' where key=:key and emailtemplatetypesid=:templateTypeId and language=:language",nativeQuery = true)
    void inactivateAllTemplatesByKeyAndTypeAndLanguage(String key, Long templateTypeId,String language);

    Optional<EmailTemplate> findByIdAndKey(Long id, String key);
}
