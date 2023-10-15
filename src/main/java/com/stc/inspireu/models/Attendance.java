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
@Table(name = "attendance")
public class Attendance extends BaseEntity {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "oneToOneMeetingId")
    private OneToOneMeeting oneToOneMeeting;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private User member;

    @Temporal(TemporalType.DATE)
    @Column
    private Date attendanceDate;

    @Temporal(TemporalType.TIME)
    @Column
    private Date sessionStart;

    @Temporal(TemporalType.TIME)
    @Column
    private Date sessionEnd;

    @Column
    private Boolean isPresent;

    @Column
    private Boolean isLate;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "trainingSessionId")
    private TrainingSession trainingSession;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "slotId")
    private Slot slot;
}
