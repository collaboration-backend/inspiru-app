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
@Table(name = "judge_calendar")
public class JudgeCalendar extends BaseEntity {
    @Column
    private Date sessionStart;

    @Column
    private Date sessionEnd;

    @Column(nullable = false)
    private String phase;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IntakeProgram intakeProgram;

    @Column(nullable = false)
    String email;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "judgeId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User judge;
}
