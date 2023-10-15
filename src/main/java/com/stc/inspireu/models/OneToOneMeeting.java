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
@Table(name = "one_to_one_meetings")
public class OneToOneMeeting extends BaseEntity {

    @Column(nullable = false)
    private String meetingName;

    @Column
    private Boolean willOnline;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "trainerId")
    private User trainer;

    @Column
    private String invitationStatus;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "inviteAcceptedUserId")
    private User inviteAcceptedUser;

    @Column
    private String meetingLink;

    @Column(columnDefinition = "TEXT")
    private String meetingHostLink;

    @Column
    private String description;

    @Column
    private Date sessionStart;

    @Column
    private Date sessionEnd;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "startupId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Startup startup;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column
    private String meetingRoom;

    @Column
    private Date sessionPreviousStart;

    @Column
    private Date sessionPreviousEnd;
}
