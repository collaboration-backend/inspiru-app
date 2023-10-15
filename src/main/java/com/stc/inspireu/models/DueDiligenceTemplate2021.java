package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "due_diligence_templates")
public class DueDiligenceTemplate2021 extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private String status;

    @Column(columnDefinition = "TEXT")
    private String jsonForm;

    @Column
    private boolean isActive;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IntakeProgram intakeProgram;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "startupId")
    private Startup startup;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "refDueDiligenceTemplate2021")
    private DueDiligenceTemplate2021 refDueDiligenceTemplate2021;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "submittedUserId")
    private User submittedUser;

    @Column
    private boolean isArchive = false;

    @Column
    private String reviewComment;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewUserId")
    private User reviewUser;

    @Column
    private Date reviewedOn;

    @Column
    private Date submittedOn;

    @Column(columnDefinition = "TEXT")
    private String jsonFieldActions;
}
