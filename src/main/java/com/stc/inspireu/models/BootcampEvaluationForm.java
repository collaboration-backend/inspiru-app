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
@Table(name = "bootcamp_evaluation_forms")
public class BootcampEvaluationForm extends BaseEntity {

    @Column(nullable = false)
    private String formName;

    @Column
    private String status;

    @Column
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String jsonForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId", nullable = false)
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publishedBy")
    private User publishedUser;

    @Column
    private Date publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId")
    private IntakeProgram intakeProgram;

    @Column
    private String evaluationPhase;

}
