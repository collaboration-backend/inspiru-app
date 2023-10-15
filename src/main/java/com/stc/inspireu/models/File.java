package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "files")
public class File extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    @Column
    private String status;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId")
    private IntakeProgram intakeProgram;
}
