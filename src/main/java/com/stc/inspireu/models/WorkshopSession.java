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
@Table(name = "workshop_sessions")
public class WorkshopSession extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private Integer percentageFinish;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "academyRoomId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AcademyRoom academyRoom;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "startupId")
    private Startup startup;

    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "refWorkshopSession")
    private WorkshopSession refWorkshopSession;

    @Column
    private String status;

    @Column
    private String statusPublish;

    @Column
    private Date sessionStart;

    @Column
    private Date sessionEnd;

    @Column
    private Boolean willOnline;

    @Column
    private String meetingRoomOrLink;

    public WorkshopSession(String name, String description, int percentageFinish, AcademyRoom academyRoom) {
        super();
        this.name = name;
        this.description = description;
        this.percentageFinish = percentageFinish;
        this.academyRoom = academyRoom;
    }
}
