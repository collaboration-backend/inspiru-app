package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "milestones")
public class Milestone extends BaseEntity {

    @Column(nullable = false)
    private String milestoneName;

    @Column(nullable = false)
    private Integer milestoneNumber;

    @Column

    private String status;

    @Column
    private String applicable;

    @Column
    private String milestoneCondition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId")
    private IntakeProgram intakeProgram;
}
