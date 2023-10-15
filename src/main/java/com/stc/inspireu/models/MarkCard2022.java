package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "markcards")
public class MarkCard2022 extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IntakeProgram intakeProgram;
}
