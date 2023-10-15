package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "contact_us_subjects")
public class ContactUsSubject extends BaseEntity {

    private String subject;

    @Column(name = "emails", columnDefinition = "text")
    private String emails;

    public ContactUsSubject(String subject, String emails) {
        this.subject = subject;
        this.emails = emails;
    }
}
