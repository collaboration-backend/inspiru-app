package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity()
@Setter
@Getter
@NoArgsConstructor
@Table(name = "attendance_workshops")
public class Attendance2022 extends BaseEntity {

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IntakeProgram intakeProgram;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "startupId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Startup startup;

    @Column
    private Integer percentage;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "academyRoomId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AcademyRoom academyRoom;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "workshopSessionId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private WorkshopSession workshopSession;

}
