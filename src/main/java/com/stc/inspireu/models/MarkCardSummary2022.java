package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "markcard_summaries")
public class MarkCardSummary2022 extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updatedUserId")
    private User updatedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "startupId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Startup startup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academyRoomId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AcademyRoom academyRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "markCardId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MarkCard2022 markCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refMarkCardSummaryId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MarkCardSummary2022 refMarkCardSummary;

    @Column(columnDefinition = "TEXT")
    private String jsonMarkCard;

    @Column
    private Float amountPaid;

    @Column
    private Boolean isMarkCardGenerated;

    @Column
    private Long progressReportId;
}
