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
@Table(name = "notifications", indexes = {@Index(name = "keywords_index", columnList = "keywords", unique = false)})
public class Notification extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column
    private String keywords;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "targetStartupId")
    private Startup targetStartup;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "sourceUserId")
    private User sourceUser;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "targetUserId")
    private User targetUser;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId")
    private IntakeProgram intakeProgram;

    @Column()
    private boolean showToAdmin;

    @Column
    private Date targetDate;
}
