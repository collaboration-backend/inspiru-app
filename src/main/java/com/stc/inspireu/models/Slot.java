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
@Table(name = "slots")
public class Slot extends BaseEntity {

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private String venue;

    @Column
    private String qrCodeId;

    @Column(nullable = false)
    private String status;

    @Column
    private String description;

    @Column
    private Date sessionStart;

    @Column
    private Date sessionEnd;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "startupId")
    private Startup startup;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;
}
