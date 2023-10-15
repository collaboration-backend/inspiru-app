package com.stc.inspireu.models;

import com.stc.inspireu.enums.Constant;
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
@Table(name = "assignments")
public class Assignment extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Date dueDate;

    @Column
    private Date reviwed1On;

    @Column
    private Date reviwed2On;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "submittedUserId")
    private User submittedUser;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "submittedStartupId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Startup submittedStartup;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "workshopSessionId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private WorkshopSession workshopSession;

    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "refAssignment")
    private Assignment refAssignment;

    @Column
    private String review1Status = Constant.PENDING.toString();

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "review1UserId")
    private User review1User;

    @Column
    private String review2Status = Constant.PENDING.toString();

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "review2UserId")
    private User review2User;

    @Column
    private Date submitDate;

    @Column
    private String review1Comment;

    @Column
    private String review2Comment;

}
