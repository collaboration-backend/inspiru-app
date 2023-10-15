package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "contact_us_submissions")
public class ContactUs extends BaseEntity {

    private String name;

    private String email;

    private String mobile;

    @Column(columnDefinition = "text")
    private String message;

    private String subject;

}
