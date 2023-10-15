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
@Table(name = "open_event_slots")
public class OpenEventSlot extends BaseEntity {

    @Column(nullable = false)
    private Date day;

    @Column(nullable = false)
    private Short startTimeHour;

    @Column(nullable = false)
    private Short startTimeMinute;

    @Column(nullable = false)
    private Short endTimeHour;

    @Column(nullable = false)
    private Short endTimeMinute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "openEventId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private OpenEvent openEvent;

    @Column(nullable = false)
    private String email;
}
