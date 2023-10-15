package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "email_template_attachment")
public class EmailTemplateAttachment extends BaseEntity {

    @Column(name = "file_name")
    private String name;

    @ManyToOne
    private EmailTemplate template;
}
