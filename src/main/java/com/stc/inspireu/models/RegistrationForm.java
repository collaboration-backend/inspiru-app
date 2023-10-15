package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "registration_forms")
public class RegistrationForm extends BaseEntity {

    @Column(nullable = false)
    private String formName;

    @Column
    private String status;

    @Column(columnDefinition = "TEXT", name = "rich_text_description")
    private String description;

    @Column(name = "banner_image")
    private String banner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publishedUserId")
    private User publishedUser;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String jsonForm;


    @Column
    private Date dueDate;

    @Column()
    private String language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId")
    private IntakeProgram intakeProgram;

    @Column
    private Date publishedAt;
}
