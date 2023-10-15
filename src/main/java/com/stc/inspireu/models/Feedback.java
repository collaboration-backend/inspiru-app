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
@Table(name = "feedbacks")
public class Feedback extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String status = Constant.NOT_SUBMITTED.toString();

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "forStartupId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Startup forStartup;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "workshopSessionId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private WorkshopSession workshopSession;

    @Column(columnDefinition = "TEXT")
    private String jsonForm;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "refFormTemplateId")
    private Feedback refFormTemplate;

    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "refFeedback")
    private Feedback refFeedback;

    @Column
    private Date submitDate;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "submittedUserId")
    private User submittedUser;
}
