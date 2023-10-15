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
@Table(name = "open_events")
public class OpenEvent extends BaseEntity {

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private String eventPhase;

    @Column(nullable = false)
    private String timezone;

    @Column
    private Date sessionStart;

    @Column
    private Date sessionEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IntakeProgram intakeProgram;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdUser;

    @Column
    private String meetingRoom;

    @Column
    private String meetingLink;

    @Column(columnDefinition = "TEXT")
    private String meetingHostLink;

    @Column(columnDefinition = "TEXT")
    private String descriptionSection;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private String schedulingMethod;

    @Column(columnDefinition = "TEXT")
    private String jsonEventInfo;
}
