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
@Table(name = "workshop_session_submissions")
public class WorkshopSessionSubmissions extends BaseEntity {

    @Column(nullable = false)
    private String submittedFileName;

    @Column
    private String fileType;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "startupId")
    private Startup startup;

    @Column
    private Long metaDataId;

    @Column(nullable = false)
    private String status;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "submittedUserId")
    private User submittedUser;

    @Column
    private Date submittedOn;

    @Column
    private Boolean isLate;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "workshopSessionId")
    private WorkshopSession workshopSession;

    @Column
    private Long metaDataParentId;
}
