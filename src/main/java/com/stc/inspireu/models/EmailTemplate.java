package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "email_templates")
public class EmailTemplate extends BaseEntity {

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(name = "language", columnDefinition = "varchar(20) default 'en'")
    private String language;

    @Column()
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String header;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String footer;

    @Column
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId")
    private IntakeProgram intakeProgram;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emailTemplateTypesId")
    private EmailTemplatesTypes emailTemplatesTypes;

    @Column(nullable = false)
    private String emailContentType;

    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private Set<EmailTemplateAttachment> attachments = new HashSet<>();

}
