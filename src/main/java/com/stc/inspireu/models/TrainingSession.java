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
@Table(name = "training_sessions")
public class TrainingSession extends BaseEntity {

    @Column(nullable = false)
    private String meetingName;

    @Column
    private Boolean willOnline;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "workshopSessionId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private WorkshopSession workshopSession;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdUser;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IntakeProgram intakeProgram;

    @Column
    private String meetingRoomOrLink;

    @Column
    private String description;

    @Column
    private Date sessionStart;

    @Column
    private Date sessionEnd;

    @Column
    private String sessionStartTime;

    @Column
    private String sessionEndTime;

    @Column
    private Boolean isRecurring;
}
