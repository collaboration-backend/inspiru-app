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
@Table(name = "academy_rooms")
public class AcademyRoom extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private String status;

    @Column
    private String statusPublish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "startupId")
    private Startup startup;

    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "refAcademyRoom")
    private AcademyRoom refAcademyRoom;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId")
    private IntakeProgram intakeProgram;

    @Column
    private Date sessionStart;

    @Column
    private Date sessionEnd;

    @Column
    private Boolean markCardNotified;

    public AcademyRoom(String name, String des, String status, String statusPublish) {
        super();
        this.name = name;
        this.description = des;
        this.status = status;
        this.statusPublish = statusPublish;
    }
}
